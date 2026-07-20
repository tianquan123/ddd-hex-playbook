package __DDD_BASE_PACKAGE__.application.sampleorder.query;

import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderStatus;
import lombok.Getter;

@Getter
public class SampleOrderPageCriteria {

    private final SampleOrderStatus status;
    private final int pageNum;
    private final int pageSize;

    public SampleOrderPageCriteria(SampleOrderStatus status, int pageNum, int pageSize) {
        if (pageNum <= 0) {
            throw new IllegalArgumentException("pageNum must be positive");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be positive");
        }
        this.status = status;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public long getOffset() {
        return (long) (pageNum - 1) * pageSize;
    }
}
