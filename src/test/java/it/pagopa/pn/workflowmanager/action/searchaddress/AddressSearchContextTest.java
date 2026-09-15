package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AddressSearchContextTest {

    @Test
    void shouldExposeAllRecordFields() {
        Instant sentAt = Instant.parse("2026-01-01T10:15:30Z");
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN-001")
                .build();

        AddressSearchContext context = new AddressSearchContext(ChannelType.PEC, sentAt, notification, 2, 3);

        assertThat(context.channel()).isEqualTo(ChannelType.PEC);
        assertThat(context.sentAt()).isEqualTo(sentAt);
        assertThat(context.notification()).isSameAs(notification);
        assertThat(context.recipientIndex()).isEqualTo(2);
        assertThat(context.attempt()).isEqualTo(3);
    }
}
