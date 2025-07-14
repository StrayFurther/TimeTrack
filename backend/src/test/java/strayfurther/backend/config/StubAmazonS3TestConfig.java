package strayfurther.backend.config;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import strayfurther.backend.config.stub.StubAmazonS3;

@TestConfiguration
public class StubAmazonS3TestConfig {

    @Bean
    public AmazonS3 amazonS3() {
        return new StubAmazonS3();
    }
}