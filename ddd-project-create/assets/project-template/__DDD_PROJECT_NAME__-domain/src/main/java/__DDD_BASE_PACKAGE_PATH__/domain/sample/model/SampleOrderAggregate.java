package __DDD_BASE_PACKAGE__.domain.sample.model;

import java.util.Objects;

public final class SampleOrderAggregate {

    private final SampleOrderId id;
    private final String productCode;
    private final int quantity;
    private final SampleOrderStatus status;

    private SampleOrderAggregate(
            SampleOrderId id,
            String productCode,
            int quantity,
            SampleOrderStatus status) {
        this.id = Objects.requireNonNull(id, "id");
        this.productCode = productCode;
        this.quantity = quantity;
        this.status = Objects.requireNonNull(status, "status");
    }

    public static SampleOrderAggregate create(SampleOrderId id, String productCode, int quantity) {
        validateDetails(productCode, quantity);
        return new SampleOrderAggregate(id, productCode, quantity, SampleOrderStatus.CREATED);
    }

    public static SampleOrderAggregate rehydrate(
            SampleOrderId id,
            String productCode,
            int quantity,
            SampleOrderStatus status) {
        validateDetails(productCode, quantity);
        return new SampleOrderAggregate(id, productCode, quantity, status);
    }

    private static void validateDetails(String productCode, int quantity) {
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("product code must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    public SampleOrderId id() {
        return id;
    }

    public String productCode() {
        return productCode;
    }

    public int quantity() {
        return quantity;
    }

    public SampleOrderStatus status() {
        return status;
    }
}
