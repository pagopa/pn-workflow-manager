const axios = require("axios");

/**
 * Funzione di utilità interna per l'estrazione o la decodifica del campaignId dai dettagli della timeline 
 * nel caso in cui non sia mappato al primo livello dell'Image.
 */

async function getNotificationFromDelivery(responseBody) {
    const deliveryPrivateUrl = `${process?.env?.BASE_PATH || "http://localhost:8080"}/delivery-private/notifications/${responseBody.iun}`;
    try {
      const notification = await axios.get(deliveryPrivateUrl);
      return notification.data;

    } catch (e) {
      console.error("Call to delivery-private failed with status %s, iun : %s ", e.response?.status, responseBody.iun);
      throw new Error(`Call to delivery-private failed with status ${e.response?.status}, iun : ${responseBody.iun}`);
    }
}

exports.extractCampaignId = async (item) => {
    if (item.iun) {
        try {
            const notification = await getNotificationFromDelivery(item);
            return notification.campaignId;
        } catch (err) {
            console.error("Failed to extract campaignId from notification:", err.message);
            return null;
        }
    } else {
        console.warn(`Record without IUN: ${JSON.stringify(item)}`);
        return null;
    }
};