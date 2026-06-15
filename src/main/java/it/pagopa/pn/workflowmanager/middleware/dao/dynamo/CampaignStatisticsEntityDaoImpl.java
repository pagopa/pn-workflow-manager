package it.pagopa.pn.workflowmanager.middleware.dao.dynamo;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.entity.CampaignStatisticsEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Component
@Slf4j
public class CampaignStatisticsEntityDaoImpl implements CampaignStatisticsEntityDao {

    private final DynamoDbAsyncTable<CampaignStatisticsEntity> table;

    public CampaignStatisticsEntityDaoImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient, PnWorkflowManagerConfigs cfg) {
        this.table = dynamoDbEnhancedClient.table(cfg.getDao().getCampaignStatisticsTableName(), TableSchema.fromBean(CampaignStatisticsEntity.class));
    }

    @Override
    public Mono<CampaignStatisticsEntity> get(String campaignId) {
        Key hashKey = Key.builder().partitionValue(campaignId).build();
        return Mono.fromFuture(table.getItem(hashKey));
    }
}
