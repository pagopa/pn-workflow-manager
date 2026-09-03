const { expect } = require("chai");
const sinon = require("sinon");
const proxyquire = require("proxyquire").noCallThru().noPreserveCache();

describe("deduplication", () => {
    let sendStub;
    let batchWriteItemCommandStub;
    let putItemCommandStub;
    let deduplication;

    beforeEach(() => {
        sendStub = sinon.stub().resolves();
        batchWriteItemCommandStub = sinon.stub().callsFake((params) => params);
        putItemCommandStub = sinon.stub().callsFake((params) => params);

        deduplication = proxyquire("../app/lib/deduplication", {
            "@aws-sdk/client-dynamodb": {
                BatchWriteItemCommand: batchWriteItemCommandStub,
                PutItemCommand: putItemCommandStub
            }
        });
    });

    afterEach(() => {
        sinon.restore();
    });

    it("creates a dedup lock", async () => {
        await deduplication.acquireDeduplicationLock({ send: sendStub }, "dedup", "timeline-1", 123);

        expect(putItemCommandStub.calledOnce).to.be.true;
        expect(putItemCommandStub.firstCall.args[0].TableName).to.equal("dedup");
    });

    it("removes dedup locks in chunks", async () => {
        await deduplication.removeDeduplicationLocks({ send: sendStub }, "dedup", Array.from({ length: 26 }, (_, i) => `t-${i + 1}`));

        expect(batchWriteItemCommandStub.callCount).to.equal(2);
        expect(sendStub.callCount).to.equal(2);
    });

    it("does nothing when there are no ids", async () => {
        await deduplication.removeDeduplicationLocks({ send: sendStub }, "dedup", []);
        expect(sendStub.called).to.be.false;
    });
});