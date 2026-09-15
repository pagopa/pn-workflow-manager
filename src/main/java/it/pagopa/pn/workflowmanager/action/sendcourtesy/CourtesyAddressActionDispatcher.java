package it.pagopa.pn.workflowmanager.action.sendcourtesy;

import it.pagopa.pn.workflowmanager.action.utils.NotificationUtils;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.SendCourtesyMessageActionDetails;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@CustomLog
public class CourtesyAddressActionDispatcher {
    private final InformalCourtesyAddressResolver informalCourtesyAddressResolver;
    private final PnWorkflowManagerConfigs pnWorkflowManagerConfigs;
    private final SchedulerService schedulerService;

    public void dispatch(NotificationInt notification, Integer recIndex) {
        log.debug("Dispatching courtesy message for IUN: {}, recIndex: {}", notification.getIun(), recIndex);
        NotificationRecipientInt recipientInt = NotificationUtils.getRecipientFromIndex(notification, recIndex);
        String recipientId = recipientInt.getInternalId();
        String senderId = notification.getSender().getPaId();

        List<CourtesyDigitalAddressInt> courtesyAddresses = informalCourtesyAddressResolver.resolveAddresses(recipientId, senderId);
        // Al momento l'unica funzionalità che deve schedulare messaggi di cortesia, è triggerata dal flusso di SERCQ
        // e se non esiste almeno l'indirizzo di cortesia di tipo EMAIL lo segnaliamo poichè è un caso anomalo.
        if(courtesyAddresses.isEmpty() || userWithoutEmailCourtesyAddress(courtesyAddresses)) {
            log.fatal("User without EMAIL courtesy address - iun={} id={} recipientId={} senderId={}", notification.getIun(), recIndex, recipientId, senderId);
            return;
        }

        List<CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT> plannedChannels = courtesyAddresses.stream()
                .map(CourtesyDigitalAddressInt::getType)
                .toList();

        for (CourtesyDigitalAddressInt address : courtesyAddresses) {
            log.info("Dispatching courtesy message for IUN: {}, recIndex: {}, addressType: {}", notification.getIun(), recIndex, address.getType());
            if(address.getType() == CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS && !pnWorkflowManagerConfigs.getSmsCourtesyEnabled()) {
                log.info("SMS courtesy messages are disabled. Skipping SMS dispatch for IUN: {}, recIndex: {}", notification.getIun(), recIndex);
                continue;
            }

            SendCourtesyMessageActionDetails details = SendCourtesyMessageActionDetails.builder()
                    .channel(address.getType())
                    .retryIndex(0)
                    .plannedChannels(plannedChannels)
                    .build();
            schedulerService.scheduleEvent(notification.getIun(), recIndex, Instant.now(), ActionType.SEND_COURTESY_MESSAGE_ACTION, details);
        }
    }

    private boolean userWithoutEmailCourtesyAddress(List<CourtesyDigitalAddressInt> courtesyAddresses) {
        return courtesyAddresses.stream()
                .noneMatch(address -> address.getType() == CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL);
    }
}
