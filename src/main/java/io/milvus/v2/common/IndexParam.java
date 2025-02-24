package io.milvus.v2.common;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class IndexParam {
    @NonNull
    private String fieldName;
    private String indexName;
    @Builder.Default
    private IndexType indexType = IndexType.AUTOINDEX;
    private MetricType metricType;

    public String getIndexName() {
        if(indexName == null) {
            return fieldName;
        }
        return indexName;
    }

    public enum MetricType {
        INVALID,
        // Only for float vectors
        L2,
        IP,
        COSINE,

        // Only for binary vectors
        HAMMING,
        JACCARD,
        ;
    }

    @Getter
    public enum IndexType {
        INVALID,
        //Only supported for float vectors
        FLAT(1),
        IVF_FLAT(2),
        IVF_SQ8(3),
        IVF_PQ(4),
        HNSW(5),
        DISKANN(10),
        AUTOINDEX(11),
        SCANN(12),

        // GPU index
        GPU_IVF_FLAT(50),
        GPU_IVF_PQ(51),

        //Only supported for binary vectors
        BIN_FLAT(80),
        BIN_IVF_FLAT(81),

        //Scalar field index start from here
        //Only for varchar type field
        TRIE("Trie", 100),
        //Only for scalar type field
        STL_SORT(200),
        ;
        private final String name;
        private final int code;

        IndexType(){
            this.name = this.toString();
            this.code = this.ordinal();
        }

        IndexType(int code){
            this.name = this.toString();
            this.code = code;
        }

        IndexType(String name, int code){
            this.name = name;
            this.code = code;
        }
    }
}
