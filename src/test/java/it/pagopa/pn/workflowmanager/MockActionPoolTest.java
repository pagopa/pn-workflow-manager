package it.pagopa.pn.workflowmanager;


import it.pagopa.pn.workflowmanager.middleware.queue.actionspool.ActionsPool;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class MockActionPoolTest {
    @MockitoBean
    private ActionsPool actionsPool;
}
