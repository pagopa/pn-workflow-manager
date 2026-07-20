package it.pagopa.pn.workflowmanager.dto.address;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class InformalDigitalAddressInt extends DigitalAddressInt {
    @Getter
    public enum INFORMAL_DIGITAL_ADDRESS_TYPE {
        PEC("PEC"),
        EMAIL("EMAIL"),
        SMS("SMS"),
        APPIO("APPIO");

        private final String value;

        INFORMAL_DIGITAL_ADDRESS_TYPE(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    private INFORMAL_DIGITAL_ADDRESS_TYPE type;
}
