package io.milvus.v2.service.utility;

import io.milvus.v2.BaseTest;
import io.milvus.v2.service.utility.request.AlterAliasReq;
import io.milvus.v2.service.utility.request.CreateAliasReq;
import io.milvus.v2.service.utility.request.DropAliasReq;
import io.milvus.v2.service.utility.response.DescribeAliasResp;
import io.milvus.v2.service.utility.response.ListAliasResp;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UtilityTest extends BaseTest {
    Logger logger = LoggerFactory.getLogger(UtilityTest.class);

    @Test
    void testCreateAlias() {
        CreateAliasReq req = CreateAliasReq.builder()
                .collectionName("test")
                .alias("test_alias")
                .build();
        client_v2.createAlias(req);
    }

    @Test
    void testDropAlias() {
        DropAliasReq req = DropAliasReq.builder()
                .alias("test_alias")
                .build();
        client_v2.dropAlias(req);
    }
    @Test
    void testAlterAlias() {
        AlterAliasReq req = AlterAliasReq.builder()
                .collectionName("test")
                .alias("test_alias")
                .build();
        client_v2.alterAlias(req);
    }

    @Test
    void describeAlias() {
        DescribeAliasResp statusR = client_v2.describeAlias("test_alias");
    }
    @Test
    void listAliases() {
        ListAliasResp statusR = client_v2.listAliases();
    }
}