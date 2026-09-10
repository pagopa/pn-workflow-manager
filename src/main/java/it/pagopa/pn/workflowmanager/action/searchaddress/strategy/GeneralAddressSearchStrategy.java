package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceChannelKey;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.timeline.DeliveryModeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ContactPhaseInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressOK;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressRequestBody;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressRequestBodyFilter;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.nationalregistries.PnNationalRegistriesClient;
import it.pagopa.pn.workflowmanager.utils.PublicRegistryUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@AllArgsConstructor
@CustomLog
public class GeneralAddressSearchStrategy implements AsyncAddressSearchStrategy {

    private final PnNationalRegistriesClient nationalRegistriesClient;
    private final PublicRegistryUtils publicRegistryUtils;

    @Override
    public Set<SourceChannelKey> supportedKeys() {
        return new HashSet<>(Set.of(
                new SourceChannelKey(DigitalAddressSourceInt.GENERAL, ChannelType.PEC)
        ));
    }

    @Override
    public void triggerSearch(AddressSearchContext context) {
        //TODO aggiungere logs

        String recipientId = context.notification().getRecipients().get(context.recipientIndex()).getInternalId();
        AddressRequestBody addressRequestBody = createAddressRequestBody(context);

        nationalRegistriesClient.getAddresses(recipientId, addressRequestBody);

        publicRegistryUtils.addPublicRegistryCallToTimeline(
                context.notification(),
                context.recipientIndex(),
                ContactPhaseInt.SEND_ATTEMPT,
                0,
                addressRequestBody.getFilter().getCorrelationId(),
                DeliveryModeInt.DIGITAL,
                null);

        log.debug("End sendRequestForGetAddress correlationId={} - iun={} id={}", addressRequestBody.getFilter().getCorrelationId(), context.notification().getIun(), context.recipientIndex());
    }

    private AddressRequestBody createAddressRequestBody(AddressSearchContext context) {
        AddressRequestBodyFilter filter = new AddressRequestBodyFilter()
                .taxId(context.notification().getRecipients().get(context.recipientIndex()).getTaxId())
                .correlationId(publicRegistryUtils.generateCorrelationId(
                        context.notification().getIun(),
                        context.recipientIndex(),
                        ContactPhaseInt.SEND_ATTEMPT,
                        0,
                        DeliveryModeInt.DIGITAL
                ))
                .referenceRequestDate(context.notification().getSentAt())
                .domicileType(AddressRequestBodyFilter.DomicileTypeEnum.DIGITAL);

        AddressRequestBody addressRequestBody = new AddressRequestBody();
        addressRequestBody.filter(filter);
        return addressRequestBody;
    }
}
