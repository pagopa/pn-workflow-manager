package it.pagopa.pn.workflowmanager.dto.timeline;

import org.junit.jupiter.api.Test;

import static it.pagopa.pn.workflowmanager.dto.notification.informalnotification.AnalogDeliveryTypeInt.RS;
import static it.pagopa.pn.workflowmanager.dto.notification.informalnotification.DigitalChannelsInt.APPIO;
import static org.assertj.core.api.Assertions.assertThat;

class TimelineEventIdBuilderTest {

    private static final String IUN = "KWKU-JHXN-HJXM-202304-U-A";

    @Test
    void buildSEND_DIGITAL_MESSAGETest() {
        String timeLineEventIdExpected = "SEND_DIGITAL_MESSAGE.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0.CHANNEL_APPIO";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.SEND_DIGITAL_MESSAGE.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .withChannel(APPIO.getValue())
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.SEND_DIGITAL_MESSAGE.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .channel(APPIO.getValue())
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildSEND_DIGITAL_MESSAGE_SKIPTest() {
        String timeLineEventIdExpected = "SEND_DIGITAL_MESSAGE_SKIP.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0.CHANNEL_APPIO";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.SEND_DIGITAL_MESSAGE_SKIP.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .withChannel(APPIO.getValue())
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.SEND_DIGITAL_MESSAGE_SKIP.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .channel(APPIO.getValue())
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildSEND_DIGITAL_MESSAGE_PROGRESSTest() {
        String timeLineEventIdExpected = "SEND_DIGITAL_MESSAGE_PROGRESS.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0.IDX_0.CHANNEL_APPIO";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.SEND_DIGITAL_MESSAGE_PROGRESS.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .withChannel(APPIO.getValue())
                .withProgressIndex(0)
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.SEND_DIGITAL_MESSAGE_PROGRESS.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .channel(APPIO.getValue())
                .progressIndex(0)
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildSEND_DIGITAL_MESSAGE_FEEDBACKTest() {
        String timeLineEventIdExpected = "SEND_DIGITAL_MESSAGE_FEEDBACK.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0.CHANNEL_APPIO";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.SEND_DIGITAL_MESSAGE_FEEDBACK.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .withChannel(APPIO.getValue())
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.SEND_DIGITAL_MESSAGE_FEEDBACK.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .channel(APPIO.getValue())
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildPREPARE_ANALOG_DELIVERYTest() {
        String timeLineEventIdExpected = "PREPARE_ANALOG_DELIVERY.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0.ATTEMPT_0.DELIVERYTYPE_RS";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.PREPARE_ANALOG_DELIVERY.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .withSentAttemptMade(0)
                .withDeliveryType(RS.getValue())
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.PREPARE_ANALOG_DELIVERY.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .sentAttemptMade(0)
                .deliveryType(RS.getValue())
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildSEND_ANALOG_MESSAGETest() {
        String timeLineEventIdExpected = "SEND_ANALOG_MESSAGE.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0.ATTEMPT_0.DELIVERYTYPE_RS";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.SEND_ANALOG_MESSAGE.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .withSentAttemptMade(0)
                .withDeliveryType(RS.getValue())
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.SEND_ANALOG_MESSAGE.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .sentAttemptMade(0)
                .deliveryType(RS.getValue())
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildSEND_ANALOG_MESSAGE_PROGRESSTest() {
        String timeLineEventIdExpected = "SEND_ANALOG_MESSAGE_PROGRESS.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0.ATTEMPT_0.IDX_0.DELIVERYTYPE_RS";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.SEND_ANALOG_MESSAGE_PROGRESS.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .withSentAttemptMade(0)
                .withProgressIndex(0)
                .withDeliveryType(RS.getValue())
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.SEND_ANALOG_MESSAGE_PROGRESS.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .sentAttemptMade(0)
                .progressIndex(0)
                .deliveryType(RS.getValue())
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildSEND_ANALOG_MESSAGE_FEEDBACKTest() {
        String timeLineEventIdExpected = "SEND_ANALOG_MESSAGE_FEEDBACK.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0.ATTEMPT_0.DELIVERYTYPE_RS";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.SEND_ANALOG_MESSAGE_FEEDBACK.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .withSentAttemptMade(0)
                .withDeliveryType(RS.getValue())
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.SEND_ANALOG_MESSAGE_FEEDBACK.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .sentAttemptMade(0)
                .deliveryType(RS.getValue())
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildREACHEDTest() {
        String timeLineEventIdExpected = "REACHED.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0.CHANNEL_APPIO";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.REACHED.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .withChannel(APPIO.getValue())
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.REACHED.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .channel(APPIO.getValue())
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildWORKFLOW_ENDED_REACHEDTest() {
        String timeLineEventIdExpected = "WORKFLOW_ENDED_REACHED.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.WORKFLOW_ENDED_REACHED.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.WORKFLOW_ENDED_REACHED.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildWORKFLOW_ENDED_UNREACHEDTest() {
        String timeLineEventIdExpected = "WORKFLOW_ENDED_UNREACHED.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.WORKFLOW_ENDED_UNREACHED.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.WORKFLOW_ENDED_UNREACHED.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildWORKFLOW_ENDED_UNDELIVERABLETest() {
        String timeLineEventIdExpected = "WORKFLOW_ENDED_UNDELIVERABLE.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.WORKFLOW_ENDED_UNDELIVERABLE.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.WORKFLOW_ENDED_UNDELIVERABLE.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildWORKFLOW_DONETest() {
        String timeLineEventIdExpected = "WORKFLOW_DONE.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.WORKFLOW_DONE.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.WORKFLOW_DONE.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildCOVERPAGE_CREATION_REQUESTTest() {
        String timeLineEventIdExpected = "COVERPAGE_CREATION_REQUEST.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.COVERPAGE_CREATION_REQUEST.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.COVERPAGE_CREATION_REQUEST.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildINFORMAL_NOTIFICATION_VIEWEDTest() {
        String timeLineEventIdExpected = "INFORMAL_NOTIFICATION_VIEWED.IUN_KWKU-JHXN-HJXM-202304-U-A.RECINDEX_0.CHANNEL_APPIO";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.INFORMAL_NOTIFICATION_VIEWED.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .withChannel(APPIO.getValue())
                .build();
        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);
        String timeLineEventIdActualFromBuildEvent = TimelineEventId.INFORMAL_NOTIFICATION_VIEWED.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .channel(APPIO.getValue())
                .build());
        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }


}
