package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.actionmanager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.common.rest.error.v1.dto.ProblemError;
import it.pagopa.pn.commons.pnclients.RestTemplateFactory;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.actionmanager.model.ActionType;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.actionmanager.model.CommunicationType;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.actionmanager.model.NewAction;
import it.pagopa.pn.workflowmanager.MockAWSObjectsTest;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.config.msclient.ActionManagerApiConfigurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Collections;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_DELIVERYPUSH_ACTION_CONFLICT;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ActiveProfiles("test")
@TestPropertySource(
        properties = {
                "pn.workflow-manager.action-manager-base-url=http://localhost:9999"
        }
)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ActionManagerClientImpl.class
        , ActionManagerApiConfigurator.class,
        PnWorkflowManagerConfigs.class,
        RestTemplateFactory.class})
class ActionManagerClientImplTest extends MockAWSObjectsTest {
    @Autowired
    private ActionManagerClientImpl actionManagerClient;

    @Test
    void testAddOnlyActionIfAbsentSuccess() throws JsonProcessingException {
        try (ClientAndServer ignored = startClientAndServer(9999);
             MockServerClient mockServerClient = new MockServerClient("localhost", 9999)) {
            NewAction actionRequest = new NewAction();
            actionRequest.setActionId("action456");
            actionRequest.setTimelineId("timeline123");
            actionRequest.setIun("iun123");
            actionRequest.setNotBefore(Instant.parse("2024-11-28T23:26:33.841637462Z"));
            actionRequest.setRecipientIndex(0);
            actionRequest.communicationType(CommunicationType.INFORMAL);
            actionRequest.setType(ActionType.valueOf(ActionType.START_WORKFLOW.name()));

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            String requestJson = mapper.writeValueAsString(actionRequest);

            mockServerClient
                    .when(request()
                            .withMethod("POST")
                            .withPath("/action-manager-private/action")
                            .withBody(requestJson)
                    )
                    .respond(response()
                            .withStatusCode(200)
                            .withContentType(MediaType.APPLICATION_JSON)
                    );
            Assertions.assertDoesNotThrow(() -> actionManagerClient.addOnlyActionIfAbsent(actionRequest));
        }
    }

    @Test
    void testAddOnlyActionIfAbsentFailSilently() throws JsonProcessingException {
        try (ClientAndServer ignored = startClientAndServer(9999);
             MockServerClient mockServerClient = new MockServerClient("localhost", 9999)) {
            NewAction actionRequest = new NewAction();
            actionRequest.setActionId("action456");
            actionRequest.setTimelineId("timeline123");
            actionRequest.setIun("iun123");
            actionRequest.setNotBefore(Instant.parse("2024-11-28T23:26:33.841637462Z"));
            actionRequest.setRecipientIndex(0);
            actionRequest.setType(ActionType.valueOf(ActionType.ANALOG_WORKFLOW.name()));

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            ProblemError problemError = new ProblemError();
            problemError.setCode(ERROR_CODE_DELIVERYPUSH_ACTION_CONFLICT);
            problemError.setDetail("Action already exists");
            problemError.setElement("actionId");

            Problem problem = new Problem();
            problem.setErrors(Collections.singletonList(problemError));

            String requestJson = mapper.writeValueAsString(actionRequest);

            String responseJson = mapper.writeValueAsString(problem);

            mockServerClient
                    .when(request()
                            .withMethod("POST")
                            .withPath("/action-manager-private/action")
                            .withBody(requestJson)
                    )
                    .respond(response()
                            .withStatusCode(409)
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withBody(responseJson)
                    );

            Assertions.assertDoesNotThrow(() -> actionManagerClient.addOnlyActionIfAbsent(actionRequest));
        }
    }

    @Test
    void testAddOnlyActionIfAbsentFails() throws JsonProcessingException {
        try (ClientAndServer ignored = startClientAndServer(9999);
             MockServerClient mockServerClient = new MockServerClient("localhost", 9999)) {
            NewAction actionRequest = new NewAction();
            actionRequest.setActionId("action456");
            actionRequest.setTimelineId("timeline123");
            actionRequest.setIun("iun123");
            actionRequest.setNotBefore(Instant.parse("2024-11-28T23:26:33.841637462Z"));
            actionRequest.setRecipientIndex(0);
            actionRequest.setType(ActionType.valueOf(ActionType.ANALOG_WORKFLOW.name()));

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            ProblemError problemError = new ProblemError();
            problemError.setCode("PN_GENERIC_ERROR");
            problemError.setDetail("Generic error occurred");
            problemError.setElement("request");

            Problem problem = new Problem();
            problem.setErrors(Collections.singletonList(problemError));

            String requestJson = mapper.writeValueAsString(actionRequest);

            String responseJson = mapper.writeValueAsString(problem);

            mockServerClient
                    .when(request()
                            .withMethod("POST")
                            .withPath("/action-manager-private/action")
                            .withBody(requestJson)
                    )
                    .respond(response()
                            .withStatusCode(500)
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withBody(responseJson)
                    );

            Assertions.assertThrows(Exception.class, () -> actionManagerClient.addOnlyActionIfAbsent(actionRequest));
        }
    }
}