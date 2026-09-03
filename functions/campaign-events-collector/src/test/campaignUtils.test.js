const { expect } = require("chai");
const sinon = require("sinon");
const proxyquire = require("proxyquire").noCallThru().noPreserveCache();

describe("campaignUtils", () => {
    let consoleWarnStub;
    let consoleErrorStub;

    beforeEach(() => {
        consoleWarnStub = sinon.stub(console, "warn");
        consoleErrorStub = sinon.stub(console, "error");
    });

    afterEach(() => {
        sinon.restore();
        delete process.env.BASE_PATH;
    });

    it("extracts campaignId from delivery-private using the iun", async () => {
        const axiosStub = {
            get: sinon.stub().resolves({
                data: { campaignId: "campaign-456" }
            })
        };

        process.env.BASE_PATH = "http://localhost:8080";

        const utils = proxyquire("../app/lib/campaignUtils", {
            axios: axiosStub
        });

        const result = await utils.extractCampaignId({ iun: "abc-123" });

        expect(result).to.equal("campaign-456");
        expect(axiosStub.get.calledOnce).to.be.true;
        expect(axiosStub.get.firstCall.args[0]).to.equal("http://localhost:8080/delivery-private/notifications/abc-123");
    });

    it("returns null and warns when item has no iun", async () => {
        const axiosStub = {
            get: sinon.stub()
        };

        const utils = proxyquire("../app/lib/campaignUtils", {
            axios: axiosStub
        });

        const result = await utils.extractCampaignId({ timelineElementId: "timeline-1" });

        expect(result).to.be.null;
        expect(axiosStub.get.called).to.be.false;
        expect(consoleWarnStub.calledOnce).to.be.true;
    });

    it("returns null when delivery-private fails", async () => {
        const axiosStub = {
            get: sinon.stub().rejects({
                response: { status: 404 }
            })
        };

        const utils = proxyquire("../app/lib/campaignUtils", {
            axios: axiosStub
        });

        const result = await utils.extractCampaignId({ iun: "abc-123" });

        expect(result).to.be.null;
        expect(consoleErrorStub.called).to.be.true;
    });
});
