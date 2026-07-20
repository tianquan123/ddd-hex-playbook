package __DDD_BASE_PACKAGE__.domain.sampleorder.exception;

import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderId;

public class SampleOrderNotFoundException extends RuntimeException {

    public SampleOrderNotFoundException(SampleOrderId id) {
        super("Sample order not found: " + id.getValue());
    }
}
