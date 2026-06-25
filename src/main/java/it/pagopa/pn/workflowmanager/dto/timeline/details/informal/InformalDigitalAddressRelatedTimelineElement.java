package it.pagopa.pn.workflowmanager.dto.timeline.details.informal;

import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.ConfidentialInformationTimelineElement;

public interface InformalDigitalAddressRelatedTimelineElement extends ConfidentialInformationTimelineElement {
    InformalDigitalAddressInt getDigitalAddress();
    void setDigitalAddress(InformalDigitalAddressInt digitalAddressInt);
}
