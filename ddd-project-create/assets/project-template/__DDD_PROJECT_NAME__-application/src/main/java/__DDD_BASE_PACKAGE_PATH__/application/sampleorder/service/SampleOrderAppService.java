package __DDD_BASE_PACKAGE__.application.sampleorder.service;

import __DDD_BASE_PACKAGE__.application.sampleorder.command.CancelSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.command.ConfirmSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.command.CreateSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;

public interface SampleOrderAppService {
    SampleOrderView create(CreateSampleOrderCommand command);

    SampleOrderView confirm(ConfirmSampleOrderCommand command);

    SampleOrderView cancel(CancelSampleOrderCommand command);
}
