const { expect } = require("chai");
const sinon = require("sinon");
const proxyquire = require("proxyquire");

describe("dbOperations", () => {
    let batchWriteItemCommandStub;
    let putItemCommandStub;
    let sendStub;
    let updateItemCommandStub;
    let sendErrorStub;

    let dbOperations;
    const dbError = new Error("dynamo failure");

    beforeEach(() => {

        sendStub = sinon.stub().resolves();
        sendErrorStub = sinon.stub().rejects(dbError);
        batchWriteItemCommandStub = sinon.stub().callsFake((params) => ({ __params: params }));
        putItemCommandStub = sinon.stub().callsFake((params) => ({ __params: params }));
        updateItemCommandStub = sinon.stub().callsFake((params) => ({ __params: params }));

        dbOperations = proxyquire("../app/lib/dbOperations.js", {
            "@aws-sdk/client-dynamodb": {
                BatchWriteItemCommand: batchWriteItemCommandStub,
                PutItemCommand: putItemCommandStub,
                UpdateItemCommand: updateItemCommandStub
            }
        });
    });

    afterEach(() => {
        sinon.restore();
    });

    it("should skip update when no counters are present", async () => {
        const dynamoDb = { send: sendStub };

        await dbOperations.updateCounters(dynamoDb, "statistics", "CampagnaTest", {
            counters: {},
            lastTimestamp: "2026-01-01T00:00:00.000Z"
        });

        expect(updateItemCommandStub.called).to.be.false;
        expect(sendStub.called).to.be.false;
    });

    it("should build and send update command with counters and provided timestamp", async () => {
           const dynamoDb = { send: sendStub };
   
           await dbOperations.updateCounters(dynamoDb, "statistics", "CampagnaTest", {
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
                await dbOperations.updateCounters(dynamoDb, "stats-table", "CampagnaTest", {
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

    it("should acquire deduplication lock with conditional put", async () => {
       const dynamoDb = { send: sendStub };

       await dbOperations.acquireDeduplicationLock(
           dynamoDb,
           "dedup-table",
           "timeline-1",
           1893456000
       );

       expect(putItemCommandStub.calledOnce).to.be.true;
       const params = putItemCommandStub.firstCall.args[0];
       expect(params).to.deep.equal({
           TableName: "dedup-table",
           Item: {
               timelineElementId: { S: "timeline-1" },
               ttl: { N: "1893456000" }
           },
           ConditionExpression: "attribute_not_exists(timelineElementId)"
       });
       expect(sendStub.calledOnce).to.be.true;
    });

    it("should skip deduplication lock removal when no ids are provided", async () => {
       const dynamoDb = { send: sendStub };

       await dbOperations.removeDeduplicationLocks(dynamoDb, "dedup-table", []);

       expect(batchWriteItemCommandStub.called).to.be.false;
       expect(sendStub.called).to.be.false;
    });

    it("should remove deduplication locks in DynamoDB batch chunks", async () => {
       const dynamoDb = { send: sendStub };
       const timelineElementIds = Array.from({ length: 26 }, (_, index) => `timeline-${index + 1}`);

       await dbOperations.removeDeduplicationLocks(dynamoDb, "dedup-table", timelineElementIds);

       expect(batchWriteItemCommandStub.callCount).to.equal(2);
       expect(sendStub.callCount).to.equal(2);
       expect(batchWriteItemCommandStub.firstCall.args[0].RequestItems["dedup-table"]).to.have.lengthOf(25);
       expect(batchWriteItemCommandStub.secondCall.args[0].RequestItems["dedup-table"]).to.have.lengthOf(1);
    });
});
