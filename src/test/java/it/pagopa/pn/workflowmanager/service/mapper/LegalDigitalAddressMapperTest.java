package it.pagopa.pn.workflowmanager.service.mapper;

import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.LegalChannelType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.LegalDigitalAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class LegalDigitalAddressMapperTest {
    @ParameterizedTest
    @CsvSource({
            "PEC, PEC",
            "SERCQ, SERCQ"
    })
    void externalToInternal_shouldMapValidChannelTypesCorrectly(String channelType, String expectedType) {
        // given
        LegalDigitalAddress external = new LegalDigitalAddress();
        external.setChannelType(LegalChannelType.valueOf(channelType));
        external.setValue("test.address@example.com");

        // when
        LegalDigitalAddressInt result = LegalDigitalAddressMapper.externalToInternal(external);

        // then
        assertNotNull(result);
        assertEquals(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.valueOf(expectedType), result.getType());
        assertEquals("test.address@example.com", result.getAddress());
    }

    @Test
    void externalToInternal_shouldThrowException_whenChannelTypeIsNotSupported() {
        // given
        LegalDigitalAddress external = new LegalDigitalAddress();
        external.setChannelType(LegalChannelType.APPIO);
        external.setValue("test.address@example.com");

        // when / then
        assertThrows(IllegalArgumentException.class,
                () -> LegalDigitalAddressMapper.externalToInternal(external));
    }

    @ParameterizedTest
    @CsvSource({
            "PEC, PEC",
            "SERCQ, SERCQ"
    })
    void internalToExternal_shouldMapValidTypesCorrectly(String type, String expectedChannelType) {
        // given
        LegalDigitalAddressInt internal = LegalDigitalAddressInt.builder()
                .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.valueOf(type))
                .address("test.address@example.com")
                .build();

        // when
        LegalDigitalAddress result = LegalDigitalAddressMapper.internalToExternal(internal);

        // then
        assertNotNull(result);
        assertEquals(LegalChannelType.valueOf(expectedChannelType), result.getChannelType());
        assertEquals("test.address@example.com", result.getValue());
    }


    @Test
    void externalToInternal_shouldPreserveAddressValue_evenWhenNull() {
        // given
        LegalDigitalAddress external = new LegalDigitalAddress();
        external.setChannelType(LegalChannelType.SERCQ);
        external.setValue(null);

        // when
        LegalDigitalAddressInt result = LegalDigitalAddressMapper.externalToInternal(external);

        // then
        assertNull(result.getAddress());
        assertEquals(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.SERCQ, result.getType());
    }
}