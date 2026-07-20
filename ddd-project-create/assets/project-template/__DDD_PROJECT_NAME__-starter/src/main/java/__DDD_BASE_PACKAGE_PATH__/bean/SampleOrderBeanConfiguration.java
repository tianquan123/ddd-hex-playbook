package __DDD_BASE_PACKAGE__.bean;

import __DDD_BASE_PACKAGE__.application.sampleorder.port.SampleOrderQueryPort;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderAppService;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderQueryService;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.impl.SampleOrderAppServiceImpl;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.impl.SampleOrderQueryServiceImpl;
import __DDD_BASE_PACKAGE__.domain.sampleorder.repository.SampleOrderRepository;
import __DDD_BASE_PACKAGE__.infra.sampleorder.adapter.SampleOrderPersistenceAdapter;
import __DDD_BASE_PACKAGE__.infra.sampleorder.persistence.mapper.SampleOrderMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SampleOrderBeanConfiguration {

    @Bean
    public SampleOrderPersistenceAdapter sampleOrderPersistenceAdapter(SampleOrderMapper mapper) {
        return new SampleOrderPersistenceAdapter(mapper);
    }

    @Bean
    public SampleOrderAppService sampleOrderAppService(SampleOrderRepository repository) {
        return new SampleOrderAppServiceImpl(repository);
    }

    @Bean
    public SampleOrderQueryService sampleOrderQueryService(SampleOrderQueryPort queryPort) {
        return new SampleOrderQueryServiceImpl(queryPort);
    }
}
