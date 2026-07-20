package __DDD_BASE_PACKAGE__.domain.sampleorder.exception;

import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderStatus;

public class InvalidSampleOrderStateException extends RuntimeException {

    public InvalidSampleOrderStateException(
            SampleOrderId id,
            SampleOrderStatus current,
            SampleOrderStatus target
    ) {
        super("Cannot transition sample order " + id.getValue() + " from " + current + " to " + target);
    }
}
