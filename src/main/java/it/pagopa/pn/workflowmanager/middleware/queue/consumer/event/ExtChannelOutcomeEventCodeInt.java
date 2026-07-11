package it.pagopa.pn.workflowmanager.middleware.queue.consumer.event;

import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import lombok.Getter;

/**
 * Enum that merges event codes from LegalMessageSentDetails and CourtesyMessageProgressEvent.
 * Maps each event code to its corresponding channel type.
 */
@Getter
public enum ExtChannelOutcomeEventCodeInt {
    // codici in arrivo da ext-Channel (C) con/senza busta indica se lo stato contiene allegati
    C000("C000", ChannelType.PEC), // COMUNICAZIONE CON SERVER PEC AVVENUTA  (senza busta)
    C001("C001", ChannelType.PEC), // StatusPec.ACCETTAZIONE  (con busta)
    C002("C002", ChannelType.PEC), // StatusPec.NON_ACCETTAZIONE  (con busta)
    C003("C003", ChannelType.PEC), // StatusPec.AVVENUTA_CONSEGNA  (con busta)
    C004("C004", ChannelType.PEC), // StatusPec.ERRORE_CONSEGNA (con busta)
    C005("C005", ChannelType.PEC), // StatusPec.PRESA_IN_CARICO  (senza busta)
    C006("C006", ChannelType.PEC), // StatusPec.RILEVAZIONE_VIRUS (con busta)
    C007("C007", ChannelType.PEC), // StatusPec.PREAVVISO_ERRORE_CONSEGNA  (senza busta)
    C008("C008", ChannelType.PEC), // StatusPec.ERRORE_COMUNICAZIONE_SERVER_PEC  - con retry da parte di PN (senza busta)
    C009("C009", ChannelType.PEC), // StatusPec.ERRORE_DOMINIO_PEC_NON_VALIDO - senza retry:  indica un dominio pec non valido; (senza busta)
    C010("C010", ChannelType.PEC), // StatusPec.ERROR_INVIO_PEC - con retry da parte di PN: indica un errore generico di invio pec (senza busta)
    C011("C011", ChannelType.PEC), // PEC - ADDRESS_ERROR

    // EMAIL events (Courtesy channel) - start with M
    M003("M003", ChannelType.EMAIL), // SENT
    M004("M004", ChannelType.EMAIL), // DELIVERED
    M005("M005", ChannelType.EMAIL), // BOUNCED
    M006("M006", ChannelType.EMAIL), // SPAM
    M008("M008", ChannelType.EMAIL), // ERROR
    M009("M009", ChannelType.EMAIL), // REFUSED
    M010("M010", ChannelType.EMAIL), // INTERNAL ERROR
    M011("M011", ChannelType.EMAIL), // SYSTEM ERROR

    // SMS events (Courtesy channel) - start with S
    S003("S003", ChannelType.SMS), // SENT
    S008("S008", ChannelType.SMS), // ERROR
    S010("S010", ChannelType.SMS); // ERROR

    private final String value;
    private final ChannelType channel;

    ExtChannelOutcomeEventCodeInt(String value, ChannelType channel) {
        this.value = value;
        this.channel = channel;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public ChannelType getChannelType() {
        return channel;
    }

    /**
     * Get ExtChannelOutcomeEventCodeInt from string value
     * @param value the event code
     * @return the corresponding enum value
     */
    public static ExtChannelOutcomeEventCodeInt fromValue(String value) {
        for (ExtChannelOutcomeEventCodeInt code : ExtChannelOutcomeEventCodeInt.values()) {
            if (code.value.equals(value)) {
                return code;
            }
        }
        throw new IllegalArgumentException("Unknown event code: " + value);
    }
}

