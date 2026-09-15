package it.pagopa.pn.workflowmanager.dto.timeline.details;

import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;

public interface CourtesyAddressRelatedTimelineElement extends ConfidentialInformationTimelineElement{
    CourtesyDigitalAddressInt getDigitalAddress();
    void setDigitalAddress(CourtesyDigitalAddressInt digitalAddressInt);
}
