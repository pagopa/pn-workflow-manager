package it.pagopa.pn.workflowmanager.dto.timeline;

import jakarta.validation.constraints.NotNull;
import javax.annotation.Nullable;

/**
 * Classe builder che permette di costruire un timelineEventId
 * <p>
 * Il formato dello della stringa di input dovrà essere:
 * <CATEGORY_VALUE>;IUN_<IUN_VALUE>;RECINDEX_<RECINDEX_VALUE>...
 * tutti i value sono facoltativi, tranne il campo category.
 * Sarà responsabilità del builder concatenare ogni singolo value alla timelineEventId solo se non gli viene passato null.
 */
public class TimelineEventIdBuilder {
    public static final String DELIMITER = ".";

    private String iun = "";

    private String recIndex = "";

    private String sentAttemptMade = "";

    private String progressIndex = "";

    private String channel = "";

    private String deliveryType = "";

    private String category = "";

    private String paymentCode = "";


    public TimelineEventIdBuilder withIun(@Nullable String iun) {
        if(iun != null)
            this.iun = DELIMITER.concat("IUN_").concat(iun);
        return this;
    }

    public TimelineEventIdBuilder withRecIndex(@Nullable Integer recIndex) {
        if(recIndex != null)
            this.recIndex = DELIMITER.concat("RECINDEX_").concat(recIndex + "");
        return this;
    }

    public TimelineEventIdBuilder withSentAttemptMade(@Nullable Integer sentAttemptMade) {
        if(sentAttemptMade != null && sentAttemptMade >= 0)
            this.sentAttemptMade = DELIMITER.concat("ATTEMPT_").concat(sentAttemptMade + "");
        return this;
    }

    public TimelineEventIdBuilder withCategory(@NotNull String category) {
        this.category = category;
        return this;
    }

    // payment code per pagamenti PagoPa = PPANoticeNumberCreditorTaxId
    public TimelineEventIdBuilder withPaymentCode(@Nullable String paymentCode) {
        if(paymentCode != null)
            this.paymentCode = DELIMITER.concat("CODE_").concat(paymentCode);
        return this;
    }

    public TimelineEventIdBuilder withProgressIndex(@Nullable Integer progressIndex) {
        // se passo un progressindex negativo, è perchè non voglio che venga inserito nell'eventid. Usato per cercare con l'inizia per
        if(progressIndex != null && progressIndex >= 0)
            this.progressIndex = DELIMITER.concat("IDX_").concat(progressIndex + "");
        return this;
    }

    public TimelineEventIdBuilder withChannel(@Nullable String channel) {
        if (channel != null)
            this.channel = DELIMITER.concat("CHANNEL_").concat(channel);
        return this;
    }

    public TimelineEventIdBuilder withDeliveryType(@Nullable String deliveryType) {
        if (deliveryType != null)
            this.deliveryType = DELIMITER.concat("DELIVERYTYPE_").concat(deliveryType);
        return this;
    }


    public String build() {
        return category +
                iun +
                recIndex +
                sentAttemptMade +
                progressIndex +
                channel +
                deliveryType +
                paymentCode;
    }

}
