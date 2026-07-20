package __DDD_BASE_PACKAGE__.domain.sampleorder.model;

import __DDD_BASE_PACKAGE__.domain.sampleorder.exception.InvalidSampleOrderStateException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class SampleOrder {

    private final SampleOrderId id;
    private final String productCode;
    private final int quantity;
    private SampleOrderStatus status;
    private final LocalDateTime createTime;
    private final LocalDateTime updateTime;

    private SampleOrder(
            SampleOrderId id,
            String productCode,
            int quantity,
            SampleOrderStatus status,
            LocalDateTime createTime,
            LocalDateTime updateTime
    ) {
        this.id = Objects.requireNonNull(id, "sampleOrderId must not be null");
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("productCode must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.productCode = productCode;
        this.quantity = quantity;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public static SampleOrder create(SampleOrderId id, String productCode, int quantity) {
        return new SampleOrder(id, productCode, quantity, SampleOrderStatus.PENDING, null, null);
    }

    public static SampleOrder reconstitute(
            SampleOrderId id,
            String productCode,
            int quantity,
            SampleOrderStatus status,
            LocalDateTime createTime,
            LocalDateTime updateTime
    ) {
        return new SampleOrder(id, productCode, quantity, status, createTime, updateTime);
    }

    public void confirm() {
        requirePending(SampleOrderStatus.CONFIRMED);
        status = SampleOrderStatus.CONFIRMED;
    }

    public void cancel() {
        requirePending(SampleOrderStatus.CANCELLED);
        status = SampleOrderStatus.CANCELLED;
    }

    private void requirePending(SampleOrderStatus target) {
        if (status != SampleOrderStatus.PENDING) {
            throw new InvalidSampleOrderStateException(id, status, target);
        }
    }
}
