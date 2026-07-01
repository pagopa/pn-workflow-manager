package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.workflowmanager.action.utils.AttachmentType;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PnSendModeUtilsTest {

    @Test
    void getPnSendModeReturnsNullWhenTimeIsBeforeFirstConfiguration() {
        PnSendModeUtils pnSendModeUtils = new PnSendModeUtils(buildConfigs(List.of(
                "2024-03-01T00:00:00Z;IGNORED;DOCUMENTS;PAYMENTS;COVERPAGE"
        )));

        PnSendMode result = pnSendModeUtils.getPnSendMode(Instant.parse("2024-02-29T23:59:59Z"));

        assertNull(result);
    }

    @Test
    void getPnSendModeReturnsMatchingConfigurationForExactStartDateAfterSorting() {
        PnSendModeUtils pnSendModeUtils = new PnSendModeUtils(buildConfigs(List.of(
                "2024-06-01T00:00:00Z;IGNORED;PAYMENTS;COVERPAGE;COVERPAGE|PAYMENTS",
                "2024-01-01T00:00:00Z;IGNORED;COVERPAGE|DOCUMENTS;DOCUMENTS;PAYMENTS"
        )));

        PnSendMode result = pnSendModeUtils.getPnSendMode(Instant.parse("2024-01-01T00:00:00Z"));

        assertNotNull(result);
        assertTrue(result.getPecSendAttachmentMode().includes(AttachmentType.COVERPAGE));
        assertTrue(result.getPecSendAttachmentMode().includes(AttachmentType.DOCUMENTS));
        assertFalse(result.getPecSendAttachmentMode().includes(AttachmentType.PAYMENTS));
        assertTrue(result.getEmailSendAttachmentMode().includes(AttachmentType.DOCUMENTS));
        assertTrue(result.getSimpleRegisteredLetterSendAttachmentMode().includes(AttachmentType.PAYMENTS));
    }

    @Test
    void getPnSendModeReturnsMostRecentConfigurationBeforeRequestedTime() {
        PnSendModeUtils pnSendModeUtils = new PnSendModeUtils(buildConfigs(List.of(
                "2024-06-01T00:00:00Z;IGNORED;PAYMENTS;COVERPAGE;COVERPAGE|PAYMENTS",
                "2024-01-01T00:00:00Z;IGNORED;COVERPAGE|DOCUMENTS;DOCUMENTS;PAYMENTS"
        )));

        PnSendMode result = pnSendModeUtils.getPnSendMode(Instant.parse("2024-06-15T12:00:00Z"));

        assertNotNull(result);
        assertTrue(result.getPecSendAttachmentMode().includes(AttachmentType.PAYMENTS));
        assertFalse(result.getPecSendAttachmentMode().includes(AttachmentType.COVERPAGE));
        assertTrue(result.getEmailSendAttachmentMode().includes(AttachmentType.COVERPAGE));
        assertTrue(result.getSimpleRegisteredLetterSendAttachmentMode().includes(AttachmentType.COVERPAGE));
        assertTrue(result.getSimpleRegisteredLetterSendAttachmentMode().includes(AttachmentType.PAYMENTS));
    }

    private PnWorkflowManagerConfigs buildConfigs(List<String> pnSendModes) {
        PnWorkflowManagerConfigs configs = new PnWorkflowManagerConfigs();
        configs.setPnSendMode(pnSendModes);
        return configs;
    }
}

