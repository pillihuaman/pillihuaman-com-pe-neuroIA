package pillihuaman.com.pe.neuroIA.dto;

import lombok.Data;
import java.util.List;

@Data
public class AwsResourcesSummary {
    private List<Ec2Instance> ec2Instances;
    private List<String> s3Buckets;
    private List<CloudFrontDistribution> cloudFrontDistributions;
    private List<RdsInstance> rdsInstances;
    private List<String> lambdaFunctions;
    private List<String> dynamoDbTables;

    @Data
    public static class Ec2Instance {
        private String id;
        private String type;
        private String state;
        private String ip;
    }

    @Data
    public static class CloudFrontDistribution {
        private String id;
        private String domain;
    }

    @Data
    public static class RdsInstance {
        private String identifier;
        private String engine;
        private String status;
    }
}
