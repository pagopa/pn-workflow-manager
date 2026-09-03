const { expect } = require("chai");
const sinon = require("sinon");
const proxyquire = require("proxyquire").noCallThru().noPreserveCache();
const { unmarshall } = require("@aws-sdk/util-dynamodb");
const encode = (payload) => Buffer.from(JSON.stringify(payload)).toString("base64");

describe("eventHandler", () => {
    let handleEvent;
    let unmarshallStub;
    let acquireDeduplicationLockStub;
    let removeDeduplicationLocksStub;
    let updateCountersStub;
    let configStub;
    let consoleWarnStub;
    let consoleErrorStub;

    const loadHandler = () => proxyquire("../app/eventHandler", {
        config: configStub,
        "@aws-sdk/client-dynamodb": {
            DynamoDBClient: sinon.stub().returns({ send: sinon.stub().resolves() })
        },
        "./lib/deduplication": {
            acquireDeduplicationLock: acquireDeduplicationLockStub,
            removeDeduplicationLocks: removeDeduplicationLocksStub
        },
        "./lib/dbOperations": {
            updateCounters: updateCountersStub
        },
        "./lib/kinesis": {
            extractKinesisData: (event) => event.Records.map((record) => {
                const decoded = typeof record.kinesis.data === "string"
                    ? JSON.parse(Buffer.from(record.kinesis.data, "base64").toString("utf8"))
                    : record.kinesis.data;
                return {
                    kinesisSeqNumber: record.kinesis.sequenceNumber,
                    ...decoded
                };
            }).filter((item) => !!item.dynamodb?.NewImage?.communicationType?.S)
        }
    }).handleEvent;

    beforeEach(() => {
        unmarshallStub = sinon.stub().callsFake((newImage) => unmarshall(newImage));
        acquireDeduplicationLockStub = sinon.stub().resolves();
        removeDeduplicationLocksStub = sinon.stub().resolves();
        updateCountersStub = sinon.stub().resolves();
        configStub = {
            get: sinon.stub()
        };
        configStub.get.withArgs("RUN_TOLLERANCE_IN_MILLIS").returns(3000);
        configStub.get.withArgs("DEDUPLICATION_MANAGEMENT_ENABLED").returns(true);
        configStub.get.withArgs("CAMPAIGN_STATISTICS_TABLE").returns("statistics-table");
        configStub.get.withArgs("CAMPAIGN_EVENTS_DEDUPLICATION_TABLE").returns("dedup-table");
        configStub.get.withArgs("CAMPAIGN_EVENTS_DEDUPLICATION_TTL_DAYS").returns(7);
        configStub.get.withArgs("REGION").returns("eu-west-1");

        consoleWarnStub = sinon.stub(console, "warn");
        consoleErrorStub = sinon.stub(console, "error");

        handleEvent = loadHandler();
    });

    afterEach(() => {
        sinon.restore();
    });

    it("processes the example record and increments totalAccepted", async () => {
        const event = require("./kinesis.event.example.json");
        const result = await handleEvent({
            Records: event.Records.map((record) => ({
                ...record,
                kinesis: {
                    ...record.kinesis,
                    data: encode(record.kinesis.data)
                }
            }))
        });

        expect(result).to.deep.equal({ batchItemFailures: [] });
        expect(updateCountersStub.calledOnce).to.be.true;
        expect(acquireDeduplicationLockStub.calledOnce).to.be.true;
        expect(updateCountersStub.firstCall.args[2]).to.equal("FattOrd");
        expect(updateCountersStub.firstCall.args[3].counters.totalAccepted).to.equal(1);
    });

    it("returns batchItemFailures when channel is invalid", async () => {
        const event = {
            Records: [{
                kinesis: {
                    sequenceNumber: "seq-1",
                    data: {
                        eventName: "INSERT",
                        dynamodb: {
                            NewImage: {
                                campaignId: { S: "campaign-1" },
                                timelineElementId: { S: "t-1" },
                                category: { S: "SEND_DIGITAL_MESSAGE" },
                                communicationType: { S: "INFORMAL" },
                                timestamp: { S: "2026-01-01T10:00:00.000Z" },
                                details: {
                                    M: {
                                        channel: { S: "INVALID" }
                                    }
                                }
                            }
                        }
                    }
                }
            }]
        };

        const result = await handleEvent(event);
        expect(result).to.deep.equal({
            batchItemFailures: [
                { itemIdentifier: "seq-1" }
            ]
        });
        expect(updateCountersStub.called).to.be.false;
        expect(removeDeduplicationLocksStub.calledOnce).to.be.true;
        expect(removeDeduplicationLocksStub.firstCall.args[2]).to.deep.equal(["t-1"]);
    });

    it("stops when timeout is close", async () => {
        const timeoutStub = sinon.stub();
        timeoutStub.onCall(0).returns(999999); // non in timeout durante l'elaborazione del record
        timeoutStub.onCall(1).returns(1000);   // in timeout prima dell'update dei contatori di campagna

        const event = {
            Records: [{
                kinesis: {
                    sequenceNumber: "seq-1",
                    data: {
                        eventName: "INSERT",
                        dynamodb: {
                            NewImage: {
                                campaignId: { S: "campaign-1" },
                                timelineElementId: { S: "t-1" },
                                category: { S: "REQUEST_ACCEPTED" },
                                communicationType: { S: "INFORMAL" },
                                timestamp: { S: "2026-01-01T10:00:00.000Z" }
                            }
                        }
                    }
                }
            }]
        };

        const result = await handleEvent(event, { getRemainingTimeInMillis: timeoutStub });

        expect(result.batchItemFailures).to.have.lengthOf(1);
        expect(removeDeduplicationLocksStub.calledOnce).to.be.true;
    });
});
