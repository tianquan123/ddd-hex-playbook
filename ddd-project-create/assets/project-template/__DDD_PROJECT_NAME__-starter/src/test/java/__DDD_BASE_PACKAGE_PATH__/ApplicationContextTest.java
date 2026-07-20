package __DDD_BASE_PACKAGE__;

import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderAppService;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderQueryService;
import __DDD_BASE_PACKAGE__.domain.sampleorder.repository.SampleOrderRepository;
import __DDD_BASE_PACKAGE__.infra.sampleorder.adapter.SampleOrderPersistenceAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = __DDD_PROJECT_CLASS__Application.class)
@ActiveProfiles("test")
class ApplicationContextTest {

    @Autowired
    private SampleOrderAppService appService;

    @Autowired
    private SampleOrderQueryService queryService;

    @Autowired
    private SampleOrderRepository repository;

    @Test
    void composes_explicit_application_and_persistence_beans() {
        assertNotNull(appService);
        assertNotNull(queryService);
        assertInstanceOf(SampleOrderPersistenceAdapter.class, repository);
    }
}
