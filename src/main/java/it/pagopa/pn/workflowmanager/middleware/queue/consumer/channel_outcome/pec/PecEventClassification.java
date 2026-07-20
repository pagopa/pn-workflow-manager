package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.pec;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeClassification;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import lombok.Getter;

import java.util.Optional;

/**
 * Enum that classifies PEC events and implements ChannelOutcomeClassification.
 * Handles different scenarios based on the event code
 */
@Getter
public enum PecEventClassification implements ChannelOutcomeClassification {
    // codici in arrivo da ext-Channel (C) con/senza busta indica se lo stato contiene allegati
    C000(false, ChannelOutcomeCategory.progress(), null), // COMUNICAZIONE CON SERVER PEC AVVENUTA  (senza busta)
    C001(false, ChannelOutcomeCategory.progress(), DesiredFeedbackType.SENT), // StatusPec.ACCETTAZIONE  (con busta)
    C002(false, ChannelOutcomeCategory.negativeFeedback(), null), // StatusPec.NON_ACCETTAZIONE  (con busta)
    C003(true, ChannelOutcomeCategory.positiveFeedback(false), DesiredFeedbackType.RECEIVED), // StatusPec.AVVENUTA_CONSEGNA  (con busta)
    C004(false, ChannelOutcomeCategory.negativeFeedback(), null), // StatusPec.ERRORE_CONSEGNA (con busta)
    C005(false, ChannelOutcomeCategory.progress(), null), // StatusPec.PRESA_IN_CARICO  (senza busta)
    C006(false, ChannelOutcomeCategory.negativeFeedback(), null), // StatusPec.RILEVAZIONE_VIRUS (con busta)
    C007(false, ChannelOutcomeCategory.progress(), null), // StatusPec.PREAVVISO_ERRORE_CONSEGNA  (senza busta)
    C008(false, ChannelOutcomeCategory.negativeFeedback(), null), // StatusPec.ERRORE_COMUNICAZIONE_SERVER_PEC  - con retry da parte di PN (senza busta)
    C009(false, ChannelOutcomeCategory.negativeFeedback(), null), // StatusPec.ERRORE_DOMINIO_PEC_NON_VALIDO - senza retry:  indica un dominio pec non valido; (senza busta)
    C010(false, ChannelOutcomeCategory.negativeFeedback(), null), // StatusPec.ERROR_INVIO_PEC - con retry da parte di PN: indica un errore generico di invio pec (senza busta)
    C011(false, ChannelOutcomeCategory.negativeFeedback(), null); // PEC - ADDRESS_ERROR


    private final ChannelOutcomeCategory category;
    private final boolean recipientReached;
    private final DesiredFeedbackType desiredFeedback;

    PecEventClassification(boolean recipientReached, ChannelOutcomeCategory category, DesiredFeedbackType desiredFeedback) {
        this.recipientReached = recipientReached;
        this.category = category;
        this.desiredFeedback = desiredFeedback;
    }

    @Override
    public Optional<DesiredFeedbackType> getSatisfiedDesiredFeedback() {
        return Optional.ofNullable(desiredFeedback);
    }

    /**
     * Get classification from event code
     * @param eventCode the PEC event code
     * @return the corresponding PecEventClassification
     * @throws IllegalArgumentException if the event code is not recognized
     */
    public static PecEventClassification fromEventCode(String eventCode) {
        try {
            return PecEventClassification.valueOf(eventCode);
        } catch (IllegalArgumentException ex) {
            throw new PnUnknownEventCodeException(eventCode, ChannelType.PEC);
        }
    }
}

