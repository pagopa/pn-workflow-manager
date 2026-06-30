package it.pagopa.pn.workflowmanager.dto.ext.delivery.notification;

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
public class LocalizedMessageInt {
    @Nonnull
    private String subject;
    @Nonnull
    private String longBody;
    private String shortBody;
    @Nonnull
    private String language;
}
