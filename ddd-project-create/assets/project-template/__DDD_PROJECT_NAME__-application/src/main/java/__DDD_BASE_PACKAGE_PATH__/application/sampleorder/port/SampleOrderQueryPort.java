package __DDD_BASE_PACKAGE__.application.sampleorder.port;

import __DDD_BASE_PACKAGE__.application.sampleorder.model.PageView;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderCriteria;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderPageCriteria;

import java.util.Optional;

public interface SampleOrderQueryPort {
    Optional<SampleOrderView> find(SampleOrderCriteria criteria);

    PageView<SampleOrderView> page(SampleOrderPageCriteria criteria);
}
