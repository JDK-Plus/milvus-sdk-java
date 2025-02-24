package io.milvus.v2.service.vector;

import io.milvus.grpc.*;
import io.milvus.response.DescCollResponseWrapper;
import io.milvus.v2.service.BaseService;
import io.milvus.v2.service.collection.CollectionService;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.index.IndexService;
import io.milvus.v2.service.index.request.DescribeIndexReq;
import io.milvus.v2.service.index.response.DescribeIndexResp;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VectorService extends BaseService {
    Logger logger = LoggerFactory.getLogger(VectorService.class);
    public CollectionService collectionService = new CollectionService();
    public IndexService indexService = new IndexService();

    public InsertResp insert(MilvusServiceGrpc.MilvusServiceBlockingStub blockingStub, InsertReq request) {
        String title = String.format("InsertRequest collectionName:%s", request.getCollectionName());
        checkCollectionExist(blockingStub, request.getCollectionName());
        DescribeCollectionRequest describeCollectionRequest = DescribeCollectionRequest.newBuilder()
                .setCollectionName(request.getCollectionName()).build();
        DescribeCollectionResponse descResp = blockingStub.describeCollection(describeCollectionRequest);

        MutationResult response = blockingStub.insert(dataUtils.convertGrpcInsertRequest(request, new DescCollResponseWrapper(descResp)));
        rpcUtils.handleResponse(title, response.getStatus());
        return InsertResp.builder()
                .InsertCnt(response.getInsertCnt())
                .build();
    }

    public UpsertResp upsert(MilvusServiceGrpc.MilvusServiceBlockingStub milvusServiceBlockingStub, UpsertReq request) {
        String title = String.format("UpsertRequest collectionName:%s", request.getCollectionName());

        checkCollectionExist(milvusServiceBlockingStub, request.getCollectionName());

        DescribeCollectionRequest describeCollectionRequest = DescribeCollectionRequest.newBuilder()
                .setCollectionName(request.getCollectionName()).build();
        DescribeCollectionResponse descResp = milvusServiceBlockingStub.describeCollection(describeCollectionRequest);

        MutationResult response = milvusServiceBlockingStub.upsert(dataUtils.convertGrpcUpsertRequest(request, new DescCollResponseWrapper(descResp)));
        rpcUtils.handleResponse(title, response.getStatus());
        return UpsertResp.builder()
                .upsertCnt(response.getInsertCnt())
                .build();
    }

    public QueryResp query(MilvusServiceGrpc.MilvusServiceBlockingStub milvusServiceBlockingStub, QueryReq request) {
        String title = String.format("QueryRequest collectionName:%s", request.getCollectionName());
        checkCollectionExist(milvusServiceBlockingStub, request.getCollectionName());
        DescribeCollectionResp descR = collectionService.describeCollection(milvusServiceBlockingStub, DescribeCollectionReq.builder().collectionName(request.getCollectionName()).build());
        if(request.getOutputFields() == null){
            request.setOutputFields(descR.getFieldNames());
        }
        if(request.getIds() != null){
            request.setFilter(vectorUtils.getExprById(descR.getPrimaryFieldName(), request.getIds()));
        }
        QueryResults response = milvusServiceBlockingStub.query(vectorUtils.ConvertToGrpcQueryRequest(request));
        rpcUtils.handleResponse(title, response.getStatus());

        return QueryResp.builder()
                .queryResults(convertUtils.getEntities(response))
                .build();

    }

    public SearchResp search(MilvusServiceGrpc.MilvusServiceBlockingStub milvusServiceBlockingStub, SearchReq request) {
        String title = String.format("SearchRequest collectionName:%s", request.getCollectionName());

        checkCollectionExist(milvusServiceBlockingStub, request.getCollectionName());
        DescribeCollectionResp descR = collectionService.describeCollection(milvusServiceBlockingStub, DescribeCollectionReq.builder().collectionName(request.getCollectionName()).build());
        if (request.getVectorFieldName() == null) {
            request.setVectorFieldName(descR.getVectorFieldName().get(0));
        }
        if(request.getOutputFields() == null){
            request.setOutputFields(descR.getFieldNames());
        }
        DescribeIndexReq describeIndexReq = DescribeIndexReq.builder()
                .collectionName(request.getCollectionName())
                .fieldName(request.getVectorFieldName())
                .build();
        DescribeIndexResp respR = indexService.describeIndex(milvusServiceBlockingStub, describeIndexReq);

        SearchRequest searchRequest = vectorUtils.ConvertToGrpcSearchRequest(respR.getMetricType(), request);

        SearchResults response = milvusServiceBlockingStub.search(searchRequest);
        rpcUtils.handleResponse(title, response.getStatus());

        return SearchResp.builder()
                .searchResults(convertUtils.getEntities(response))
                .build();
    }

    public DeleteResp delete(MilvusServiceGrpc.MilvusServiceBlockingStub milvusServiceBlockingStub, DeleteReq request) {
        String title = String.format("DeleteRequest collectionName:%s", request.getCollectionName());
        checkCollectionExist(milvusServiceBlockingStub, request.getCollectionName());
        DescribeCollectionResp respR = collectionService.describeCollection(milvusServiceBlockingStub, DescribeCollectionReq.builder().collectionName(request.getCollectionName()).build());
        if(request.getExpr() == null){
            request.setExpr(vectorUtils.getExprById(respR.getPrimaryFieldName(), request.getIds()));
        }
        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
                .setCollectionName(request.getCollectionName())
                .setPartitionName(request.getPartitionName())
                .setExpr(request.getExpr())
                .build();
        MutationResult response = milvusServiceBlockingStub.delete(deleteRequest);
        rpcUtils.handleResponse(title, response.getStatus());
        return DeleteResp.builder()
                .deleteCnt(response.getDeleteCnt())
                .build();
    }

    public GetResp get(MilvusServiceGrpc.MilvusServiceBlockingStub milvusServiceBlockingStub, GetReq request) {
        String title = String.format("GetRequest collectionName:%s", request.getCollectionName());
        logger.debug(title);
        QueryReq queryReq = QueryReq.builder()
                .collectionName(request.getCollectionName())
                .ids(request.getIds())
                .build();
        QueryResp queryResp = query(milvusServiceBlockingStub, queryReq);

        return GetResp.builder()
                .getResults(queryResp.getQueryResults())
                .build();
    }
}
