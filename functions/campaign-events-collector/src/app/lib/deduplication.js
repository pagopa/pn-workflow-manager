const {
    BatchWriteItemCommand,
    PutItemCommand
} = require("@aws-sdk/client-dynamodb");

const DEDUP_PARTITION_KEY = "timelineElementId";
const MAX_BATCH_DELETE_ITEMS = 25;

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