const { expect } = require("chai");
const sinon = require("sinon");
const proxyquire = require("proxyquire").noCallThru().noPreserveCache();

describe("dbOperations", () => {
    let sendStub;
    let sendErrorStub;
    let batchWriteItemCommandStub;
    let putItemCommandStub;
    let updateItemCommandStub;
    let dbOperations;

    beforeEach(() => {
        sendStub = sinon.stub().resolves();
        sendErrorStub = sinon.stub().rejects(new Error("dynamo failure"));
        batchWriteItemCommandStub = sinon.stub().callsFake((params) => params);
        putItemCommandStub = sinon.stub().callsFake((params) => params);
        updateItemCommandStub = sinon.stub().callsFake((params) => params);

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

    it("skips update when no counters are present", async () => {
        await dbOperations.updateCounters({ send: sendStub }, "statistics", "CampagnaTest", {
            counters: {},
            lastTimestamp: "2026-01-01T00:00:00.000Z"
        });

        expect(updateItemCommandStub.called).to.be.false;
        expect(sendStub.called).to.be.false;
    });

    it("builds and sends update command with counters and provided timestamp", async () => {
        await dbOperations.updateCounters({ send: sendStub }, "statistics", "CampagnaTest", {
            counters: { delivered: 5, failed: 2 },
            lastTimestamp: "2026-01-01T10:00:00.000Z"
        });

        expect(updateItemCommandStub.calledOnce).to.be.true;
        const params = updateItemCommandStub.firstCall.args[0];

        expect(params.TableName).to.equal("statistics");
        expect(params.Key).to.deep.equal({ campaignId: { S: "CampagnaTest" } });
        expect(params.UpdateExpression).to.equal("SET #lastTs = :lastTs ADD #c0 :val0, #c1 :val1");
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

    it("rethrows errors from DynamoDB update", async () => {
        let thrownError;

        try {
            await dbOperations.updateCounters({ send: sendErrorStub }, "stats-table", "CampagnaTest", {
                counters: { delivered: 5 },
                lastTimestamp: "2026-01-01T10:00:00.000Z"
            });
        } catch (error) {
            thrownError = error;
        }

        expect(thrownError.message).to.equal("dynamo failure");
        expect(sendErrorStub.calledOnce).to.be.true;
    });

    it("acquires deduplication lock with conditional put", async () => {
        await dbOperations.acquireDeduplicationLock({ send: sendStub }, "dedup-table", "timeline-1", 1893456000);

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
    });

    it("skips deduplication lock removal when no ids are provided", async () => {
        await dbOperations.removeDeduplicationLocks({ send: sendStub }, "dedup-table", []);

        expect(batchWriteItemCommandStub.called).to.be.false;
        expect(sendStub.called).to.be.false;
    });

    it("removes deduplication locks in DynamoDB batch chunks", async () => {
        const timelineElementIds = Array.from({ length: 26 }, (_, index) => `timeline-${index + 1}`);

        await dbOperations.removeDeduplicationLocks({ send: sendStub }, "dedup-table", timelineElementIds);

        expect(batchWriteItemCommandStub.callCount).to.equal(2);
        expect(sendStub.callCount).to.equal(2);
        expect(batchWriteItemCommandStub.firstCall.args[0].RequestItems["dedup-table"]).to.have.lengthOf(25);
        expect(batchWriteItemCommandStub.secondCall.args[0].RequestItems["dedup-table"]).to.have.lengthOf(1);
    });
});
