package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

@ExtendWith(SpringExtension.class)
class TimelineUtilsTest {

    private TimelineUtils timelineUtils;

    @BeforeEach
    void setUp() {
        timelineUtils = new TimelineUtils();
    }

    @Test
    void buildTimeline() {
        SendDigitalMessageDetailsInt detailsInt = new SendDigitalMessageDetailsInt();
        detailsInt.setRecIndex(0);
        TimelineElementInternal actual = timelineUtils.buildTimeline(buildNotificationInt(), TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE, "001", detailsInt);
        Assertions.assertEquals("001", actual.getIun());
        Assertions.assertEquals("001", actual.getElementId());
        Assertions.assertEquals("pa_02", actual.getPaId());
        Assertions.assertEquals(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE, actual.getCategory());
        Assertions.assertEquals(detailsInt, actual.getDetails());
    }

    @Test
    void buildSendDigitalMessageTimelineElement() {
        String elementId = "send_digital_message_001";
        int recIndex = 0;
        InformalDigitalAddressInt informalDigitalAddressInt = InformalDigitalAddressInt.builder()
                .address("address")
                .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC)
                .build();
        DigitalChannelsInt digitalChannelsInt = DigitalChannelsInt.PEC;
        DigitalAddressSourceInt digitalAddressSourceInt = DigitalAddressSourceInt.SPECIAL;
        TimelineElementInternal actual = timelineUtils.buildSendDigitalMessageTimelineElement(
                buildNotificationInt(),
                elementId,
                recIndex,
                informalDigitalAddressInt,
                digitalChannelsInt,
                digitalAddressSourceInt
        );
        Assertions.assertEquals("001", actual.getIun());
        Assertions.assertEquals(elementId, actual.getElementId());
        Assertions.assertEquals("pa_02", actual.getPaId());
        Assertions.assertEquals(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE, actual.getCategory());
        Assertions.assertNotNull(actual.getDetails());
        SendDigitalMessageDetailsInt detailsInt = (SendDigitalMessageDetailsInt) actual.getDetails();
        Assertions.assertEquals(recIndex, detailsInt.getRecIndex());
        Assertions.assertEquals(informalDigitalAddressInt, detailsInt.getDigitalAddress());
        Assertions.assertEquals(digitalChannelsInt, detailsInt.getChannel());
        Assertions.assertEquals(digitalAddressSourceInt, detailsInt.getDigitalAddressSource());
    }

    private NotificationInt buildNotificationInt() {
        return NotificationInt.builder()
                .iun("001")
                .paProtocolNumber("protocol_01")
                .sender(NotificationSenderInt.builder()
                        .paId("pa_02")
                        .build()
                )
                .recipients(Collections.singletonList(
                        NotificationRecipientInt.builder()
                                .taxId("testIdRecipient")
                                .denomination("Nome Cognome/Ragione Sociale")
                                .build()
                ))
                .build();
    }
}