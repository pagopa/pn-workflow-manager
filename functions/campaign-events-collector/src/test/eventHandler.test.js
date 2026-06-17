const { expect } = require("chai");
const sinon = require("sinon");
const proxyquire = require("proxyquire").noCallThru();

const base64Json = (payload) => Buffer.from(JSON.stringify(payload)).toString("base64");

const makeRecord = ({
    eventID = "event-1",
    eventName = "INSERT",
    parsedData,
    payload = {}
}) => ({
    eventID,
    eventName,
    kinesis: {
        data: base64Json(payload)
    },
    dynamodb: parsedData ? { NewImage: { __mockParsedData: parsedData } } : undefined
});

describe("eventHandler", () => {
    let handleEvent;
    let updateCountersStub;
    let extractCampaignIdStub;
    let unmarshallStub;
    let docClientMock;
    let fromStub;
    let dynamoDbClientStub;
    let consoleLogStub;
    let consoleWarnStub;
    let consoleErrorStub;

    beforeEach(() => {
        process.env.CAMPAIGN_STATISTICS_TABLE = "statistics-table";
        process.env.REGION = "eu-west-1";

        updateCountersStub = sinon.stub().resolves();
        extractCampaignIdStub = sinon.stub();
        unmarshallStub = sinon.stub().callsFake((newImage) => newImage.__mockParsedData);
        docClientMock = { send: sinon.stub().resolves() };
        fromStub = sinon.stub().returns(docClientMock);
        dynamoDbClientStub = sinon.stub().callsFake(function DynamoDBClient() {});

        consoleLogStub = sinon.stub(console, "log");
        consoleWarnStub = sinon.stub(console, "warn");
        consoleErrorStub = sinon.stub(console, "error");

        ({ handleEvent } = proxyquire("../app/eventHandler", {
            "@aws-sdk/util-dynamodb": { unmarshall: unmarshallStub },
            "./lib/campaignUtils": { extractCampaignId: extractCampaignIdStub },
            "./lib/dbOperations": { updateCounters: updateCountersStub },
            "@aws-sdk/client-dynamodb": { DynamoDBClient: dynamoDbClientStub },
            "@aws-sdk/lib-dynamodb": { DynamoDBDocumentClient: { from: fromStub } }
        }));
    });

    afterEach(() => {
        sinon.restore();
        delete process.env.CAMPAIGN_STATISTICS_TABLE;
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

        expect(updateCountersStub.firstCall.args[0]).to.equal(docClientMock);
        expect(updateCountersStub.firstCall.args[1]).to.equal("statistics-table");
        expect(updateCountersStub.firstCall.args[2]).to.equal("campaign-1");
        expect(updateCountersStub.firstCall.args[3]).to.deep.equal({
            counters: {
                totalSent: 1,
                digitalSent_EMAIL: 1,
                digitalSent_SMS: 1
            },
            lastTimestamp: "2026-01-01T12:00:00.000Z"
        });

        expect(updateCountersStub.secondCall.args[0]).to.equal(docClientMock);
        expect(updateCountersStub.secondCall.args[1]).to.equal("statistics-table");
        expect(updateCountersStub.secondCall.args[2]).to.equal("campaign-2");
        expect(updateCountersStub.secondCall.args[3]).to.deep.equal({
            counters: {
                payed: 1
            },
            lastTimestamp: "2026-01-01T09:00:00.000Z"
        });

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
                    eventName: "INSERT",
                    kinesis: {
                        data: base64Json({})
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
        expect(updateCountersStub.firstCall.args[3]).to.deep.equal({
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
        expect(consoleErrorStub.calledOnce).to.be.true;
        expect(consoleErrorStub.firstCall.args[0]).to.match(/Parsing error on record/);
    });
});

