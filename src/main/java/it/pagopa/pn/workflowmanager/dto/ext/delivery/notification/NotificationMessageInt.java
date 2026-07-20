package it.pagopa.pn.workflowmanager.dto.ext.delivery.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import jakarta.annotation.Nonnull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotificationMessageInt {
    @Nonnull
    private LocalizedMessageInt primaryMessage;
    private LocalizedMessageInt additionalMessage;
}
