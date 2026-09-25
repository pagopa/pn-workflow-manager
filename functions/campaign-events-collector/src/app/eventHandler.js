const { unmarshall } = require("@aws-sdk/util-dynamodb");
const config = require("config");
const { DynamoDBClient } = require("@aws-sdk/client-dynamodb");
const { updateCounters } = require("./lib/dbOperations");
const { acquireDeduplicationLock, removeDeduplicationLocks } = require("./lib/deduplication");
const { applyCategoryMetric } = require("./lib/timelineMetrics");
const { extractKinesisData } = require("./lib/kinesis");
const STATS_TABLE = config.get("CAMPAIGN_STATISTICS_TABLE");
const DEDUP_TABLE = config.get("CAMPAIGN_EVENTS_DEDUPLICATION_TABLE");
const DEDUP_TTL_DAYS = Number(config.get("CAMPAIGN_EVENTS_DEDUPLICATION_TTL_DAYS"));
const CONDITIONAL_CHECK_FAILED = "ConditionalCheckFailedException";
const TIMEOUT_GUARD_TRIGGERED = "TimeoutGuardTriggered";
const TIMEOUT_TOLERANCE_IN_MILLIS = config.get("RUN_TOLLERANCE_IN_MILLIS");
const DEDUPLICATION_MANAGEMENT_ENABLED = !["false", "0", "off"].includes(
    String(config.get("DEDUPLICATION_MANAGEMENT_ENABLED")).trim().toLowerCase()
);
const client = new DynamoDBClient({ region: config.get("REGION") });

/**
 * Verifica se la Lambda si sta avvicinando al tempo massimo di esecuzione consentito
 */
const isTimeToLeave = (context) =>
    typeof context?.getRemainingTimeInMillis === "function" &&
    context.getRemainingTimeInMillis() < TIMEOUT_TOLERANCE_IN_MILLIS;

/**
 * Inizializza la struttura dati in memoria per accumulare i contatori di una campagna
 */
const createAggregate = (timestamp) => ({
    counters: {},
    timelineElementIds: [],
    sequenceNumbers: [],
    lastTimestamp: timestamp
});

/**
 * Assicura che esista un contenitore di aggregazione per la campagna specificata di un determinato senderId
 */
const ensureAggregate = (aggregates, senderId, campaignId, timestamp) => {
    const aggregateKey = JSON.stringify([senderId, campaignId]);
    if (!aggregates[aggregateKey]) {
        aggregates[aggregateKey] = {
            ...createAggregate(timestamp),
            senderId,
            campaignId
        };
    }
    return aggregates[aggregateKey];
};

function logFatalError(message) {
    console.error(`* FATAL * ALLARM!: ${message}`);
}

/**
 * Estrae gli ID/sequenceNumbers per la lista di campagne fallite o andate in timeout
 */
const buildFailures = (aggregateKeys, aggregates, fieldName) =>
    aggregateKeys.flatMap((aggregateKey) => aggregates[aggregateKey]?.[fieldName] || []);

exports.handleEvent = async (event, context) => {
    const decodedRecords = extractKinesisData(event);
    const campaignAggregates = {};
    const recordFailures = [];
    const lockedFailedTimelineElementIds = [];
    const isTimedOut = () => isTimeToLeave(context);

    const dedupTtlSeconds = Math.floor(Date.now() / 1000) + (DEDUP_TTL_DAYS * 24 * 60 * 60);

    console.log(`Batch size: ${decodedRecords.length} cdc`);

    if (decodedRecords.length === 0) {
        console.log("No events to process");
        return { batchItemFailures: [] };
    }

    for (let i = 0; i < decodedRecords.length; i++) {
        const record = decodedRecords[i];
        if (isTimedOut()) {
            console.warn("Stopping record processing because Lambda is close to timeout.");
            recordFailures.push(...decodedRecords.slice(i).map(r => r.kinesisSeqNumber));
            break;
        }
        const parsedData = unmarshall(record.dynamodb.NewImage);
        let timelineElementId = parsedData.timelineElementId;
        const senderId = parsedData.paId;
        let campaignId = parsedData.campaignId;
        let category = parsedData.category;

        try {
            console.log("Parsed timeline event:", {
                timelineElementId,
                category,
                communicationType: parsedData.communicationType
            });

            // Valida la presenza dei campi obbligatori
            if (typeof senderId !== "string" || senderId.trim().length === 0) {
              logFatalError(`Missing or invalid paId for record: ${record.kinesisSeqNumber}. Skipping without retry.`);
              continue;
            }
            if (!campaignId) {
                logFatalError(`Missing campaignId for record: ${record.kinesisSeqNumber}. Skipping without retry.`);
                continue;
            }
            if (!timelineElementId) {
                logFatalError(`Missing timelineElementId for record: ${record.kinesisSeqNumber}. Skipping without retry.`);
                continue;
            }

            // Gestione del lock di deduplicazione su DynamoDB
            if (DEDUPLICATION_MANAGEMENT_ENABLED) {
                try {
                    await acquireDeduplicationLock(client, DEDUP_TABLE, timelineElementId, dedupTtlSeconds);
                } catch (dedupErr) {
                    // Se il record esiste già, l'evento è un duplicato e viene ignorato in sicurezza
                    if (dedupErr?.name === CONDITIONAL_CHECK_FAILED) {
                        console.log(`Duplicate event detected for timelineElementId: ${timelineElementId}. Skipping.`);
                        continue;
                    }

                    // Se fallisce per un errore di rete o DB, segnala il record come fallito per riprovare
                    logFatalError(`Deduplication lock error for campaignId=${campaignId}, timelineElementId=${timelineElementId}, category=${category}: ${dedupErr}`);
                    recordFailures.push(record.kinesisSeqNumber);
                    continue;
                }
            }

            // Prepara o recupera l'aggregato in memoria per questa campagna
            const aggregate = ensureAggregate(
                campaignAggregates,
                senderId,
                campaignId,
                parsedData.timestamp
            );

            // Mantiene aggiornato l'ultimo timestamp utile di aggiornamento della campagna
            if (parsedData.timestamp > aggregate.lastTimestamp) {
                aggregate.lastTimestamp = parsedData.timestamp;
            }

            // Applica la logica delle metriche
            if (!applyCategoryMetric(aggregate.counters, category, parsedData)) {
                logFatalError(`Category metric skipped/invalid for sequenceNumber=${record.kinesisSeqNumber}. Skipping without retry.`);
                continue;
            }

            // Traccia gli id e i sequence numbers elaborati con successo
            aggregate.timelineElementIds.push(timelineElementId);
            aggregate.sequenceNumbers.push(record.kinesisSeqNumber);
        } catch (err) {
            logFatalError(`Parsing error on record: sequenceNumber=${record.kinesisSeqNumber}. Error: ${err}`);
            recordFailures.push(record.kinesisSeqNumber);
            if (typeof timelineElementId !== "undefined" && DEDUPLICATION_MANAGEMENT_ENABLED) {
                lockedFailedTimelineElementIds.push(timelineElementId);
            }
        }
    }

    //recupera le chiavi degli aggregati identificati dalla coppia senderId+campaignId
    const aggregateKeys = Object.keys(campaignAggregates).filter(
        (aggregateKey) => campaignAggregates[aggregateKey].timelineElementIds.length > 0
    );

    const failedCampaigns = [];
    const timedOutAggregateKeys = [];

    for (let index = 0; index < aggregateKeys.length; index++) {
        const aggregateKey = aggregateKeys[index];

        if (isTimedOut()) {
            console.warn("Stopping campaign updates because Lambda is close to timeout.");
            timedOutAggregateKeys.push(...aggregateKeys.slice(index));
            break;
        }

        try {
            const aggregate = campaignAggregates[aggregateKey];

            await updateCounters(
                client,
                STATS_TABLE,
                aggregate.senderId,
                aggregate.campaignId,
                aggregate,
                isTimedOut
            );
        } catch (error) {
            if (error?.name === TIMEOUT_GUARD_TRIGGERED) {
                console.warn(`Timeout guard triggered while updating campaign: ${aggregateKey}.`);
                timedOutAggregateKeys.push(...aggregateKeys.slice(index));
                break;
            }

            failedCampaigns.push({ aggregateKey, reason: error });
        }
    }

    // Raccoglie tutti i timelineElementId dei record che non sono stati scritti a DB
    const failedTimelineElementIds = [
        ...lockedFailedTimelineElementIds,
        ...buildFailures(timedOutAggregateKeys, campaignAggregates, "timelineElementIds"),
        ...buildFailures(failedCampaigns.map(({ aggregateKey }) => aggregateKey), campaignAggregates, "timelineElementIds")
    ];

    // Rimuove i lock di deduplicazione per gli eventi falliti, consentendone il riprocessamento al retry Kinesis
    if (DEDUPLICATION_MANAGEMENT_ENABLED && failedTimelineElementIds.length > 0) {
        await removeDeduplicationLocks(client, DEDUP_TABLE, failedTimelineElementIds);
    }

    // Costruisce la risposta strutturata per Kinesis indicando solo gli elementi falliti
    const batchItemFailures = [
        ...recordFailures,
        ...buildFailures(timedOutAggregateKeys, campaignAggregates, "sequenceNumbers"),
        ...buildFailures(failedCampaigns.map(({ aggregateKey }) => aggregateKey), campaignAggregates, "sequenceNumbers")
    ].map((sequenceNumber) => ({ itemIdentifier: sequenceNumber }));

    if (batchItemFailures.length > 0) {
        console.warn(
            `Batch completed with partial failures. Failed items=${batchItemFailures.length}. ` +
            `Failed campaigns=${JSON.stringify(failedCampaigns.map((f) => f.aggregateKey))}. ` +
            `Timed out campaigns=${JSON.stringify(timedOutAggregateKeys)}.`
        );
    }

    return { batchItemFailures };
};