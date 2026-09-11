package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressSearchOrchestratorTest {

    @Mock
    private ChannelAddressSourceConfigResolver configResolver;
    @Mock
    private AddressSearchProgressor progressor;
    @Mock
    private AddressSearchUtils searchUtils;

    private AddressSearchOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new AddressSearchOrchestrator(configResolver, progressor, searchUtils);
    }

    @Test
    void handleSchedulesSpecialWhenNoSourcesAreConfigured() {
        AddressSearchContext context = buildContext();
        when(configResolver.resolveSources(ChannelType.PEC, context.sentAt())).thenReturn(Optional.empty());

        orchestrator.handle(context);

        verify(searchUtils).scheduleSendChannelMessageAction(context, DigitalAddressSourceInt.SPECIAL);
        verifyNoInteractions(progressor);
    }

    @Test
    void handleDelegatesToProgressorWhenSourcesAreConfigured() {
        AddressSearchContext context = buildContext();
        List<DigitalAddressSourceInt> sources = List.of(DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL);
        when(configResolver.resolveSources(ChannelType.PEC, context.sentAt())).thenReturn(Optional.of(sources));

        orchestrator.handle(context);

        verify(progressor).run(context, sources, 0);
        verifyNoInteractions(searchUtils);
    }

    private static AddressSearchContext buildContext() {
        Instant sentAt = Instant.parse("2026-03-10T11:15:30Z");
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN-ORCH")
                .sentAt(sentAt)
                .build();
        return new AddressSearchContext(ChannelType.PEC, sentAt, notification, 0, 1);
    }
}
