/**
 * Funzione di utilità interna per l'estrazione o la decodifica del campaignId dai dettagli della timeline 
 * nel caso in cui non sia mappato al primo livello dell'Image.
 */

async function getNotificationFromDelivery(responseBody) {
    const deliveryPrivateUrl = `${process.env.BASE_PATH}/delivery-private/notifications/${responseBody.iun}`;
    try {
      const notification = await axios.get(deliveryPrivateUrl, {
        headers: {}
      });
      return notification;
    
    } catch (e) {
      console.error("Call to delivery-private failed with status %s, iun : %s ", e.response.status, responseBody.iun);
      e.message = `Call to delivery-private failed with status ${e.response.status}, iun : ${responseBody.iun}`
      throw e;
    }
}

exports.extractCampaignId = (item)  =>{
    if (item.iun) 
        getNotificationFromDelivery(item)
        .then(notification =>  notification.data.campaignId);
    else {
        console.warn(`Record without IUN: ${JSON.stringify(item)}`);
        // Esempio logica di parsing condizionale da elementId o IUN se previsto dal pattern generato
        return null;
    }
};