const {
    UpdateItemCommand
} = require("@aws-sdk/client-dynamodb");
/**
 * Aggiornamento dei contatori su DynamoDB per una singola campagna
 * @param {DynamoDBClient} dynamoDb - Client DynamoDB inizializzato
 * @param {string} statsTable - Nome della tabella di statistiche
 * @param {string} senderId - ID del mittente
 * @param {string} campaignId - ID della campagna
 * @param {Object} aggregate - Oggetto aggregato con counters e lastTimestamp
 * @param {Object} isTimedOut - Funzione per verificare se il Lambda è vicino al timeout
 * @throws {Error} - Lancia un errore se l'aggiornamento fallisce o se il Lambda è vicino al timeout
 */
exports.updateCounters = async (
    dynamoDb, statsTable, senderId, campaignId, aggregate, isTimedOut) => {
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
            senderId: { S: senderId },
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