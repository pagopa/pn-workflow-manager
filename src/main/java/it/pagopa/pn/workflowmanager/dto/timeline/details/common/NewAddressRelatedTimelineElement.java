package it.pagopa.pn.workflowmanager.dto.timeline.details.common;


import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;

public interface NewAddressRelatedTimelineElement extends ConfidentialInformationTimelineElement {
    PhysicalAddressInt getNewAddress();
    void setNewAddress(PhysicalAddressInt digitalAddressInt);
}
