package it.pagopa.pn.workflowmanager.middleware.queue.consumer.event;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class DigitalMessageReferenceInt {
    private String system;
    private String id;
    private String location;
}
