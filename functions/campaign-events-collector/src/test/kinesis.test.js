const { expect } = require("chai");
const kinesis = require("../app/lib/kinesis");

describe("kinesis", () => {
    it("decodes the example event json and keeps INFORMAL records", () => {
        const event = {
            Records: require("./kinesis.event.example.json").Records.map((record) => ({
                ...record,
                kinesis: {
                    ...record.kinesis,
                    data: Buffer.from(JSON.stringify(record.kinesis.data)).toString("base64")
                }
            }))
        };
        const result = kinesis.extractKinesisData(event);

        expect(result).to.have.lengthOf(1);
        expect(result[0].eventName).to.equal("INSERT");
        expect(result[0].kinesisSeqNumber).to.equal("49651151769547260642845689623061309776858789665900068930");
        expect(result[0].dynamodb.NewImage.category.S).to.equal("REQUEST_ACCEPTED");
        expect(result[0].dynamodb.NewImage.communicationType.S).to.equal("INFORMAL");
        expect(result[0].dynamodb.NewImage.timelineElementId.S).to.equal("REQUEST_ACCEPTED.IUN_XHRX-MYNM-YVHA-202609-D-A");
    });

    it("filters out records without communicationType", () => {
        const event = {
            Records: [{
                kinesis: {
                    sequenceNumber: "1",
                    data: Buffer.from(JSON.stringify({
                        eventName: "INSERT",
                        dynamodb: { NewImage: { category: { S: "TEST" } } }
                    })).toString("base64")
                }
            }]
        };

        const result = kinesis.extractKinesisData(event);

        expect(result).to.have.lengthOf(0);
    });

    it("throws when payload is neither json nor gzip", () => {
        const event = {
            Records: [{
                kinesis: {
                    sequenceNumber: "1",
                    data: Buffer.from("not-json").toString("base64")
                }
            }]
        };

        expect(() => kinesis.extractKinesisData(event)).to.throw();
    });
});