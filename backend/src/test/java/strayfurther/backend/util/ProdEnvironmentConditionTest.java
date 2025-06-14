package strayfurther.backend.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProdEnvironmentConditionTest {

    @Test
    void shouldMatchWhenProfileIsProd() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.profiles.active", "prod");

        ConditionContext mockContext = mock(ConditionContext.class);
        when(mockContext.getEnvironment()).thenReturn(mockEnvironment);

        ProdEnvironmentCondition condition = new ProdEnvironmentCondition();
        boolean matches = condition.matches(mockContext, mock(AnnotatedTypeMetadata.class));

        assertTrue(matches, "Condition should match when the active profile is 'prod'");
    }

    @Test
    void shouldNotMatchWhenProfileIsNotProd() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.profiles.active", "dev");

        ConditionContext mockContext = mock(ConditionContext.class);
        when(mockContext.getEnvironment()).thenReturn(mockEnvironment);

        ProdEnvironmentCondition condition = new ProdEnvironmentCondition();
        boolean matches = condition.matches(mockContext, mock(AnnotatedTypeMetadata.class));

        assertFalse(matches, "Condition should not match when the active profile is not 'prod'");
    }

    @Test
    void shouldNotMatchWhenProfileIsNull() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        // Do not set the "spring.profiles.active" property to simulate a null value

        ConditionContext mockContext = mock(ConditionContext.class);
        when(mockContext.getEnvironment()).thenReturn(mockEnvironment);

        ProdEnvironmentCondition condition = new ProdEnvironmentCondition();
        boolean matches = condition.matches(mockContext, mock(AnnotatedTypeMetadata.class));

        assertFalse(matches, "Condition should not match when the active profile is null or not set");
    }
}