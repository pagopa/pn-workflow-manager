    const { DynamoDBClient, UpdateItemCommand } = require("@aws-sdk/client-dynamodb");

    // Esecuzione delle scritture atomiche cumulative su DynamoDB
    exports.updateCounters = async (campaignId) => {
        const aggregate = campaignAggregates[campaignId];
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
            ":lastTs": { S: aggregate.lastTimestamp }
        };

        counterKeys.forEach((key, index) => {
            expressionAttributeNames[`#c${index}`] = key;
            expressionAttributeValues[`:val${index}`] = { N: aggregate.counters[key].toString() };
        });

        const command = new UpdateItemCommand({
            TableName: STATS_TABLE,
            Key: {
                campaignId: { S: campaignId }
            },
            UpdateExpression: updateExpression,
            ExpressionAttributeNames: expressionAttributeNames,
            ExpressionAttributeValues: expressionAttributeValues
        });

        try {
            await dynamoDb.send(command);
            console.log(`Successfully updated statistics for campaign: ${campaignId}`);
        } catch (dbError) {
            console.error(`Failed atomic increment update for campaign ${campaignId}:`, dbError);
            throw dbError; // Rilanciato per attivare le logiche di DLQ/Retry sull'intero Batch Kinesis
        }
    };