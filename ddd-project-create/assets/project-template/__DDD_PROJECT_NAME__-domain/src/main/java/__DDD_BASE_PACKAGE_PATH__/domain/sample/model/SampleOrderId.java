package __DDD_BASE_PACKAGE__.domain.sample.model;

public record SampleOrderId(String value) {

    public SampleOrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("sample order id must not be blank");
        }
    }
}
