package __DDD_BASE_PACKAGE__.application.sampleorder.service;

import __DDD_BASE_PACKAGE__.application.sampleorder.model.PageView;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderCriteria;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderPageCriteria;

import java.util.Optional;

public interface SampleOrderQueryService {
    Optional<SampleOrderView> get(SampleOrderCriteria criteria);

    PageView<SampleOrderView> page(SampleOrderPageCriteria criteria);
}
