const { expect } = require("chai");
const sinon = require("sinon");
const metrics = require("../app/lib/timelineMetrics");

describe("timelineMetrics", () => {
    let consoleWarnStub;
    let consoleLogStub;

    beforeEach(() => {
        consoleWarnStub = sinon.stub(console, "warn");
        consoleLogStub = sinon.stub(console, "log");
    });

    afterEach(() => {
        sinon.restore();
    });

    it("handles REQUEST_ACCEPTED", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "REQUEST_ACCEPTED", {});
        expect(ok).to.be.true;
        expect(counters.totalAccepted).to.equal(1);
    });

    it("handles REQUEST_REFUSED", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "REQUEST_REFUSED", {});
        expect(ok).to.be.true;
        expect(counters.totalRefused).to.equal(1);
    });

    it("handles WORKFLOW_ENDED_UNDELIVERABLE", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "WORKFLOW_ENDED_UNDELIVERABLE", {});
        expect(ok).to.be.true;
        expect(counters.totalUndeliverable).to.equal(1);
    });

    it("handles WORKFLOW_DONE_REACHED without statusChanged", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "WORKFLOW_DONE_REACHED", {});
        expect(ok).to.be.true;
        expect(counters.workflowDone).to.equal(1);
        expect(counters.totalDelivered).to.be.undefined;
    });

    it("handles WORKFLOW_DONE_REACHED with statusChanged true", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "WORKFLOW_DONE_REACHED", {
            statusInfo: { statusChanged: true }
        });
        expect(ok).to.be.true;
        expect(counters.workflowDone).to.equal(1);
        expect(counters.totalDelivered).to.equal(1);
    });

    it("handles WORKFLOW_DONE_UNREACHED", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "WORKFLOW_DONE_UNREACHED", {});
        expect(ok).to.be.true;
        expect(counters.workflowDone).to.equal(1);
    });

    it("handles WORKFLOW_ENDED_REACHED", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "WORKFLOW_ENDED_REACHED", {});
        expect(ok).to.be.true;
        expect(counters.totalDelivered).to.equal(1);
    });

    it("handles INFORMAL_NOTIFICATION_VIEWED with valid platform", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "INFORMAL_NOTIFICATION_VIEWED", {
            details: { channel: "IO" }
        });
        expect(ok).to.be.true;
        expect(counters.viewedIO).to.equal(1);
    });

    it("rejects INFORMAL_NOTIFICATION_VIEWED with invalid platform", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "INFORMAL_NOTIFICATION_VIEWED", {
            details: { channel: "INVALID" }
        });
        expect(ok).to.be.false;
        expect(counters.viewedINVALID).to.be.undefined;
        expect(consoleWarnStub.calledOnce).to.be.true;
    });

    it("handles SEND_DIGITAL_MESSAGE and totalSent only for PROCESSING", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "SEND_DIGITAL_MESSAGE", {
            details: { channel: "EMAIL" },
            statusInfo: { statusChanged: true, actual: "PROCESSING" }
        });

        expect(ok).to.be.true;
        expect(counters.digitalSentEMAIL).to.equal(1);
        expect(counters.totalSent).to.equal(1);
    });

    it("handles SEND_DIGITAL_MESSAGE without totalSent when status is not PROCESSING", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "SEND_DIGITAL_MESSAGE", {
            details: { channel: "SMS" },
            statusInfo: { statusChanged: true, actual: "DELIVERED" }
        });

        expect(ok).to.be.true;
        expect(counters.digitalSentSMS).to.equal(1);
        expect(counters.totalSent).to.be.undefined;
    });

    it("rejects SEND_DIGITAL_MESSAGE with invalid channel", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "SEND_DIGITAL_MESSAGE", {
            details: { channel: "INVALID" }
        });

        expect(ok).to.be.false;
        expect(consoleWarnStub.calledOnce).to.be.true;
    });

    it("handles SEND_ANALOG_MESSAGE with PROCESSING status", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "SEND_ANALOG_MESSAGE", {
            statusInfo: { statusChanged: true, actual: "PROCESSING" },
            details: { channel: "RS" }
        });

        expect(ok).to.be.true;
        expect(counters.analogSentRS).to.equal(1);
        expect(counters.totalSent).to.equal(1);
    });

    it("handles SEND_ANALOG_MESSAGE without totalSent when status is not PROCESSING", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "SEND_ANALOG_MESSAGE", {
            statusInfo: { statusChanged: false, actual: "PROCESSING" },
            details: { channel: "RS" }
        });

        expect(ok).to.be.true;
        expect(counters.analogSentRS).to.equal(1);
        expect(counters.totalSent).to.be.undefined;
    });

    it("skips SEND_ANALOG_MESSAGE with invalid channel", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "SEND_ANALOG_MESSAGE", {
            statusInfo: { statusChanged: true, actual: "PROCESSING" },
            details: { channel: "INVALID" }
        });

        expect(ok).to.be.false;
        expect(counters.analogSentRS).to.be.undefined;
        expect(counters.totalSent).to.be.undefined;
    });

    it("handles DELIVERED by channel", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "DELIVERED", {
            details: { channel: "RS" }
        });

        expect(ok).to.be.true;
        expect(counters.receivedRS).to.equal(1);
    });

    it("rejects DELIVERED without a valid channel", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "DELIVERED", {});

        expect(ok).to.be.false;
        expect(consoleWarnStub.calledOnce).to.be.true;
    });

    it("handles PAYMENT", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "PAYMENT", {});

        expect(ok).to.be.true;
        expect(counters.paid).to.equal(1);
    });

    it("logs unhandled categories and returns true", () => {
        const counters = {};
        const ok = metrics.applyCategoryMetric(counters, "UNKNOWN", {});

        expect(ok).to.be.true;
        expect(consoleLogStub.calledOnce).to.be.true;
    });
});