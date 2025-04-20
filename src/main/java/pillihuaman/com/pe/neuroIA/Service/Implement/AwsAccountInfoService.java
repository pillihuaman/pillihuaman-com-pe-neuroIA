package pillihuaman.com.pe.neuroIA.Service.Implement;

import org.springframework.stereotype.Service;
import pillihuaman.com.pe.neuroIA.dto.AwsResourcesSummary;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.ListDistributionsResponse;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.ListFunctionsResponse;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Service
public class AwsAccountInfoService {


    private final Ec2Client ec2Client = Ec2Client.create();
    private final S3Client s3Client = S3Client.create();
    private final CloudFrontClient cloudFrontClient = CloudFrontClient.create();
    private final RdsClient rdsClient = RdsClient.create();
    private final LambdaClient lambdaClient = LambdaClient.create();
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private final CostExplorerClient ceClient = CostExplorerClient.create();

    public String describeAllAwsResources() {
        StringBuilder sb = new StringBuilder();

        // EC2
        sb.append(describeInstances());

        // S3
        ListBucketsResponse bucketsResponse = s3Client.listBuckets();
        sb.append("\nS3 Buckets:\n");
        for (Bucket bucket : bucketsResponse.buckets()) {
            sb.append("- ").append(bucket.name()).append("\n");
        }

        // CloudFront
        ListDistributionsResponse cfResponse = cloudFrontClient.listDistributions();
        sb.append("\nCloudFront Distributions:\n");
        if (cfResponse.distributionList() != null && cfResponse.distributionList().items() != null) {
            cfResponse.distributionList().items().forEach(dist ->
                    sb.append("- ID: ").append(dist.id()).append(" | Domain: ").append(dist.domainName()).append("\n")
            );
        }

       /* RDS
        DescribeDBInstancesResponse rdsResponse = rdsClient.describeDBInstances();
        sb.append("\nRDS Instances:\n");
        for (DBInstance db : rdsResponse.dbInstances()) {
            sb.append("- Identifier: ").append(db.dbInstanceIdentifier())
                    .append(" | Engine: ").append(db.engine())
                    .append(" | Status: ").append(db.dbInstanceStatus())
                    .append("\n");
        }
*/
        // Lambda
        ListFunctionsResponse lambdaResponse = lambdaClient.listFunctions();
        sb.append("\nLambda Functions:\n");
        lambdaResponse.functions().forEach(func ->
                sb.append("- ").append(func.functionName()).append("\n")
        );

        // DynamoDB
        ListTablesResponse tablesResponse = dynamoDbClient.listTables();
        sb.append("\nDynamoDB Tables:\n");
        tablesResponse.tableNames().forEach(table ->
                sb.append("- ").append(table).append("\n")
        );

        return sb.toString();
    }

    public String describeInstances() {
        DescribeInstancesResponse response = ec2Client.describeInstances();
        StringBuilder sb = new StringBuilder("EC2 Instances:\n");

        for (Reservation reservation : response.reservations()) {
            for (Instance instance : reservation.instances()) {
                sb.append("ID: ").append(instance.instanceId())
                        .append(" | Type: ").append(instance.instanceTypeAsString())
                        .append(" | State: ").append(instance.state().nameAsString())
                        .append(" | IP: ").append(instance.publicIpAddress())
                        .append("\n");
            }
        }

        return sb.toString();
    }

    public String getBillingLastMonth() {
        LocalDate now = LocalDate.now();
        String start = now.minusMonths(1).withDayOfMonth(1).format(DateTimeFormatter.ISO_DATE);
        String end = now.withDayOfMonth(1).format(DateTimeFormatter.ISO_DATE);

        GetCostAndUsageResponse response = ceClient.getCostAndUsage(GetCostAndUsageRequest.builder()
                .timePeriod(DateInterval.builder().start(start).end(end).build())
                .granularity(Granularity.MONTHLY)
                .metrics("UnblendedCost")
                .build());

        String amount = response.resultsByTime().stream()
                .flatMap(r -> r.total().values().stream())
                .map(MetricValue::amount)
                .findFirst()
                .orElse("0");

        return "Costo del último mes: $" + amount;
    }

    public AwsResourcesSummary getAllAwsResourcesSummaryAsObject() {
        AwsResourcesSummary summary = new AwsResourcesSummary();

        // EC2
        try {
            DescribeInstancesResponse response = ec2Client.describeInstances();
            List<AwsResourcesSummary.Ec2Instance> ec2List = new ArrayList<>();
            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    AwsResourcesSummary.Ec2Instance ec2 = new AwsResourcesSummary.Ec2Instance();
                    ec2.setId(instance.instanceId());
                    ec2.setType(instance.instanceTypeAsString());
                    ec2.setState(instance.state().nameAsString());
                    ec2.setIp(instance.publicIpAddress());
                    ec2List.add(ec2);
                }
            }
            summary.setEc2Instances(ec2List);
        } catch (Exception e) {
            summary.setEc2Instances(List.of());
        }

        // S3
        try {
            List<String> bucketNames = s3Client.listBuckets().buckets()
                    .stream().map(Bucket::name).toList();
            summary.setS3Buckets(bucketNames);
        } catch (Exception e) {
            summary.setS3Buckets(List.of());
        }

        // CloudFront
        try {
            List<AwsResourcesSummary.CloudFrontDistribution> distList = new ArrayList<>();
            var cfResponse = cloudFrontClient.listDistributions();
            if (cfResponse.distributionList() != null && cfResponse.distributionList().items() != null) {
                cfResponse.distributionList().items().forEach(dist -> {
                    var cf = new AwsResourcesSummary.CloudFrontDistribution();
                    cf.setId(dist.id());
                    cf.setDomain(dist.domainName());
                    distList.add(cf);
                });
            }
            summary.setCloudFrontDistributions(distList);
        } catch (Exception e) {
            summary.setCloudFrontDistributions(List.of());
        }

        // RDS
        try {
            List<AwsResourcesSummary.RdsInstance> rdsList = rdsClient.describeDBInstances().dbInstances()
                    .stream().map(db -> {
                        var rds = new AwsResourcesSummary.RdsInstance();
                        rds.setIdentifier(db.dbInstanceIdentifier());
                        rds.setEngine(db.engine());
                        rds.setStatus(db.dbInstanceStatus());
                        return rds;
                    }).toList();
            summary.setRdsInstances(rdsList);
        } catch (Exception e) {
            summary.setRdsInstances(List.of());
        }

        // Lambda
        try {
            List<String> functions = lambdaClient.listFunctions().functions()
                    .stream().map(f -> f.functionName()).toList();
            summary.setLambdaFunctions(functions);
        } catch (Exception e) {
            summary.setLambdaFunctions(List.of());
        }

        // DynamoDB
        try {
            summary.setDynamoDbTables(dynamoDbClient.listTables().tableNames());
        } catch (Exception e) {
            summary.setDynamoDbTables(List.of());
        }

        return summary;
    }
}


