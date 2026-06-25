const { UpdateItemCommand } = require("@aws-sdk/client-dynamodb");

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