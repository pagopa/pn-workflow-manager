package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.actionmanager.model.NewAction;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.actionmanager.ActionManagerClient;
import it.pagopa.pn.workflowmanager.service.mapper.ActionManagerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionServiceRestImplTest {
    @Mock
    private ActionManagerClient actionManagerClient;
    @Mock
    private ActionManagerMapper actionManagerMapper;

    private ActionServiceRestImpl actionServiceRest;

    @BeforeEach
    void setUp() {
        actionManagerClient = mock(ActionManagerClient.class);
        actionManagerMapper = mock(ActionManagerMapper.class);
        actionServiceRest = new ActionServiceRestImpl(actionManagerClient, actionManagerMapper);
    }

    @Test
    void addOnlyActionIfAbsent_shouldCallClient() {
        Action action = mock(Action.class);
        NewAction actionDto = mock(NewAction.class);
        when(actionManagerMapper.fromActionInternalToActionDto(action)).thenReturn(actionDto);

        actionServiceRest.addOnlyActionIfAbsent(action);

        verify(actionManagerMapper, times(1)).fromActionInternalToActionDto(action);
        verify(actionManagerClient, times(1)).addOnlyActionIfAbsent(actionDto);
    }

}
