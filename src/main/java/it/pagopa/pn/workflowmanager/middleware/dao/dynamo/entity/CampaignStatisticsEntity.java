package it.pagopa.pn.workflowmanager.middleware.dao.dynamo.entity;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * Entity Stream
 */
@DynamoDbBean
@Data
public class CampaignStatisticsEntity {

    public static final String COL_PK = "campaignId";
    private static final String COL_TOTAL_SENT = "totalSent";
    private static final String COL_TOTAL_REFUSED = "totalRefused";
    private static final String COL_TOTAL_UNDELIVERABLE = "totalUndeliverable";
    private static final String COL_TOTAL_REACHED = "totalReached";
    private static final String COL_WORKFLOW_DONE = "workflowDone";

    private static final String COL_DIGITAL_SENT_IO = "digitalSent_IO";
    private static final String COL_DIGITAL_SENT_EMAIL = "digitalSent_EMAIL";
    private static final String COL_DIGITAL_SENT_PEC = "digitalSent_PEC";
    private static final String COL_DIGITAL_SENT_SMS = "digitalSent_SMS";
    private static final String COL_ANALOG_SENT_RS = "analogSent_RS";

    private static final String COL_RECEIVED_IO = "received_IO";
    private static final String COL_RECEIVED_EMAIL = "received_EMAIL";
    private static final String COL_RECEIVED_PEC = "received_PEC";
    private static final String COL_RECEIVED_RS = "received_RS";

    private static final String COL_VIEWED = "viewed";
    private static final String COL_PAYED = "payed";
    private static final String COL_LAST_COMPLETED_TIMESTAMP = "lastCompletedTimestamp";

    public CampaignStatisticsEntity() {}

    public CampaignStatisticsEntity(String campaignId) {
        this.setCampaignId(campaignId);
    }

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)}))
    private String campaignId;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_TOTAL_SENT)}))
    private Integer totalSent;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_TOTAL_REFUSED)}))
    private Integer totalRefused;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_TOTAL_UNDELIVERABLE)}))
    private Integer totalUndeliverable;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_TOTAL_REACHED)}))
    private Integer totalReached;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_WORKFLOW_DONE)}))
    private Integer workflowDone;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DIGITAL_SENT_IO)}))
    private Integer digitalSentIO;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DIGITAL_SENT_EMAIL)}))
    private Integer digitalSentEMAIL;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DIGITAL_SENT_PEC)}))
    private Integer digitalSentPEC;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DIGITAL_SENT_SMS)}))
    private Integer digitalSentSMS;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_ANALOG_SENT_RS)}))
    private Integer analogSentRS;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_RECEIVED_IO)}))
    private Integer receivedIO;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_RECEIVED_EMAIL)}))
    private Integer receivedEMAIL;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_RECEIVED_PEC)}))
    private Integer receivedPEC;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_RECEIVED_RS)}))
    private Integer receivedRS;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_VIEWED)}))
    private Integer viewed;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_PAYED)}))
    private Integer payed;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_LAST_COMPLETED_TIMESTAMP)}))
    private String lastCompletedTimestamp;
}
