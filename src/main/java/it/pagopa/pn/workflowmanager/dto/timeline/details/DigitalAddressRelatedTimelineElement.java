package it.pagopa.pn.workflowmanager.dto.timeline.details;


import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;

public interface DigitalAddressRelatedTimelineElement extends ConfidentialInformationTimelineElement{
    LegalDigitalAddressInt getDigitalAddress();
    void setDigitalAddress(LegalDigitalAddressInt digitalAddressInt);
}
