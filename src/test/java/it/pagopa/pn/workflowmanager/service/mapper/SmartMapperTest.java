package it.pagopa.pn.workflowmanager.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.*;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageDetailsInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

class SmartMapperTest {
    private SmartMapper smartMapper;
    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        smartMapper = new SmartMapper(objectMapper);
    }

    @Test
    void fromInternalToExternalSendDigitalDetails() {
        SendDigitalMessageDetailsInt sendDigitalDetails = SendDigitalMessageDetailsInt.builder()
                .recIndex(0)
                .digitalAddressSource(DigitalAddressSourceInt.PLATFORM)
                .digitalAddress(InformalDigitalAddressInt.builder()
                        .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC)
                        .address("testAddress@gmail.com")
                        .build())
                .retryNumber(0)
                .categoryType("SEND_DIGITAL_MESSAGE")
                .build();

        var details = smartMapper.mapToClassWithObjectMapper(sendDigitalDetails, TimelineElementDetails.class);
        var sendDigitalDetailsExt = (SendDigitalMessageDetails) details;
        Assertions.assertEquals(sendDigitalDetails.getRecIndex(),  sendDigitalDetailsExt.getRecIndex());
        Assertions.assertEquals(sendDigitalDetails.getDigitalAddress().getAddress(), Objects.requireNonNull(sendDigitalDetailsExt.getDigitalAddress()).getAddress() );
    }

    @Test
    void fromExternalToInternalSendDigitalDetails() {

        var timelineElementDetails = new SendDigitalDetails()
                .recIndex(0)
                .digitalAddressSource(DigitalAddressSource.PLATFORM)
                .digitalAddress(new DigitalAddress()
                        .type("PEC")
                        .address("testAddress@gmail.com"))
                .retryNumber(0);

        SendDigitalMessageDetailsInt details = SmartMapper.mapToClass(timelineElementDetails, SendDigitalMessageDetailsInt.class);

        Assertions.assertEquals(timelineElementDetails.getRecIndex(), details.getRecIndex());
        Assertions.assertEquals(timelineElementDetails.getDigitalAddress().getAddress(), details.getDigitalAddress().getAddress() );
    }


    @Test
    void mapToClassWithNullSource() {
        TimelineElementInternal source = null;

        TimelineElementInternal ret = SmartMapper.mapToClass(source, TimelineElementInternal.class);

        Assertions.assertNull(ret);
    }

    @Test
    void mapToClassWithObjectMapperWithNullSource() {
        TimelineElementInternal source = null;

        TimelineElementDetails ret = smartMapper.mapToClassWithObjectMapper(source, TimelineElementDetails.class);

        Assertions.assertNull(ret);
    }
}