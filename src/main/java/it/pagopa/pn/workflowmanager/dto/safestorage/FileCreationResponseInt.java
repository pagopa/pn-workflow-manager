package it.pagopa.pn.workflowmanager.dto.safestorage;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
public class FileCreationResponseInt {
    private String key;
}
