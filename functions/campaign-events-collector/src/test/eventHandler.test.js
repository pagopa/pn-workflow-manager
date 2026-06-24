const { expect } = require("chai");
const sinon = require("sinon");
const proxyquire = require("proxyquire").noCallThru();

const base64Json = (payload) => Buffer.from(JSON.stringify(payload)).toString("base64");

/**
 * Costruisce un record Kinesis nel formato reale usato da AWS (kinesis.data contiene il payload
 * DynamoDB CDC serializzato in base64), coerente con kinesis.event.example.json.
 */
const makeRecord = ({
    eventID = "event-1",
    eventName = "INSERT",
    parsedData
}) => ({
    eventID,
    eventName: "aws:kinesis:record",
    kinesis: {
        data: base64Json({
            eventName,
            dynamodb: parsedData ? { NewImage: { __mockParsedData: parsedData } } : undefined
        })
    }
});

describe("eventHandler", () => {
    let handleEvent;
    let acquireDeduplicationLockStub;
    let removeDeduplicationLocksStub;
    let updateCountersStub;
    let extractCampaignIdStub;
    let unmarshallStub;
    let docClientMock;
    let dynamoDbClientStub;
    let consoleLogStub;
    let consoleWarnStub;
    let consoleErrorStub;

    beforeEach(() => {
        process.env.CAMPAIGN_STATISTICS_TABLE = "statistics-table";
        process.env.CAMPAIGN_EVENTS_DEDUPLICATION_TABLE = "dedup-table";
        process.env.CAMPAIGN_EVENTS_DEDUPLICATION_TTL_DAYS = "7";
        process.env.REGION = "eu-west-1";

        acquireDeduplicationLockStub = sinon.stub().resolves();
        removeDeduplicationLocksStub = sinon.stub().resolves();
        updateCountersStub = sinon.stub().resolves();
        extractCampaignIdStub = sinon.stub();
        unmarshallStub = sinon.stub().callsFake((newImage) => newImage.__mockParsedData);
        docClientMock = { send: sinon.stub().resolves() };
        dynamoDbClientStub = sinon.stub().returns(docClientMock);

        consoleLogStub = sinon.stub(console, "log");
        consoleWarnStub = sinon.stub(console, "warn");
        consoleErrorStub = sinon.stub(console, "error");

        ({ handleEvent } = proxyquire("../app/eventHandler", {
            "@aws-sdk/util-dynamodb": { unmarshall: unmarshallStub },
            "./lib/campaignUtils": { extractCampaignId: extractCampaignIdStub },
            "./lib/dbOperations": {
                acquireDeduplicationLock: acquireDeduplicationLockStub,
                removeDeduplicationLocks: removeDeduplicationLocksStub,
                updateCounters: updateCountersStub
            },
            "@aws-sdk/client-dynamodb": { DynamoDBClient: dynamoDbClientStub }
        }));
    });

    afterEach(() => {
        sinon.restore();
        delete process.env.CAMPAIGN_STATISTICS_TABLE;
        delete process.env.CAMPAIGN_EVENTS_DEDUPLICATION_TABLE;
        delete process.env.CAMPAIGN_EVENTS_DEDUPLICATION_TTL_DAYS;
        delete process.env.REGION;
    });

    it("aggregates counters per campaign and updates DynamoDB once per campaign", async () => {
        extractCampaignIdStub.resolves("campaign-2");

        const event = {
            Records: [
                makeRecord({
                    eventID: "record-1",
                    parsedData: {
                        timelineElementId: "timeline-1",
                        category: "REQUEST_ACCEPTED",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-1",
                        timestamp: "2026-01-01T10:00:00.000Z"
                    }
                }),
                makeRecord({
                    eventID: "record-2",
                    parsedData: {
                        timelineElementId: "timeline-2",
                        category: "SEND_DIGITAL_MESSAGE",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-1",
                        timestamp: "2026-01-01T12:00:00.000Z",
                        details: { channel: "EMAIL" }
                    }
                }),
                makeRecord({
                    eventID: "record-3",
                    parsedData: {
                        timelineElementId: "REACHED_IUN123_SMS",
                        category: "REACHED",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-1",
                        timestamp: "2026-01-01T11:00:00.000Z"
                    }
                }),
                makeRecord({
                    eventID: "record-4",
                    parsedData: {
                        timelineElementId: "timeline-4",
                        category: "PAYMENT",
                        communicationType: "INFORMAL",
                        iun: "IUN-123",
                        timestamp: "2026-01-01T09:00:00.000Z"
                    }
                }),
                makeRecord({
                    eventID: "record-5",
                    parsedData: {
                        timelineElementId: "timeline-5",
                        category: "REQUEST_REFUSED",
                        communicationType: "FORMAL",
                        campaignId: "campaign-ignored",
                        timestamp: "2026-01-01T13:00:00.000Z"
                    }
                })
            ]
        };

        const result = await handleEvent(event);

        expect(result).to.deep.equal({ status: "SUCCESS", processedCampaigns: 2 });
        expect(extractCampaignIdStub.calledOnce).to.be.true;
        expect(extractCampaignIdStub.firstCall.args[0]).to.deep.equal({
            timelineElementId: "timeline-4",
            category: "PAYMENT",
            communicationType: "INFORMAL",
            iun: "IUN-123",
            timestamp: "2026-01-01T09:00:00.000Z"
        });

        expect(updateCountersStub.calledTwice).to.be.true;
        expect(acquireDeduplicationLockStub.callCount).to.equal(4);

        expect(updateCountersStub.firstCall.args[0]).to.equal(docClientMock);
        expect(updateCountersStub.firstCall.args[1]).to.equal("statistics-table");
        expect(updateCountersStub.firstCall.args[2]).to.equal("campaign-1");
        expect(updateCountersStub.firstCall.args[3]).to.deep.include({
            counters: {
                totalSent: 1,
                digitalSent_EMAIL: 1,
                digitalSent_SMS: 1
            },
            lastTimestamp: "2026-01-01T12:00:00.000Z"
        });
        expect(updateCountersStub.firstCall.args[3].timelineElementIds).to.have.members([
            "timeline-1",
            "timeline-2",
            "REACHED_IUN123_SMS"
        ]);

        expect(updateCountersStub.secondCall.args[0]).to.equal(docClientMock);
        expect(updateCountersStub.secondCall.args[1]).to.equal("statistics-table");
        expect(updateCountersStub.secondCall.args[2]).to.equal("campaign-2");
        expect(updateCountersStub.secondCall.args[3]).to.deep.include({
            counters: {
                payed: 1
            },
            lastTimestamp: "2026-01-01T09:00:00.000Z"
        });
        expect(updateCountersStub.secondCall.args[3].timelineElementIds).to.have.members(["timeline-4"]);

        expect(consoleErrorStub.called).to.be.false;
    });

    it("skips non-INSERT records, records without NewImage and non-INFORMAL communications", async () => {
        const event = {
            Records: [
                makeRecord({
                    eventID: "record-1",
                    eventName: "MODIFY",
                    parsedData: {
                        timelineElementId: "timeline-1",
                        category: "REQUEST_ACCEPTED",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-1",
                        timestamp: "2026-01-01T10:00:00.000Z"
                    }
                }),
                {
                    eventID: "record-2",
                    eventName: "aws:kinesis:record",
                    kinesis: {
                        // kinesis.data contiene un INSERT senza dynamodb → deve essere scartato
                        data: base64Json({ eventName: "INSERT" })
                    }
                },
                makeRecord({
                    eventID: "record-3",
                    parsedData: {
                        timelineElementId: "timeline-3",
                        category: "REQUEST_ACCEPTED",
                        communicationType: "FORMAL",
                        campaignId: "campaign-2",
                        timestamp: "2026-01-01T11:00:00.000Z"
                    }
                })
            ]
        };

        const result = await handleEvent(event);

        expect(result).to.deep.equal({ status: "SUCCESS", processedCampaigns: 0 });
        expect(unmarshallStub.calledOnce).to.be.true;
        expect(updateCountersStub.called).to.be.false;
        expect(extractCampaignIdStub.called).to.be.false;
        expect(acquireDeduplicationLockStub.called).to.be.false;
    });

    it("returns success and skips the record when campaignId extraction fails", async () => {
        extractCampaignIdStub.resolves(null);

        const event = {
            Records: [
                makeRecord({
                    eventID: "record-1",
                    parsedData: {
                        timelineElementId: "timeline-1",
                        category: "PAYMENT",
                        communicationType: "INFORMAL",
                        iun: "IUN-404",
                        timestamp: "2026-01-01T10:00:00.000Z"
                    }
                })
            ]
        };

        const result = await handleEvent(event);

        expect(result).to.deep.equal({ status: "SUCCESS", processedCampaigns: 0 });
        expect(extractCampaignIdStub.calledOnce).to.be.true;
        expect(updateCountersStub.called).to.be.false;
        expect(acquireDeduplicationLockStub.called).to.be.false;
        expect(consoleWarnStub.calledOnce).to.be.true;
        expect(consoleWarnStub.firstCall.args[0]).to.match(/Missing campaignId/);
    });

    it("covers the remaining category branches, warnings and default handler", async () => {
        const event = {
            Records: [
                makeRecord({
                    eventID: "record-1",
                    parsedData: {
                        timelineElementId: "timeline-1",
                        category: "REQUEST_REFUSED",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-extra",
                        timestamp: "2026-01-01T08:00:00.000Z"
                    }
                }),
                makeRecord({
                    eventID: "record-2",
                    parsedData: {
                        timelineElementId: "timeline-2",
                        category: "WORKFLOW_ENDED_UNDELIVERABLE",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-extra",
                        timestamp: "2026-01-01T09:00:00.000Z"
                    }
                }),
                makeRecord({
                    eventID: "record-3",
                    parsedData: {
                        timelineElementId: "timeline-3",
                        category: "WORKFLOW_DONE",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-extra",
                        timestamp: "2026-01-01T10:00:00.000Z"
                    }
                }),
                makeRecord({
                    eventID: "record-4",
                    parsedData: {
                        timelineElementId: "timeline-4",
                        category: "WORKFLOW_ENDED_REACHED",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-extra",
                        timestamp: "2026-01-01T11:00:00.000Z"
                    }
                }),
                makeRecord({
                    eventID: "record-5",
                    parsedData: {
                        timelineElementId: "timeline-5",
                        category: "WORKFLOW_ENDED_UNREACHED",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-extra",
                        timestamp: "2026-01-01T12:00:00.000Z"
                    }
                }),
                makeRecord({
                    eventID: "record-6",
                    parsedData: {
                        timelineElementId: "timeline-6",
                        category: "NOTIFICATION_VIEWED",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-extra",
                        timestamp: "2026-01-01T13:00:00.000Z"
                    }
                }),
                makeRecord({
                    eventID: "record-7",
                    parsedData: {
                        timelineElementId: "timeline-7",
                        category: "SEND_COURTESY_MESSAGE",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-extra",
                        timestamp: "2026-01-01T14:00:00.000Z",
                        details: { channel: "PEC" }
                    }
                }),
                makeRecord({
                    eventID: "record-8",
                    parsedData: {
                        timelineElementId: "timeline-8",
                        category: "SEND_ANALOG_MESSAGE",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-extra",
                        timestamp: "2026-01-01T15:00:00.000Z"
                    }
                }),
                makeRecord({
                    eventID: "record-9",
                    parsedData: {
                        timelineElementId: "REACHED_IUN123_RS",
                        category: "REACHED",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-extra",
                        timestamp: "2026-01-01T16:00:00.000Z"
                    }
                }),
                makeRecord({
                    eventID: "record-10",
                    parsedData: {
                        timelineElementId: "timeline-10",
                        category: "SEND_DIGITAL_MESSAGE",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-extra",
                        timestamp: "2026-01-01T17:00:00.000Z",
                        details: { channel: "ANALOG" }
                    }
                }),
                makeRecord({
                    eventID: "record-11",
                    parsedData: {
                        timelineElementId: "REACHED_ONLY",
                        category: "REACHED",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-extra",
                        timestamp: "2026-01-01T18:00:00.000Z"
                    }
                }),
                makeRecord({
                    eventID: "record-12",
                    parsedData: {
                        timelineElementId: "timeline-12",
                        category: "UNKNOWN_CATEGORY",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-extra",
                        timestamp: "2026-01-01T19:00:00.000Z"
                    }
                })
            ]
        };

        const result = await handleEvent(event);

        expect(result).to.deep.equal({ status: "SUCCESS", processedCampaigns: 1 });
        expect(updateCountersStub.calledOnce).to.be.true;
        expect(updateCountersStub.firstCall.args[2]).to.equal("campaign-extra");
        expect(updateCountersStub.firstCall.args[3]).to.deep.include({
            counters: {
                totalRefused: 1,
                totalUndeliverable: 1,
                workflowDone: 1,
                totalReached: 1,
                totalUnreached: 1,
                viewed: 1,
                digitalSent_received_PEC: 1,
                analogSent_RS: 1,
                digitalSent_RS: 1
            },
            lastTimestamp: "2026-01-01T19:00:00.000Z"
        });
        expect(updateCountersStub.firstCall.args[3].timelineElementIds).to.have.members([
            "timeline-1",
            "timeline-2",
            "timeline-3",
            "timeline-4",
            "timeline-5",
            "timeline-6",
            "timeline-7",
            "timeline-8",
            "REACHED_IUN123_RS",
            "REACHED_ONLY",
            "timeline-12"
        ]);

        expect(consoleWarnStub.calledTwice).to.be.true;
        expect(consoleWarnStub.firstCall.args[0]).to.match(/Unexpected channel for SEND_DIGITAL_MESSAGE: ANALOG/);
        expect(consoleWarnStub.secondCall.args[0]).to.match(/Unexpected timelineElementId format: REACHED_ONLY/);
        expect(consoleLogStub.calledWithMatch(/Unhandled category: UNKNOWN_CATEGORY/)).to.be.true;
    });

    it("logs and skips records that fail parsing", async () => {
        const event = {
            Records: [
                {
                    eventID: "record-bad-json",
                    eventName: "INSERT",
                    kinesis: {
                        data: Buffer.from("not-json").toString("base64")
                    }
                }
            ]
        };

        const result = await handleEvent(event);

        expect(result).to.deep.equal({ status: "SUCCESS", processedCampaigns: 0 });
        expect(updateCountersStub.called).to.be.false;
        expect(acquireDeduplicationLockStub.called).to.be.false;
        expect(consoleErrorStub.calledOnce).to.be.true;
        expect(consoleErrorStub.firstCall.args[0]).to.match(/Parsing error on record/);
    });

    it("skips duplicate events when deduplication conditional put fails", async () => {
        const duplicateError = new Error("duplicate");
        duplicateError.name = "ConditionalCheckFailedException";
        acquireDeduplicationLockStub.rejects(duplicateError);

        const event = {
            Records: [
                makeRecord({
                    eventID: "record-1",
                    parsedData: {
                        timelineElementId: "timeline-1",
                        category: "REQUEST_ACCEPTED",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-1",
                        timestamp: "2026-01-01T10:00:00.000Z"
                    }
                })
            ]
        };

        const result = await handleEvent(event);

        expect(result).to.deep.equal({ status: "SUCCESS", processedCampaigns: 0 });
        expect(updateCountersStub.called).to.be.false;
        expect(removeDeduplicationLocksStub.called).to.be.false;
    });

    it("compensates deduplication locks when a campaign update fails", async () => {
        updateCountersStub.callsFake(async (client, table, campaignId) => {
            if (campaignId === "campaign-fail") {
                throw new Error("update failed");
            }
        });

        const event = {
            Records: [
                makeRecord({
                    eventID: "record-1",
                    parsedData: {
                        timelineElementId: "timeline-ok",
                        category: "REQUEST_ACCEPTED",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-ok",
                        timestamp: "2026-01-01T10:00:00.000Z"
                    }
                }),
                makeRecord({
                    eventID: "record-2",
                    parsedData: {
                        timelineElementId: "timeline-fail",
                        category: "REQUEST_ACCEPTED",
                        communicationType: "INFORMAL",
                        campaignId: "campaign-fail",
                        timestamp: "2026-01-01T11:00:00.000Z"
                    }
                })
            ]
        };

        let thrownError;
        try {
            await handleEvent(event);
        } catch (error) {
            thrownError = error;
        }

        expect(thrownError.message).to.equal("update failed");
        expect(removeDeduplicationLocksStub.calledOnce).to.be.true;
        expect(removeDeduplicationLocksStub.firstCall.args[1]).to.equal("dedup-table");
        expect(removeDeduplicationLocksStub.firstCall.args[2]).to.deep.equal(["timeline-fail"]);
    });

    describe("integration: kinesis.event.example.json", () => {
        let handleEventReal;

        beforeEach(() => {
            // Usa il vero @aws-sdk/util-dynamodb (unmarshall reale) per testare con dati DynamoDB autentici.
            // Si stubbano solo le dipendenze esterne (DB, HTTP).
            handleEventReal = proxyquire("../app/eventHandler", {
                "./lib/campaignUtils": { extractCampaignId: extractCampaignIdStub },
                "./lib/dbOperations": {
                    acquireDeduplicationLock: acquireDeduplicationLockStub,
                    removeDeduplicationLocks: removeDeduplicationLocksStub,
                    updateCounters: updateCountersStub
                },
                "@aws-sdk/client-dynamodb": { DynamoDBClient: dynamoDbClientStub }
            }).handleEvent;
        });

        it("skips a real Kinesis record without communicationType INFORMAL (ANALOG_SUCCESS_WORKFLOW)", async () => {
            const exampleJson = require("./kinesis.event.example.json");

            // Nel file di esempio kinesis.data è un oggetto JSON per leggibilità;
            // in produzione è una stringa base64 → la codifichiamo qui come farebbe AWS.
            const event = {
                Records: exampleJson.Records.map((rec) => ({
                    ...rec,
                    kinesis: {
                        ...rec.kinesis,
                        data: base64Json(rec.kinesis.data)
                    }
                }))
            };

            const result = await handleEventReal(event);

            // Il record ha category "ANALOG_SUCCESS_WORKFLOW" e nessun communicationType INFORMAL
            // → deve essere scartato senza toccare DynamoDB
            expect(result).to.deep.equal({ status: "SUCCESS", processedCampaigns: 0 });
            expect(updateCountersStub.called).to.be.false;
            expect(extractCampaignIdStub.called).to.be.false;
        });

        it("processes a real Kinesis record with communicationType INFORMAL and campaignId", async () => {
            const exampleJson = require("./kinesis.event.example.json");

            // Cloniamo il NewImage del record di esempio aggiungendo i campi necessari
            // per superare il filtro INFORMAL e attivare il contatore REQUEST_ACCEPTED.
            const enrichedData = {
                ...exampleJson.Records[0].kinesis.data,
                dynamodb: {
                    NewImage: {
                        ...exampleJson.Records[0].kinesis.data.dynamodb.NewImage,
                        communicationType: { S: "INFORMAL" },
                        campaignId: { S: "campaign-real-001" },
                        category: { S: "REQUEST_ACCEPTED" },
                        timestamp: { S: "2025-01-31T16:15:39.713731463Z" }
                    }
                }
            };

            const event = {
                Records: [{
                    ...exampleJson.Records[0],
                    kinesis: {
                        ...exampleJson.Records[0].kinesis,
                        data: base64Json(enrichedData)
                    }
                }]
            };

            const result = await handleEventReal(event);

            expect(result).to.deep.equal({ status: "SUCCESS", processedCampaigns: 1 });
            expect(updateCountersStub.calledOnce).to.be.true;
            expect(updateCountersStub.firstCall.args[2]).to.equal("campaign-real-001");
            expect(updateCountersStub.firstCall.args[3].counters).to.deep.equal({ totalSent: 1 });
        });
    });
});
