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

    const createRecord = (senderId, campaignId, sequenceNumber, timestamp) => ({
        kinesis: {
            sequenceNumber,
            data: encode({
                eventName: "INSERT",
                dynamodb: {
                    NewImage: {
                        paId: { S: senderId },
                        campaignId: { S: campaignId },
                        timelineElementId: { S: `timeline-${sequenceNumber}` },
                        category: { S: "REQUEST_ACCEPTED" },
                        communicationType: { S: "INFORMAL" },
                        timestamp: {
                            S: timestamp || "2026-01-01T10:00:00.000Z"
                        }
                    }
                }
            })
        }
    });

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

    it("aggregates records for the same sender and campaign", async () => {
        const result = await handleEvent({
            Records: [
                createRecord(
                    "sender-1", "campaign-1", "1",
                    "2026-01-01T10:00:00.000Z"
                ),
                createRecord(
                    "sender-1", "campaign-1", "2",
                    "2026-01-01T11:00:00.000Z"
                )
            ]
        });

        expect(result).to.deep.equal({ batchItemFailures: [] });
        expect(acquireDeduplicationLockStub.callCount).to.equal(2);
        expect(updateCountersStub.calledOnce).to.be.true;
        expect(updateCountersStub.firstCall.args.slice(1, 4)).to.deep.equal([
            "statistics-table", "sender-1", "campaign-1"
        ]);
        expect(updateCountersStub.firstCall.args[4]).to.deep.equal({
            senderId: "sender-1",
            campaignId: "campaign-1",
            counters: { totalAccepted: 2 },
            timelineElementIds: ["timeline-1", "timeline-2"],
            sequenceNumbers: ["1", "2"],
            lastTimestamp: "2026-01-01T11:00:00.000Z"
        });
        expect(removeDeduplicationLocksStub.called).to.be.false;
    });

    it("keeps the same campaign separate for different senders", async () => {
        const result = await handleEvent({
            Records: [
                createRecord("sender-1", "campaign-1", "1"),
                createRecord(
                    "sender-2", "campaign-1", "2",
                    "2026-01-01T12:00:00.000Z"
                ),
                createRecord(
                    "sender-1", "campaign-1", "3",
                    "2026-01-01T11:00:00.000Z"
                )
            ]
        });

        expect(result).to.deep.equal({ batchItemFailures: [] });
        expect(updateCountersStub.callCount).to.equal(2);

        const updates = updateCountersStub.getCalls().map(({ args }) => ({
            senderId: args[2],
            campaignId: args[3],
            aggregate: args[4]
        }));

        expect(updates).to.have.deep.members([
            {
                senderId: "sender-1",
                campaignId: "campaign-1",
                aggregate: {
                    senderId: "sender-1",
                    campaignId: "campaign-1",
                    counters: { totalAccepted: 2 },
                    timelineElementIds: ["timeline-1", "timeline-3"],
                    sequenceNumbers: ["1", "3"],
                    lastTimestamp: "2026-01-01T11:00:00.000Z"
                }
            },
            {
                senderId: "sender-2",
                campaignId: "campaign-1",
                aggregate: {
                    senderId: "sender-2",
                    campaignId: "campaign-1",
                    counters: { totalAccepted: 1 },
                    timelineElementIds: ["timeline-2"],
                    sequenceNumbers: ["2"],
                    lastTimestamp: "2026-01-01T12:00:00.000Z"
                }
            }
        ]);
        expect(removeDeduplicationLocksStub.called).to.be.false;
    });

    it("keeps different campaigns separate for the same sender", async () => {
        const result = await handleEvent({
            Records: [
                createRecord("sender-1", "campaign-1", "1"),
                createRecord("sender-1", "campaign-2", "2")
            ]
        });

        expect(result).to.deep.equal({ batchItemFailures: [] });
        expect(updateCountersStub.callCount).to.equal(2);
        expect(
            updateCountersStub.getCalls().map(({ args }) => [
                args[2], args[3], args[4].counters.totalAccepted
            ])
        ).to.have.deep.members([
            ["sender-1", "campaign-1", 1],
            ["sender-1", "campaign-2", 1]
        ]);
    });

    it("retries only the failed sender aggregate and releases only its locks", async () => {
        updateCountersStub.callsFake(async (
            client, table, senderId
        ) => {
            if (senderId === "sender-1") {
                throw new Error("dynamo failure");
            }
        });

        const result = await handleEvent({
            Records: [
                createRecord("sender-1", "campaign-1", "1"),
                createRecord("sender-2", "campaign-1", "2"),
                createRecord("sender-1", "campaign-1", "3")
            ]
        });

        expect(updateCountersStub.callCount).to.equal(2);
        expect(result).to.deep.equal({
            batchItemFailures: [
                { itemIdentifier: "1" },
                { itemIdentifier: "3" }
            ]
        });
        expect(removeDeduplicationLocksStub.calledOnce).to.be.true;
        expect(removeDeduplicationLocksStub.firstCall.args[1])
            .to.equal("dedup-table");
        expect(removeDeduplicationLocksStub.firstCall.args[2])
            .to.deep.equal(["timeline-1", "timeline-3"]);
    });

    it("retries only the remaining sender when campaign updates time out", async () => {
        const getRemainingTimeInMillis = sinon.stub().returns(999999);

        updateCountersStub.onFirstCall().callsFake(async () => {
            getRemainingTimeInMillis.returns(1000);
        });

        const result = await handleEvent({
            Records: [
                createRecord("sender-1", "campaign-1", "1"),
                createRecord("sender-2", "campaign-1", "2")
            ]
        }, { getRemainingTimeInMillis });

        expect(updateCountersStub.calledOnce).to.be.true;
        expect(updateCountersStub.firstCall.args[2]).to.equal("sender-1");
        expect(result).to.deep.equal({
            batchItemFailures: [{ itemIdentifier: "2" }]
        });
        expect(removeDeduplicationLocksStub.calledOnce).to.be.true;
        expect(removeDeduplicationLocksStub.firstCall.args[2])
            .to.deep.equal(["timeline-2"]);
    });

    [
        ["missing", undefined],
        ["empty", { S: "" }],
        ["blank", { S: "   " }],
        ["non-string", { N: "123" }]
    ].forEach(([label, paId]) => {
        it(`skips a record with ${label} paId before acquiring a lock`, async () => {
            const record = createRecord("sender-1", "campaign-1", "1");
            const payload = JSON.parse(
                Buffer.from(record.kinesis.data, "base64").toString("utf8")
            );

            if (paId === undefined) {
                delete payload.dynamodb.NewImage.paId;
            } else {
                payload.dynamodb.NewImage.paId = paId;
            }

            record.kinesis.data = encode(payload);

            const result = await handleEvent({ Records: [record] });

            expect(result).to.deep.equal({ batchItemFailures: [] });
            expect(consoleErrorStub.calledOnce).to.be.true;
            expect(acquireDeduplicationLockStub.called).to.be.false;
            expect(updateCountersStub.called).to.be.false;
            expect(removeDeduplicationLocksStub.called).to.be.false;
        });
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
        expect(updateCountersStub.firstCall.args[2])
            .to.equal("5b994d4a-0fa8-47ac-9c7b-354f1d44a1ce");
        expect(updateCountersStub.firstCall.args[3]).to.equal("FattOrd");
        expect(updateCountersStub.firstCall.args[4].counters.totalAccepted).to.equal(1);
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
                                paId: { S: "sender-1" },
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
