const { expect } = require("chai");
const sinon = require("sinon");
const proxyquire = require("proxyquire");

describe("dbOperations", () => {
    let sendStub;
    let updateItemCommandStub;
    let sendErrorStub;

    let updateCounters;
    const dbError = new Error("dynamo failure");

    beforeEach(() => {

        sendStub = sinon.stub().resolves();
        sendErrorStub = sinon.stub().rejects(dbError);
        updateItemCommandStub = sinon.stub().callsFake((params) => ({ __params: params }));

        updateCounters = proxyquire("../app/lib/dbOperations.js", {
            "@aws-sdk/client-dynamodb": {
                UpdateItemCommand: updateItemCommandStub
            }
        });
    });

    afterEach(() => {
        sinon.restore();
    });

    it("should skip update when no counters are present", async () => {
        const dynamoDb = { send: sendStub };

        await updateCounters.updateCounters(dynamoDb, "statistics", "CampagnaTest", {
            counters: {},
            lastTimestamp: "2026-01-01T00:00:00.000Z"
        });

        expect(updateItemCommandStub.called).to.be.false;
        expect(sendStub.called).to.be.false;
    });

    it("should build and send update command with counters and provided timestamp", async () => {
           const dynamoDb = { send: sendStub };
   
           await updateCounters.updateCounters(dynamoDb, "statistics", "CampagnaTest", {
               counters: {
                   delivered: 5,
                   failed: 2
               },
               lastTimestamp: "2026-01-01T10:00:00.000Z"
           });
   
           expect(updateItemCommandStub.calledOnce).to.be.true;
           expect(sendStub.called).to.be.true;

           const params = updateItemCommandStub.firstCall.args[0];
   
           expect(params.TableName).to.equal("statistics");
           expect(params.Key).to.deep.equal({
               campaignId: { S: "CampagnaTest" }
           });
           expect(params.UpdateExpression).to.equal("ADD #c0 :val0, #c1 :val1 SET #lastTs = :lastTs");
           expect(params.ExpressionAttributeNames).to.deep.equal({
               "#lastTs": "lastCompletedTimestamp",
               "#c0": "delivered",
               "#c1": "failed"
           });
           expect(params.ExpressionAttributeValues).to.deep.equal({
               ":lastTs": { S: "2026-01-01T10:00:00.000Z" },
               ":val0": { N: "5" },
               ":val1": { N: "2" }
           });
        });
   
       it("should rethrow errors from DynamoDB update", async () => {
           const dynamoDb = { send: sendErrorStub };
           let thrownError;
           try {
                await updateCounters.updateCounters(dynamoDb, "stats-table", "CampagnaTest", {
                   counters: {
                       delivered: 5
                   },
                   lastTimestamp: "2026-01-01T10:00:00.000Z"
               });
           } catch (error) {
               thrownError = error;
           }
   
           expect(thrownError).to.equal(dbError);
           expect(sendErrorStub.calledOnce).to.be.true;
       });
});
