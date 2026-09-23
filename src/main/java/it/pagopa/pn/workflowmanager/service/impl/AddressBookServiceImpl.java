package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyDigitalAddress;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.LegalDigitalAddress;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.Consent;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.CxTypeAuthFleet;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.userattributes.PnUserAttributesClient;
import it.pagopa.pn.workflowmanager.service.AddressBookService;
import it.pagopa.pn.workflowmanager.service.mapper.CourtesyDigitalAddressMapper;
import it.pagopa.pn.workflowmanager.service.mapper.LegalDigitalAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressBookServiceImpl implements AddressBookService {
    private final PnUserAttributesClient userAttributesClient;
    private final PnWorkflowManagerConfigs configs;

    @Override
    public Optional<LegalDigitalAddressInt> getPlatformAddresses(String recipientId, String senderId) {
        List<LegalDigitalAddress> legalDigitalAddresses = userAttributesClient.getLegalAddressBySender(recipientId, senderId);

        log.info("GetLegalAddress OK - senderId={}", senderId);


        if (legalDigitalAddresses != null && !legalDigitalAddresses.isEmpty()) {

            if (legalDigitalAddresses.size() > 1) {
                log.warn("Digital addresses list contains more than one element - senderId={}", senderId);
            }

            List<LegalDigitalAddressInt> digitalAddresses = legalDigitalAddresses.stream().map(
                    LegalDigitalAddressMapper::externalToInternal
            ).toList();

            for (LegalDigitalAddressInt address : digitalAddresses) {
                log.debug("For senderId={} address type={} is available", senderId, address.getType());

                if (LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC.equals(address.getType()) || LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.SERCQ.equals(address.getType())) {
                    return Optional.of(address);
                }
            }
        }

        log.debug("list legal address is empty - senderId={}", senderId);
        return Optional.empty();

    }

    @Override
    public List<CourtesyDigitalAddressInt> getCourtesyAddress(String recipientId, String senderId) {
        List<CourtesyDigitalAddress> courtesyDigitalAddresses = userAttributesClient.getCourtesyAddressBySender(recipientId, senderId);

        if (courtesyDigitalAddresses != null && !courtesyDigitalAddresses.isEmpty()) {
            log.info("getCourtesyAddress OK - senderId={}, recipientId={} courtesyListSize={}", senderId, recipientId, courtesyDigitalAddresses.size());
            return courtesyDigitalAddresses.stream()
                    .map(
                            CourtesyDigitalAddressMapper::externalToInternal
                    )
                    .toList();
        }

        log.info("getCourtesyAddress OK - senderId={}, recipientId={} courtesyListSize=0", senderId, recipientId);
        return new ArrayList<>();
    }

    @Override
    public boolean areMandatoryConsentsAccepted(String recipientId, String cxId) {

        if (CollectionUtils.isEmpty(configs.getConsentsForPlatformSearch())) {
            log.debug("No configurations for consents");
            return true;
        }

        List<Consent> consents = userAttributesClient.getConsents(recipientId, CxTypeAuthFleet.fromValue(cxId));
        boolean thereAreConsentsToCheck = !CollectionUtils.isEmpty(configs.getConsentsForPlatformSearch());
        if (CollectionUtils.isEmpty(consents) && thereAreConsentsToCheck) {
            log.debug("Mandatory consents not accepted - recipientId={} cxId={}", recipientId, cxId);
            return false;
        }

        return configs.getConsentsForPlatformSearch().stream().allMatch(configConsent ->
                consents.stream().anyMatch(consent ->
                        configConsent.getType().equals(consent.getConsentType().getValue()) &&
                                configConsent.getVersion() <= Integer.parseInt(consent.getConsentVersion()) &&
                                Boolean.TRUE.equals(consent.getAccepted())
                )
        );
    }

}
