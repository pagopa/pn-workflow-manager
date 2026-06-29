package it.pagopa.pn.workflowmanager.dto.notification.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nonnull;
import lombok.*;

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
