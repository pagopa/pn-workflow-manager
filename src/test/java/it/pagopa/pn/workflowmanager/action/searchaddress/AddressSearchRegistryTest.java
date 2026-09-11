package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceChannelKey;
import it.pagopa.pn.workflowmanager.action.searchaddress.strategy.AddressSearchStrategy;
import it.pagopa.pn.workflowmanager.action.searchaddress.strategy.SyncAddressSearchStrategy;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AddressSearchRegistryTest {

    @Test
    void findReturnsMatchingStrategy() throws Exception {
        AddressSearchRegistry registry = new AddressSearchRegistry(null);
        AddressSearchStrategy expected = new TestSyncStrategy(Set.of(new SourceChannelKey(DigitalAddressSourceInt.PLATFORM, ChannelType.PEC)));
        AddressSearchStrategy other = new TestSyncStrategy(Set.of(new SourceChannelKey(DigitalAddressSourceInt.SPECIAL, ChannelType.EMAIL)));
        setRegistry(registry, List.of(other, expected));

        AddressSearchStrategy result = registry.find(DigitalAddressSourceInt.PLATFORM, ChannelType.PEC);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void findReturnsNullWhenNoStrategyMatches() throws Exception {
        AddressSearchRegistry registry = new AddressSearchRegistry(null);
        setRegistry(registry, List.of(new TestSyncStrategy(Set.of(new SourceChannelKey(DigitalAddressSourceInt.SPECIAL, ChannelType.EMAIL)))));

        AddressSearchStrategy result = registry.find(DigitalAddressSourceInt.GENERAL, ChannelType.PEC);

        assertThat(result).isNull();
    }

    private static void setRegistry(AddressSearchRegistry target, List<AddressSearchStrategy> strategies) throws Exception {
        Field field = AddressSearchRegistry.class.getDeclaredField("registry");
        field.setAccessible(true);
        field.set(target, strategies);
    }

    private static final class TestSyncStrategy implements SyncAddressSearchStrategy {
        private final Set<SourceChannelKey> supportedKeys;

        private TestSyncStrategy(Set<SourceChannelKey> supportedKeys) {
            this.supportedKeys = supportedKeys;
        }

        @Override
        public Set<SourceChannelKey> supportedKeys() {
            return supportedKeys;
        }

        @Override
        public it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome search(AddressSearchContext context) {
            throw new UnsupportedOperationException("Not needed for this test");
        }
    }
}
