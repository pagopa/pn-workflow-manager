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
 * Assicura che esista un contenitore di aggregazione per la campagna specificata
 */
const ensureAggregate = (aggregates, campaignId, timestamp) => {
    if (!aggregates[campaignId]) {
        aggregates[campaignId] = createAggregate(timestamp);
    }
    return aggregates[campaignId];
};

/**
 * Estrae gli ID/sequenceNumbers per la lista di campagne fallite o andate in timeout
 */
const buildFailures = (campaignIds, aggregates, fieldName) =>
    campaignIds.flatMap((campaignId) => aggregates[campaignId]?.[fieldName] || []);

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

        try {
            // Considera solo gli eventi di tipo INSERT contenenti i dati della timeline
            if (record.eventName !== "INSERT" || !record.dynamodb?.NewImage) {
                continue;
            }

            const parsedData = unmarshall(record.dynamodb.NewImage);
            let campaignId = parsedData.campaignId;
            let timelineElementId = parsedData.timelineElementId;
            let category = parsedData.category;

            console.log("Parsed timeline event:", {
                timelineElementId,
                category,
                communicationType: parsedData.communicationType
            });

            // Valida la presenza dei campi obbligatori
            if (!campaignId) {
                console.warn(`Missing campaignId for record: ${record.kinesisSeqNumber}. Skipping.`);
                continue;
            }

            if (!timelineElementId) {
                console.warn(`Missing timelineElementId for record: ${record.kinesisSeqNumber}. Skipping.`);
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
                    console.error(
                        `Deduplication lock error for campaignId=${campaignId}, timelineElementId=${timelineElementId}, category=${category}:`,
                        dedupErr
                    );
                    recordFailures.push(record.kinesisSeqNumber);
                    continue;
                }
            }

            // Prepara o recupera l'aggregato in memoria per questa campagna
            const aggregate = ensureAggregate(campaignAggregates, campaignId, parsedData.timestamp);

            // Mantiene aggiornato l'ultimo timestamp utile di aggiornamento della campagna
            if (parsedData.timestamp > aggregate.lastTimestamp) {
                aggregate.lastTimestamp = parsedData.timestamp;
            }

            // Applica la logica delle metriche
            if (!applyCategoryMetric(aggregate.counters, category, parsedData)) {
                console.warn(`Category metric skipped/invalid for sequenceNumber=${record.kinesisSeqNumber}. Marked as failure.`);
                recordFailures.push(record.kinesisSeqNumber);

                if (timelineElementId && DEDUPLICATION_MANAGEMENT_ENABLED) {
                    lockedFailedTimelineElementIds.push(timelineElementId);
                }
                continue;
            }

            // Traccia gli id e i sequence numbers elaborati con successo
            aggregate.timelineElementIds.push(timelineElementId);
            aggregate.sequenceNumbers.push(record.kinesisSeqNumber);
        } catch (err) {
            console.error(
                `Parsing error on record: sequenceNumber=${record.kinesisSeqNumber}. Error:`,
                err
            );
            recordFailures.push(record.kinesisSeqNumber);
            if (typeof timelineElementId !== "undefined" && DEDUPLICATION_MANAGEMENT_ENABLED) {
                lockedFailedTimelineElementIds.push(timelineElementId);
            }
        }
    }

    const campaignIds = Object.keys(campaignAggregates).filter(
        (campaignId) => campaignAggregates[campaignId].timelineElementIds.length > 0
    );    const failedCampaigns = [];
    const timedOutCampaignIds = [];

    for (let index = 0; index < campaignIds.length; index++) {
        const campaignId = campaignIds[index];

        if (isTimedOut()) {
            console.warn("Stopping campaign updates because Lambda is close to timeout.");
            timedOutCampaignIds.push(...campaignIds.slice(index));
            break;
        }

        try {
            // Esegue la query di UPDATE condizionale/incrementale sulla tabella delle statistiche
            await updateCounters(client, STATS_TABLE, campaignId, campaignAggregates[campaignId], isTimedOut);
        } catch (error) {
            if (error?.name === TIMEOUT_GUARD_TRIGGERED) {
                console.warn(`Timeout guard triggered while updating campaign: ${campaignId}.`);
                timedOutCampaignIds.push(...campaignIds.slice(index));
                break;
            }

            failedCampaigns.push({ campaignId, reason: error });
        }
    }

    // Raccoglie tutti i timelineElementId dei record che non sono stati scritti a DB
    const failedTimelineElementIds = [
        ...lockedFailedTimelineElementIds,
        ...buildFailures(timedOutCampaignIds, campaignAggregates, "timelineElementIds"),
        ...buildFailures(failedCampaigns.map(({ campaignId }) => campaignId), campaignAggregates, "timelineElementIds")
    ];

    // Rimuove i lock di deduplicazione per gli eventi falliti, consentendone il riprocessamento al retry Kinesis
    if (DEDUPLICATION_MANAGEMENT_ENABLED && failedTimelineElementIds.length > 0) {
        await removeDeduplicationLocks(client, DEDUP_TABLE, failedTimelineElementIds);
    }

    // Costruisce la risposta strutturata per Kinesis indicando solo gli elementi falliti
    const batchItemFailures = [
        ...recordFailures,
        ...buildFailures(timedOutCampaignIds, campaignAggregates, "sequenceNumbers"),
        ...buildFailures(failedCampaigns.map(({ campaignId }) => campaignId), campaignAggregates, "sequenceNumbers")
    ].map((sequenceNumber) => ({ itemIdentifier: sequenceNumber }));

    if (batchItemFailures.length > 0) {
        console.warn(
            `Batch completed with partial failures. Failed items=${batchItemFailures.length}. ` +
            `Failed campaigns=${JSON.stringify(failedCampaigns.map((f) => f.campaignId))}. ` +
            `Timed out campaigns=${JSON.stringify(timedOutCampaignIds)}.`
        );
    }

    return { batchItemFailures };
};