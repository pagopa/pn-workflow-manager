const { expect } = require("chai");
const sinon = require("sinon");
const proxyquire = require("proxyquire");

describe("campaignUtils", () => {
    it("should use BASE_PATH from environment if set", async () => {
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
        expect(axiosStub.get.firstCall.args[0]).to.equal(`${process.env.BASE_PATH}/delivery-private/notifications/abc-123`);
        delete process.env.BASE_PATH; // Clean up environment variable
    });

    it("should return campaignId from iun", async () => {
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
            expect(axiosStub.get.firstCall.args[0]).to.equal(`${process.env.BASE_PATH}/delivery-private/notifications/abc-123`);
            delete process.env.BASE_PATH; // Clean up environment variable
        });

        it("should return null for missing iun", async () => {
                const axiosStub = {
                    get: sinon.stub().rejects({
                      response: {
                        status: 400,
                        data: { message: "Bad Request" }
                      }
                    })
                };

                process.env.BASE_PATH = "http://localhost:8080";

                const utils = proxyquire("../app/lib/campaignUtils", {
                    axios: axiosStub
                });

                const result = await utils.extractCampaignId({ iun: "abc-123" });

                expect(result).to.be.null;
                delete process.env.BASE_PATH; // Clean up environment variable
            });
});