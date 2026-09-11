package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.nationalregistries.PnNationalRegistriesClient;
import it.pagopa.pn.workflowmanager.utils.PublicRegistryUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneralAddressSearchStrategyTest {

    @Mock
    private PnNationalRegistriesClient nationalRegistriesClient;
    @Mock
    private PublicRegistryUtils publicRegistryUtils;
    @InjectMocks
    private GeneralAddressSearchStrategy strategy;

    @Test
    void shouldTriggerSearchAndAddTimelineEntry() {
        AddressSearchContext context = buildContext();
        when(publicRegistryUtils.generateCorrelationId(any(), any(), any(), any(Integer.class), any()))
                .thenReturn("corr-id");

        strategy.triggerSearch(context);

        ArgumentCaptor<it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressRequestBody> bodyCaptor =
                ArgumentCaptor.forClass(it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressRequestBody.class);

        verify(nationalRegistriesClient).getAddresses(eq("PID"), bodyCaptor.capture());
        assertEquals("corr-id", bodyCaptor.getValue().getFilter().getCorrelationId());
        assertEquals("TAXID", bodyCaptor.getValue().getFilter().getTaxId());

        verify(publicRegistryUtils).addPublicRegistryCallToTimeline(
                eq(context.notification()),
                eq(0),
                eq(it.pagopa.pn.workflowmanager.dto.timeline.details.ContactPhaseInt.SEND_ATTEMPT),
                eq(0),
                eq("corr-id"),
                eq(it.pagopa.pn.workflowmanager.dto.timeline.DeliveryModeInt.DIGITAL),
                eq(null)
        );
    }

    private AddressSearchContext buildContext() {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .internalId("PID")
                .taxId("TAXID")
                .build();
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN")
                .sentAt(Instant.now())
                .recipients(List.of(recipient))
                .build();
        return new AddressSearchContext(ChannelType.PEC, notification.getSentAt(), notification, 0, 0);
    }
}

