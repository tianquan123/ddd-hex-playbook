package __DDD_BASE_PACKAGE__.application.sampleorder.service.impl;

import __DDD_BASE_PACKAGE__.application.sampleorder.model.PageView;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.application.sampleorder.port.SampleOrderQueryPort;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderCriteria;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderPageCriteria;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
public class SampleOrderQueryServiceImpl implements SampleOrderQueryService {

    private final SampleOrderQueryPort queryPort;

    @Override
    @Transactional(readOnly = true)
    public Optional<SampleOrderView> get(SampleOrderCriteria criteria) {
        return queryPort.find(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public PageView<SampleOrderView> page(SampleOrderPageCriteria criteria) {
        return queryPort.page(criteria);
    }
}
