package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelEventProcessor;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.io.IoEventNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class IoEventHandler {
    private final IoEventNormalizer ioEventNormalizer;
    private final ChannelEventProcessor channelEventProcessor;

    public void handle(IoOutcomeEvent event) {
        log.info("Handling IO outcome event: {}", event);
        channelEventProcessor.process(event, ioEventNormalizer);
    }
}
