const {
    BatchWriteItemCommand,
    PutItemCommand,
    UpdateItemCommand
} = require("@aws-sdk/client-dynamodb");

const DEDUP_PARTITION_KEY = "timelineElementId";
const MAX_BATCH_DELETE_ITEMS = 25;

/**
 * Aggiornamento dei contatori su DynamoDB per una singola campagna
 * @param {DynamoDBClient} dynamoDb - Client DynamoDB inizializzato
 * @param {string} statsTable - Nome della tabella di statistiche
 * @param {string} campaignId - ID della campagna
 * @param {Object} aggregate - Oggetto aggregato con counters e lastTimestamp
 */
exports.updateCounters = async (dynamoDb, statsTable, campaignId, aggregate) => {
    const counterKeys = Object.keys(aggregate.counters);

    if (counterKeys.length === 0) return;

    // Costruzione dinamica dell'espressione ADD per l'incremento atomico dei contatori modificati
    let updateExpression = "ADD " + counterKeys.map((key, index) => `#c${index} :val${index}`).join(", ");

    // Aggiungiamo l'aggiornamento fisso per il timestamp dell'ultima elaborazione
    updateExpression += " SET #lastTs = :lastTs";

    const expressionAttributeNames = {
        "#lastTs": "lastCompletedTimestamp"
    };
    const expressionAttributeValues = {
        ":lastTs": { S: aggregate.lastTimestamp?.toString() || new Date().toISOString() }
    };

    counterKeys.forEach((key, index) => {
        expressionAttributeNames[`#c${index}`] = key;
        expressionAttributeValues[`:val${index}`] = { N: aggregate.counters[key].toString() };
    });

    const command = new UpdateItemCommand({
        TableName: statsTable,
        Key: {
            campaignId: { S: campaignId }
        },
        UpdateExpression: updateExpression,
        ExpressionAttributeNames: expressionAttributeNames,
        ExpressionAttributeValues: expressionAttributeValues
    });

    try {
        await dynamoDb.send(command);
        console.log(`Updated campaign data for: ${campaignId}. Counters: ${JSON.stringify(aggregate.counters)}`);
    } catch (dbError) {
        console.error(`Error updating ${campaignId} data. Counters: ${JSON.stringify(aggregate.counters)}`, dbError);
        throw dbError;
    }
};

/**
 * Prova ad acquisire il lock di deduplica per un evento timeline.
 * La scrittura condizionale evita doppie elaborazioni su retry Kinesis.
 *
 * @param {DynamoDBClient} dynamoDb - Client DynamoDB inizializzato
 * @param {string} dedupTable - Nome tabella deduplica
 * @param {string} timelineElementId - Identificativo univoco evento timeline
 * @param {number} expiresAt - Epoch seconds per TTL
 */
exports.acquireDeduplicationLock = async (dynamoDb, dedupTable, timelineElementId, expiresAt) => {
    const command = new PutItemCommand({
        TableName: dedupTable,
        Item: {
            [DEDUP_PARTITION_KEY]: { S: timelineElementId },
            ttl: { N: expiresAt.toString() }
        },
        ConditionExpression: "attribute_not_exists(timelineElementId)"
    });

    await dynamoDb.send(command);
};

/**
 * Rimuove i lock di deduplica per consentire il retry di eventi non persistiti su statistiche.
 *
 * @param {DynamoDBClient} dynamoDb - Client DynamoDB inizializzato
 * @param {string} dedupTable - Nome tabella deduplica
 * @param {string[]} timelineElementIds - Lista lock da rimuovere
 */
exports.removeDeduplicationLocks = async (dynamoDb, dedupTable, timelineElementIds) => {
    if (!timelineElementIds.length) {
        return;
    }

    for (let index = 0; index < timelineElementIds.length; index += MAX_BATCH_DELETE_ITEMS) {
        const chunk = timelineElementIds.slice(index, index + MAX_BATCH_DELETE_ITEMS);
        const command = new BatchWriteItemCommand({
            RequestItems: {
                [dedupTable]: chunk.map((timelineElementId) => ({
                    DeleteRequest: {
                        Key: {
                            [DEDUP_PARTITION_KEY]: { S: timelineElementId }
                        }
                    }
                }))
            }
        });

        await dynamoDb.send(command);
    }
};