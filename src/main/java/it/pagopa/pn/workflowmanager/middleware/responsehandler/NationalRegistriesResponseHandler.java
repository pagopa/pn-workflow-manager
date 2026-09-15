package it.pagopa.pn.workflowmanager.middleware.responsehandler;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchOrchestrator;
import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchUtils;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.workflowmanager.dto.timeline.DeliveryModeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ContactPhaseInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.PublicRegistryCallDetailsInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.HandleEventUtils;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.utils.PublicRegistryUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_DELIVERYNOTFOUND;

@Component
@CustomLog
@AllArgsConstructor
public class NationalRegistriesResponseHandler {
    private final PublicRegistryUtils publicRegistryUtils;
    private final NotificationService notificationService;
    private final AddressSearchUtils searchUtils;
    private final AddressSearchOrchestrator addressSearchOrchestrator;
    private final TimelineUtils timelineUtils;

    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_NATIONAL_REGISTRIES;
    String GET_DIGITAL_GENERAL_ADDRESS = "GET DIGITAL GENERAL ADDRESS";


    public void handleResponse(NationalRegistriesResponse response) {
        String correlationId = response.getCorrelationId();
        String iun = timelineUtils.getIunFromTimelineId(correlationId);
        addMdcFilter(iun, correlationId);

        log.info("Async response received from service {} for {} with correlationId={}",
                CLIENT_NAME, GET_DIGITAL_GENERAL_ADDRESS, correlationId);

        final String processName = GET_DIGITAL_GENERAL_ADDRESS + " response handler";
        
        log.logStartingProcess(processName);
        
        try {
            NotificationInt notification = notificationService.getInformalNotificationByIun(iun);
            log.debug("Notification successfully obtained  - iun={}", notification.getIun());

            //Viene ottenuto l'oggetto di timeline creato in fase d'invio notifica al public registry
            PublicRegistryCallDetailsInt publicRegistryCallDetails = publicRegistryUtils.getPublicRegistryCallDetail(iun, correlationId);
            Integer recIndex = publicRegistryCallDetails.getRecIndex();

            publicRegistryUtils.addPublicRegistryResponseToTimeline(notification, recIndex, response);

            handleSpecificContactPhase(response, correlationId, iun, notification, publicRegistryCallDetails, recIndex);

            log.logEndingProcess(processName);
        } catch (Exception ex){
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            throw ex;
        }
    }

    private void handleSpecificContactPhase(NationalRegistriesResponse response, String correlationId, String iun, 
                                            NotificationInt notification, PublicRegistryCallDetailsInt publicRegistryCallDetails,
                                            Integer recIndex) {
        ContactPhaseInt contactPhase = publicRegistryCallDetails.getContactPhase();
        log.info("public registry response is in contactPhase {} iun {} id {} ", contactPhase, iun, recIndex);

        //In base alla fase di contatto, inserita in timeline al momento dell'invio, viene scelto il percorso da prendere
        if (contactPhase != null) {
            //request has been sent in digital or analog workflow
            if (contactPhase == ContactPhaseInt.SEND_ATTEMPT) {
                handleResponseForSendAttempt(response, notification, publicRegistryCallDetails);
            } else {
                handleContactPhaseError(correlationId, publicRegistryCallDetails);
            }
        } else {
            handleContactPhaseError(correlationId, publicRegistryCallDetails);
        }
    }

    private void handleContactPhaseError(String correlationId, PublicRegistryCallDetailsInt publicRegistryCallDetails) {
        log.error("Specified contactPhase {} does not exist for correlationId {}", publicRegistryCallDetails.getContactPhase(), correlationId);
        throw new PnInternalException("Specified contactPhase " + publicRegistryCallDetails.getContactPhase() + " does not exist for correlationId " + correlationId, ERROR_CODE_WORKFLOWMANAGER_DELIVERYNOTFOUND);
    }

    private void handleResponseForSendAttempt(NationalRegistriesResponse response, 
                                              NotificationInt notification,
                                              PublicRegistryCallDetailsInt publicRegistryCallDetails) {
        Integer recIndex = publicRegistryCallDetails.getRecIndex();
        String iun = notification.getIun();
        
        log.debug("Start handleResponseForSendAttempt iun {} id {} deliveryMode {}", iun, recIndex, publicRegistryCallDetails.getDeliveryMode());

        if (publicRegistryCallDetails.getDeliveryMode() != null) {

            if (publicRegistryCallDetails.getDeliveryMode() == DeliveryModeInt.DIGITAL) {
                boolean addressAvailable = response.getDigitalAddress() != null;
                timelineUtils.addAvailabilitySourceToTimeline(recIndex, notification, DigitalAddressSourceInt.GENERAL, addressAvailable, response.getDigitalAddress());
                AddressSearchContext ctx = new AddressSearchContext(null, notification.getSentAt(), notification, recIndex, null);
                if (addressAvailable) {
                    searchUtils.scheduleSendChannelMessageAction(ctx, DigitalAddressSourceInt.GENERAL);
                } else {
                    addressSearchOrchestrator.handle(ctx);
                }
            } else {
                handleDeliveryModeError(iun, publicRegistryCallDetails.getDeliveryMode(), recIndex);
            }
        } else {
            handleDeliveryModeError(iun, publicRegistryCallDetails.getDeliveryMode(), recIndex);
        }
    }

    private void handleDeliveryModeError(String iun, DeliveryModeInt deliveryMode, Integer recIndex) {
        log.error("Specified deliveryMode {} does not exist - iun {} id {}", deliveryMode, iun, recIndex);
        throw new PnInternalException("Specified deliveryMode " + deliveryMode + " does not exist - iun " + iun + " id " + recIndex, ERROR_CODE_WORKFLOWMANAGER_DELIVERYNOTFOUND);
    }

    private static void addMdcFilter(String iun, String correlationId) {
        HandleEventUtils.addIunToMdc(iun);
        HandleEventUtils.addCorrelationIdToMdc(correlationId);
    }

}
