package strayfurther.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(ProdEnvironmentCondition.class)
public class AwsS3Config {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Bean
    public Object amazonS3() {
        if (isProdEnvironment()) {
            // Use actual AWS SDK
            com.amazonaws.auth.BasicAWSCredentials awsCredentials =
                    new com.amazonaws.auth.BasicAWSCredentials(accessKey, secretKey);
            return com.amazonaws.services.s3.AmazonS3ClientBuilder.standard()
                    .withRegion(region)
                    .withCredentials(new com.amazonaws.auth.AWSStaticCredentialsProvider(awsCredentials))
                    .build();
        } else {
            // Use stub implementation
            return new strayfurther.backend.config.stub.StubAmazonS3();
        }
    }

    private boolean isProdEnvironment() {
        // Logic to determine if the environment is production
        return "prod".equals(System.getProperty("env"));
    }
}