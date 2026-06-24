const { unmarshall } = require("@aws-sdk/util-dynamodb");
const { extractCampaignId } = require("./lib/campaignUtils");
const {
    acquireDeduplicationLock,
    removeDeduplicationLocks,
    updateCounters
} = require("./lib/dbOperations");

// Inizializzazione del client DynamoDB fuori dall'handler per riuso delle connessioni nelle successive esecuzioni
const STATS_TABLE = process.env.CAMPAIGN_STATISTICS_TABLE || "pn-CampaignStatistics";
const DEDUP_TABLE = process.env.CAMPAIGN_EVENTS_DEDUPLICATION_TABLE || "pn-CampaignEventsDeduplication";
const DEDUP_TTL_DAYS = Number(process.env.CAMPAIGN_EVENTS_DEDUPLICATION_TTL_DAYS || 7);
const { DynamoDBClient } = require("@aws-sdk/client-dynamodb");

const client = new DynamoDBClient({ region: process.env.REGION });



const DIGITAL_CHANNELS = ["IO", "EMAIL", "PEC", "SMS"];
const ANALOG_CHANNELS = ["RS"]; 
const ALL_CHANNELS = [...DIGITAL_CHANNELS, ...ANALOG_CHANNELS];
const CONDITIONAL_CHECK_FAILED = "ConditionalCheckFailedException";


const MetricCategories = { 
    "REQUEST_ACCEPTED": "totalSent",
    "REQUEST_REFUSED": "totalRefused",
    "WORKFLOW_ENDED_UNDELIVERABLE": "totalUndeliverable",
    "WORKFLOW_DONE": "workflowDone",
    "WORKFLOW_ENDED_REACHED": "totalReached",
    "WORKFLOW_ENDED_UNREACHED": "totalUnreached",
    "PAYMENT": "payed",
    "NOTIFICATION_VIEWED": "viewed",
    "SEND_DIGITAL_MESSAGE": (channel) => `digitalSent_${channel}`,
    "SEND_COURTESY_MESSAGE": (channel) => `digitalSent_received_${channel}`,
    "SEND_ANALOG_MESSAGE": "analogSent_RS",
    "REACHED": (channel) => `digitalSent_${channel}`,
};  

exports.handleEvent = async (event) => {
    console.log(`Processing batch of ${event.Records.length} records from Kinesis stream.`);
    
    // Mappa per aggregare gli incrementi cumulativi per campagna all'interno del batch
    const campaignAggregates = {};
    const dedupTtlSeconds = Math.floor(Date.now() / 1000) + (DEDUP_TTL_DAYS * 24 * 60 * 60);

    for (const cdcEvent of event.Records) {
        try {
            // Decodifica del payload Kinesis (Base64) — il DynamoDB CDC event è dentro kinesis.data
            const kinesisEvent = JSON.parse(Buffer.from(cdcEvent.kinesis.data, 'base64').toString('utf-8'));

            // Verifichiamo che l'evento sia di tipo INSERT sulla tabella pn-Timelines
            // il controllo è superfluo visto che la lambda è triggerata da un flusso Kinesis dedicato
            //  agli eventi di timeline, lo mettiamo per robustezza
            if (kinesisEvent.eventName !== "INSERT" || !kinesisEvent.dynamodb?.NewImage) {
                continue;
            }

            const newImage = kinesisEvent.dynamodb.NewImage;

            const parsedData = unmarshall(newImage);
            console.log("Parsed timeline event:", {
                timelineElementId: parsedData.timelineElementId,
                category: parsedData.category,
                communicationType: parsedData.communicationType
            });

            // Estrazione attributi core. Nota: un'ottimizzazione  prevede il campaignId inserito 
            // direttamente nel contesto dell'evento di timeline per evitare lookup
            const communicationType = parsedData.communicationType;
            // altro check di robustezza per il dominio delle comunicazioni bonarie
            if (communicationType !== "INFORMAL") {
                continue;
            }

            


            // Risoluzione dinamica del campaignId. 
            // Se non presente nell'evento di timeline, si assume una fallback o estrazione da logica applicativa correlata.
            const campaignId = parsedData.campaignId || await extractCampaignId(parsedData);
            if (!campaignId) {
                console.warn(`Missing campaignId for record: ${cdcEvent.eventID}. Skipping.`);
                continue;
            }

            const category = parsedData.category;
            const timelineElementId = parsedData.timelineElementId;

            try {
                await acquireDeduplicationLock(client, DEDUP_TABLE, timelineElementId, dedupTtlSeconds);
            } catch (dedupErr) {
                if (dedupErr?.name === CONDITIONAL_CHECK_FAILED) {
                    console.log(`Duplicate event detected for timelineElementId: ${timelineElementId}. Skipping.`);
                    continue;
                }

                dedupErr.isFatal = true;
                throw dedupErr;
            }

            // Inizializzazione della struttura di aggregazione per la campagna corrente
            if (!campaignAggregates[campaignId]) {
                campaignAggregates[campaignId] = {
                    counters: {},
                    timelineElementIds: [],
                    lastTimestamp: parsedData.timestamp
                };
            }

            // Aggiornamento dell'ultimo timestamp utile per tracciare la freschezza del dato
            if (parsedData.timestamp > campaignAggregates[campaignId].lastTimestamp) {
                campaignAggregates[campaignId].lastTimestamp = parsedData.timestamp;
            }

            //  categorie metriche globali
            let channel
            switch (category) {
                case "REQUEST_ACCEPTED": campaignAggregates[campaignId].counters[MetricCategories["REQUEST_ACCEPTED"]] = (campaignAggregates[campaignId].counters[MetricCategories["REQUEST_ACCEPTED"]] || 0) + 1; break;
                case "REQUEST_REFUSED": campaignAggregates[campaignId].counters[MetricCategories["REQUEST_REFUSED"]] = (campaignAggregates[campaignId].counters[MetricCategories["REQUEST_REFUSED"]] || 0) + 1; break;
                case "WORKFLOW_ENDED_UNDELIVERABLE": campaignAggregates[campaignId].counters[MetricCategories["WORKFLOW_ENDED_UNDELIVERABLE"]] = (campaignAggregates[campaignId].counters[MetricCategories["WORKFLOW_ENDED_UNDELIVERABLE"]] || 0) + 1; break;
                case "WORKFLOW_DONE":
                    campaignAggregates[campaignId].counters[MetricCategories["WORKFLOW_DONE"]] =
                            (campaignAggregates[campaignId].counters[MetricCategories["WORKFLOW_DONE"]] || 0) + 1;

                 // TODO
                 // Aggiungere logica per distinguere lo stato di partenza per decrementare i contatori totalUndeliverable, totalReached, unreached, ecc. in base al flusso di stato precedente se necessario.
                break;
                case "WORKFLOW_ENDED_REACHED": campaignAggregates[campaignId].counters[MetricCategories["WORKFLOW_ENDED_REACHED"]] = (campaignAggregates[campaignId].counters[MetricCategories["WORKFLOW_ENDED_REACHED"]] || 0) + 1; break;
                case "WORKFLOW_ENDED_UNREACHED": campaignAggregates[campaignId].counters[MetricCategories["WORKFLOW_ENDED_UNREACHED"]] = (campaignAggregates[campaignId].counters[MetricCategories["WORKFLOW_ENDED_UNREACHED"]] || 0) + 1; break;
                case "NOTIFICATION_VIEWED": campaignAggregates[campaignId].counters[MetricCategories["NOTIFICATION_VIEWED"]] = (campaignAggregates[campaignId].counters[MetricCategories["NOTIFICATION_VIEWED"]] || 0) + 1; break;
                case "SEND_DIGITAL_MESSAGE":
                case "SEND_COURTESY_MESSAGE":
                    channel = parsedData.details?.channel; // IO, PEC, EMAIL, SMS, ANALOG
                    if (DIGITAL_CHANNELS.includes(channel))
                        campaignAggregates[campaignId].counters[MetricCategories[category](channel)] = (campaignAggregates[campaignId].counters[MetricCategories[category](channel)] || 0) + 1;
                    else {
                        console.warn(`Unexpected channel for ${category}: ${channel}`);
                        continue;
                    }
                break;
                case "SEND_ANALOG_MESSAGE":
                    campaignAggregates[campaignId].counters[MetricCategories["SEND_ANALOG_MESSAGE"]] = (campaignAggregates[campaignId].counters[MetricCategories["SEND_ANALOG_MESSAGE"]] || 0) + 1;
                break;
                case "REACHED":
                    const tokens = timelineElementId.split("_");
                    if (tokens.length < 3) {
                        console.warn(`Unexpected timelineElementId format: ${timelineElementId} for record: ${cdcEvent.eventID}. Unable to extract channel. Skipping channel-specific metrics.`);
                        break;
                    }
                     // Il  formato del REACHED deve essere consistente con REACHED_<IUN>_<channel>
                    channel = tokens[2];
                    if (ALL_CHANNELS.includes(channel)) {
                        campaignAggregates[campaignId].counters[MetricCategories["REACHED"](channel)] =
                            (campaignAggregates[campaignId].counters[MetricCategories["REACHED"](channel)] || 0) + 1;
                    }
                    break;
                case "PAYMENT":
                    campaignAggregates[campaignId].counters[MetricCategories["PAYMENT"]] = (campaignAggregates[campaignId].counters[MetricCategories["PAYMENT"]] || 0) + 1;
                break;
                default:
                    console.log(`Unhandled category: ${category} for campaign: ${campaignId}`);

            }

            campaignAggregates[campaignId].timelineElementIds.push(timelineElementId);
        } catch (err) {
            if (err?.isFatal) {
                throw err;
            }
            console.error(`Parsing error on record: ${JSON.stringify(cdcEvent)}. Error:`, err);
            // In caso di errore irreversibile sul singolo record, l'esecuzione prosegue per non bloccare l'intero batch
        }
    }

    // Esecuzione delle scritture atomiche cumulative su DynamoDB.
    const campaignIds = Object.keys(campaignAggregates);
    const updateResults = await Promise.allSettled(
        campaignIds.map(async (campaignId) => {
            await updateCounters(client, STATS_TABLE, campaignId, campaignAggregates[campaignId]);
            return campaignId;
        })
    );

    const failedCampaigns = updateResults.flatMap((result, index) => (
        result.status === "rejected"
            ? [{ campaignId: campaignIds[index], reason: result.reason }]
            : []
    ));

    if (failedCampaigns.length > 0) {
        for (const { campaignId } of failedCampaigns) {
            const timelineElementIds = campaignAggregates[campaignId]?.timelineElementIds || [];
            await removeDeduplicationLocks(client, DEDUP_TABLE, timelineElementIds);
        }

        throw failedCampaigns[0].reason;
    }

    return { status: "SUCCESS", processedCampaigns: campaignIds.length };
};