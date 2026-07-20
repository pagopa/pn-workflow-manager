package it.pagopa.pn.workflowmanager.dto.timeline.details;

import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;

public interface InformalDigitalAddressRelatedTimelineElement extends ConfidentialInformationTimelineElement {
    InformalDigitalAddressInt getDigitalAddress();
    void setDigitalAddress(InformalDigitalAddressInt digitalAddressInt);
}
