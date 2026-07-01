package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.workflowmanager.action.utils.AttachmentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SendAttachmentModeTest {

    @Test
    void fromValueParsesTrimmedAttachmentTypesSeparatedByPipe() {
        SendAttachmentMode result = SendAttachmentMode.fromValue(" COVERPAGE | DOCUMENTS ");

        assertTrue(result.includes(AttachmentType.COVERPAGE));
        assertTrue(result.includes(AttachmentType.DOCUMENTS));
        assertFalse(result.includes(AttachmentType.PAYMENTS));
    }

    @Test
    void fromValueThrowsWhenValueIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> SendAttachmentMode.fromValue("   "));
    }

    @Test
    void fromValueThrowsWhenAttachmentTypeIsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> SendAttachmentMode.fromValue("COVERPAGE|UNKNOWN"));
    }
}

