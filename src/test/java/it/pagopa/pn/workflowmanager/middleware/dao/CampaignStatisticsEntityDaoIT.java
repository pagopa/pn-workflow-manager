package it.pagopa.pn.workflowmanager.middleware.dao;

import it.pagopa.pn.workflowmanager.BaseTest;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.CampaignStatisticsEntityDao;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.entity.CampaignStatisticsEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude=io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration"
        }
)
@ActiveProfiles("test")
class CampaignStatisticsEntityDaoIT extends BaseTest.WithLocalStack {

    @Autowired
    CampaignStatisticsEntityDao campaignStatisticsEntityDao;

    @Test
    void getStream() {
        campaignStatisticsEntityDao.save(new CampaignStatisticsEntity("campaignId1")).block();
        campaignStatisticsEntityDao.save(new CampaignStatisticsEntity("campaignId2")).block();
        CampaignStatisticsEntity campaignStatisticsEntityOne = campaignStatisticsEntityDao.get("campaignId1").block();
        CampaignStatisticsEntity campaignStatisticsEntityTwo = campaignStatisticsEntityDao.get("campaignId2").block();

        assert campaignStatisticsEntityOne != null;
        assert campaignStatisticsEntityTwo != null;
        Assertions.assertEquals("campaignId1", campaignStatisticsEntityOne.getCampaignId());
        Assertions.assertEquals("campaignId2", campaignStatisticsEntityTwo.getCampaignId());
    }
}
