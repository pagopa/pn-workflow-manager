const {
    UpdateItemCommand
} = require("@aws-sdk/client-dynamodb");
/**
 * Aggiornamento dei contatori su DynamoDB per una singola campagna
 * @param {DynamoDBClient} dynamoDb - Client DynamoDB inizializzato
 * @param {string} statsTable - Nome della tabella di statistiche
 * @param {string} campaignId - ID della campagna
 * @param {Object} aggregate - Oggetto aggregato con counters e lastTimestamp
 * @param {Object} isTimedOut - Funzione per verificare se il Lambda è vicino al timeout
 * @throws {Error} - Lancia un errore se l'aggiornamento fallisce o se il Lambda è vicino al timeout
 */
exports.updateCounters = async (dynamoDb, statsTable, campaignId,
                                aggregate, isTimedOut) => {
    const counterKeys = Object.keys(aggregate.counters);

    if (counterKeys.length === 0) return;

    if (typeof isTimedOut === "function" && isTimedOut()) {
        const timeoutError = new Error("Stopping campaign update because Lambda is close to timeout.");
        timeoutError.name = "TimeoutGuardTriggered";
        throw timeoutError;
    }

    const setExpression = "SET #lastTs = :lastTs";
    const addExpression = "ADD " + counterKeys.map((key, index) => `#c${index} :val${index}`).join(", ");
    const updateExpression = `${setExpression} ${addExpression}`;

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

exports.acquireDeduplicationLock = async (dynamoDb, dedupTable, timelineElementId, expiresAt) => {
    const { PutItemCommand } = require("@aws-sdk/client-dynamodb");
    const command = new PutItemCommand({
        TableName: dedupTable,
        Item: {
            timelineElementId: { S: timelineElementId },
            ttl: { N: expiresAt.toString() }
        },
        ConditionExpression: "attribute_not_exists(timelineElementId)"
    });

    await dynamoDb.send(command);
};

exports.removeDeduplicationLocks = async (dynamoDb, dedupTable, timelineElementIds) => {
    const { BatchWriteItemCommand } = require("@aws-sdk/client-dynamodb");
    if (!timelineElementIds.length) {
        return;
    }

    for (let index = 0; index < timelineElementIds.length; index += 25) {
        const chunk = timelineElementIds.slice(index, index + 25);
        const command = new BatchWriteItemCommand({
            RequestItems: {
                [dedupTable]: chunk.map((timelineElementId) => ({
                    DeleteRequest: {
                        Key: { timelineElementId: { S: timelineElementId } }
                    }
                }))
            }
        });

        await dynamoDb.send(command);
    }
};