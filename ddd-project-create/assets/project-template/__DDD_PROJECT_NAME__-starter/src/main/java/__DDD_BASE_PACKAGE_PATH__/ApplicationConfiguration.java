package __DDD_BASE_PACKAGE__;

import __DDD_BASE_PACKAGE__.application.sample.SampleOrderAppService;
import __DDD_BASE_PACKAGE__.domain.sample.repository.SampleOrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    SampleOrderAppService sampleOrderAppService(SampleOrderRepository repository) {
        return new SampleOrderAppService(repository);
    }
}
