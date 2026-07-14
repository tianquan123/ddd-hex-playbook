package __DDD_BASE_PACKAGE__.infra.sample.config;

import __DDD_BASE_PACKAGE__.domain.sample.repository.SampleOrderRepository;
import __DDD_BASE_PACKAGE__.infra.sample.persistence.SampleOrderMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleInfrastructureConfiguration {

    @Bean
    SampleOrderRepository sampleOrderRepository(SampleOrderMapper mapper) {
        return new __DDD_BASE_PACKAGE__.infra.sample.adapter.SampleOrderRepository(mapper);
    }
}
