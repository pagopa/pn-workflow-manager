const { DynamoDBClient } = require("@aws-sdk/client-dynamodb");
const { unmarshall } = require("@aws-sdk/util-dynamodb");
const { extractCampaignId } = require("./lib/campaignUtils");
const { updateCounters } = require("./lib/dbOperations");

// Inizializzazione del client DynamoDB fuori dall'handler per riuso delle connessioni nelle successive esecuzioni
const dynamoDb = new DynamoDBClient({ region: process.env.AWS_REGION || "eu-south-1" });
const STATS_TABLE = process.env.CAMPAIGN_STATISTICS_TABLE || "pn-CampaignStatistics";
const DIGITAL_CHANNELS = ["IO", "EMAIL", "PEC", "SMS"];
const ANALOG_CHANNELS = ["RS"]; 
const ALL_CHANNELS = [...DIGITAL_CHANNELS, ...ANALOG_CHANNELS];


const MetricCategories = { 
    "REQUEST_ACCEPTED": "totalSent",
    "REQUEST_REFUSED": "totalRefused",
    "WORKFLOW_ENDED_UNDELIVERABLE": "totalUndeliverable",
    "WORKFLOW_DONE": "workflowDone",
    "WORKFLOW_ENDED_REACHED": "totalReached",
    "WORKFLOW_ENDED_UNREACHED": "totalUnreached",
    "PAYMENT": "payed",
    "NOTIFICATION_VIEWED": "viewed",
    "SEND_DIGITAL_MESSAGE": (channel) => `digitalSent_{channel}`,
    "SEND_COURTESY_MESSAGE": (channel) => `digitalSent_received_${channel}`,
    "SEND_ANALOG_MESSAGE": "analogSent_RS",
    "REACHED": (channel) => `digitalSent_${channel}`,
};  

exports.handleEvent = async (event) => {
    console.log(`Processing batch of ${event.Records.length} records from Kinesis stream.`);
    
    // Mappa per aggregare gli incrementi cumulativi per campagna all'interno del batch
    const campaignAggregates = {};

    for (const cdcEvent of event.Records) {
        try {
            // Decodifica del payload Kinesis (Base64)
            const payloadString = Buffer.from(cdcEvent.kinesis.data, 'base64').toString('utf-8');
            const kinesisEvent = JSON.parse(payloadString);

            // Verifichiamo che l'evento sia di tipo INSERT sulla tabella pn-Timelines
            // il controllo è superfluo visto che la lambda è triggerata da un flusso Kinesis dedicato
            //  agli eventi di timeline, lo mettiamo per robustezza
            if (cdcEvent.eventName !== "INSERT" || !cdcEvent.dynamodb || !cdcEvent.dynamodb.NewImage) {
                continue;
            }

            const newImage = cdcEvent.dynamodb.NewImage;
            
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

            // Inizializzazione della struttura di aggregazione per la campagna corrente
            if (!campaignAggregates[campaignId]) {
                campaignAggregates[campaignId] = {
                    counters: {},
                    lastTimestamp: parsedData.timestamp
                };
            }

            // Aggiornamento dell'ultimo timestamp utile per tracciare la freschezza del dato
            if (parsedData.timestamp > campaignAggregates[campaignId].lastTimestamp) {
                campaignAggregates[campaignId].lastTimestamp = parsedData.timestamp;
            }

            //  categorie metriche globali
            
            const category = parsedData.category;
            const timelineElementId = parsedData.timelineElementId;
            let channel
            switch (category) {
                case "REQUEST_ACCEPTED": campaignAggregates[campaignId].counters[MetricCategories["REQUEST_ACCEPTED"]] = (campaignAggregates[campaignId].counters[MetricCategories["REQUEST_ACCEPTED"]] || 0) + 1; break;
                case "REQUEST_REFUSED": campaignAggregates[campaignId].counters[MetricCategories["REQUEST_REFUSED"]] = (campaignAggregates[campaignId].counters[MetricCategories["REQUEST_REFUSED"]] || 0) + 1; break;
                case "WORKFLOW_ENDED_UNDELIVERABLE": campaignAggregates[campaignId].counters[MetricCategories["WORKFLOW_ENDED_UNDELIVERABLE"]] = (campaignAggregates[campaignId].counters[MetricCategories["WORKFLOW_ENDED_UNDELIVERABLE"]] || 0) + 1; break;
                case "WORKFLOW_DONE":
                campaignAggregates[campaignId].counters[MetricCategories["WORKFLOW_DONE"]] = (campaignAggregates[campaignId].counters[MetricCategories["WORKFLOW_DONE"]] || 0) + 1;

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
                    let timelineElementId = parsedData.timelineElementId;
                    let tokens = timelineElementId.split("_");
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
        } catch (err) {
            console.error(`Parsing error on record: ${JSON.stringify(cdcEvent)}. Error:`, err);
            // In caso di errore irreversibile sul singolo record, l'esecuzione prosegue per non bloccare l'intero batch
        }
    }

    // Esecuzione delle scritture atomiche cumulative su DynamoDB
    const updatePromises = Object.keys(campaignAggregates).map(campaignId =>
        updateCounters(dynamoDb, STATS_TABLE, campaignId, campaignAggregates[campaignId])
    );

    await Promise.all(updatePromises);
    return { status: "SUCCESS", processedCampaigns: Object.keys(campaignAggregates).length };
};