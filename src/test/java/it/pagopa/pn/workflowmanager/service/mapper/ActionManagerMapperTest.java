package it.pagopa.pn.workflowmanager.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.NotHandledDetails;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class ActionManagerMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void fromActionInternalToActionDto_shouldMapFieldsAndSerializeDetails() {

        NotHandledDetails details = new NotHandledDetails();

        Action action = Action.builder()
                .actionId("id1")
                .iun("iun1")
                .notBefore(Instant.now())
                .type(ActionType.WORKFLOW_DONE)
                .recipientIndex(2)
                .details(details)
                .timelineId("timeline1")
                .communicationType(CommunicationType.INFORMAL)
                .build();

        ActionManagerMapper mapper = new ActionManagerMapper(objectMapper);
        var dto = mapper.fromActionInternalToActionDto(action);

        assertEquals("id1", dto.getActionId());
        assertEquals("iun1", dto.getIun());
        assertEquals("timeline1", dto.getTimelineId());
        assertEquals(2, dto.getRecipientIndex());
        assertEquals("WORKFLOW_DONE", dto.getType().name());
        Assertions.assertNotNull(dto.getCommunicationType());
        assertEquals("INFORMAL", dto.getCommunicationType().name());
        Assertions.assertNotNull(dto.getDetails());
        Assertions.assertFalse(dto.getDetails().isEmpty());
    }

    @Test
    void fromActionInternalToActionDto_shouldHandleNullDetails() {
        Action action = Action.builder()
                .actionId("id2")
                .type(ActionType.WORKFLOW_DONE)
                .communicationType(CommunicationType.INFORMAL)
                .build();

        ActionManagerMapper mapper = new ActionManagerMapper(objectMapper);
        var dto = mapper.fromActionInternalToActionDto(action);

        assertEquals("", dto.getDetails());
    }

    @Test
    void fromActionInternalToActionDto_shouldHandleNullCommunicationType() {
        Action action = Action.builder()
                .actionId("id3")
                .type(ActionType.WORKFLOW_DONE)
                .communicationType(null)
                .build();

        ActionManagerMapper mapper = new ActionManagerMapper(objectMapper);
        var dto = mapper.fromActionInternalToActionDto(action);

        Assertions.assertNotNull(dto.getCommunicationType());
        assertEquals("INFORMAL", dto.getCommunicationType().name());
    }
}
