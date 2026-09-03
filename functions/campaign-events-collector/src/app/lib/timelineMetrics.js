const DIGITAL_CHANNELS = ["IO", "EMAIL", "PEC", "SMS"];
const ANALOG_CHANNELS = ["RS"];
const PLATFORM_CHANNELS = ["IO", "SEND"];
const DELIVERED_CHANNELS = ["IO", "EMAIL", "PEC", "SMS", "RS"];

/**
 * Verifica se l'evento ha comportato un cambio di stato verso lo stato atteso,
 * basandosi sul campo statusInfo.statusChanged/actual dell'elemento di timeline.
 */
function isStatusChangedTo(parsedData, expectedStatus) {
    return hasStatusChanged(parsedData) &&
        parsedData?.statusInfo?.actual === expectedStatus;
}

function hasStatusChanged(parsedData) {
    return parsedData?.statusInfo?.statusChanged === true;
}

/**
 * Applica, sull'oggetto counters di una campagna, l'incremento corrispondente
 * alla categoria dell'elemento di timeline processato.
 *
 * @param {Object} counters - l'oggetto counters dell'aggregato di campagna
 * @param {string} category - parsedData.category dell'elemento di timeline
 * @param {Object} parsedData - l'elemento di timeline già unmarshall-ato
 * @returns {boolean} true se l'evento è stato applicato correttamente e va considerato
 *                     "processato" (il chiamante può aggiungerlo a timelineElementIds/sequenceNumbers);
 *                     false se l'evento va scartato (es. canale mancante o non atteso) —
 *                     il chiamante decide se fare `continue`.
 */
function applyCategoryMetric(counters, category, parsedData) {
    let channel;
    switch (category) {
        case "REQUEST_ACCEPTED":
            counters.totalAccepted = (counters.totalAccepted || 0) + 1;
            break;

        case "REQUEST_REFUSED":
            counters.totalRefused = (counters.totalRefused || 0) + 1;
            break;

        case "WORKFLOW_ENDED_UNDELIVERABLE":
            counters.totalUndeliverable = (counters.totalUndeliverable || 0) + 1;
            break;

        case "WORKFLOW_DONE_REACHED":
            counters.workflowDone = (counters.workflowDone || 0) + 1;
            if (hasStatusChanged(parsedData)) {
                counters.totalDelivered = (counters.totalDelivered || 0) + 1;
            }
            break;

        case "WORKFLOW_DONE_UNREACHED":
            counters.workflowDone = (counters.workflowDone || 0) + 1;
            break;

        case "WORKFLOW_ENDED_REACHED":
            counters.totalDelivered = (counters.totalDelivered || 0) + 1;
            break;

        case "INFORMAL_NOTIFICATION_VIEWED": {
            const platform = parsedData.details?.channel;
            if (PLATFORM_CHANNELS.includes(platform)) {
                counters[`viewed${platform}`] = (counters[`viewed${platform}`] || 0) + 1;
            } else {
                console.warn(`Unexpected platform for ${category}: ${platform}`);
                return false;
            }
            break;
        }

        case "SEND_DIGITAL_MESSAGE":
            channel = parsedData.details?.channel;
            if (DIGITAL_CHANNELS.includes(channel)) {
                counters[`digitalSent${channel}`] = (counters[`digitalSent${channel}`] || 0) + 1;
                if (isStatusChangedTo(parsedData, "PROCESSING")) {
                    counters.totalSent = (counters.totalSent || 0) + 1;
                }
            } else {
                console.warn(`Unexpected channel for ${category}: ${channel}`);
                return false;
            }
            break;

        case "SEND_ANALOG_MESSAGE":
            channel = parsedData.details?.channel;
            if (ANALOG_CHANNELS.includes(channel)) {
                counters[`analogSent${channel}`] = (counters[`analogSent${channel}`] || 0) + 1;
                if (isStatusChangedTo(parsedData, "PROCESSING")) {
                    counters.totalSent = (counters.totalSent || 0) + 1;
                }
            } else {
                console.warn(`Unexpected channel for ${category}: ${channel}`);
                return false;
            }
            break;

        case "DELIVERED":
            channel = parsedData.details?.channel;
            if (DELIVERED_CHANNELS.includes(channel)) {
                counters[`received${channel}`] = (counters[`received${channel}`] || 0) + 1;
            } else {
                console.warn(`Unexpected or missing channel for DELIVERED: ${channel}`);
                return false;
            }
            break;

        case "PAYMENT":
            counters.paid = (counters.paid || 0) + 1;
            break;

        default:
            console.log(`Unhandled category: ${category}`);
    }
    return true;
}

module.exports = {
    applyCategoryMetric,
    DIGITAL_CHANNELS,
    ANALOG_CHANNELS,
    DELIVERED_CHANNELS,
    PLATFORM_CHANNELS
};