package it.pagopa.pn.workflowmanager.middleware.dao.dynamo;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.entity.CampaignStatisticsEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.*;

import java.util.Optional;

@Component
@Slf4j
public class CampaignStatisticsEntityDaoImpl implements CampaignStatisticsEntityDao {

    private final DynamoDbTable<CampaignStatisticsEntity> table;

    public CampaignStatisticsEntityDaoImpl(DynamoDbEnhancedClient dynamoDbClient, PnWorkflowManagerConfigs cfg) {
        this.table = dynamoDbClient.table(cfg.getDao().getCampaignStatisticsTableName(), TableSchema.fromBean(CampaignStatisticsEntity.class));
    }

    @Override
    public Optional<CampaignStatisticsEntity> get(String senderId, String campaignId) {
        Key key = Key.builder()
                .partitionValue(senderId)
                .sortValue(campaignId)
                .build();
        return Optional.ofNullable(table.getItem(key));
    }

    @Override
    public CampaignStatisticsEntity save(CampaignStatisticsEntity entity) {
        table.putItem(entity);
        return entity;
    }
}
