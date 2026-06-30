package it.pagopa.pn.workflowmanager.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.TimelineElementDetails;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SmartMapperTest {
    private SmartMapper smartMapper;
    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        smartMapper = new SmartMapper(objectMapper);
    }

    //ToDo: aggiungere alcuni test nel prossimo task

    @Test
    void mapToClassWithNullSource() {
        TimelineElementInternal source = null;

        TimelineElementInternal ret = SmartMapper.mapToClass(source, TimelineElementInternal.class);

        Assertions.assertNull(ret);
    }

    @Test
    void mapToClassWithObjectMappperWithNullSource() {
        TimelineElementInternal source = null;

        TimelineElementDetails ret = smartMapper.mapToClassWithObjectMapper(source, TimelineElementDetails.class);

        Assertions.assertNull(ret);
    }
}