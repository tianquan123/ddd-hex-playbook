package __DDD_BASE_PACKAGE__.domain.sample.model;

import lombok.Value;

@Value
public class SampleOrderId {

    String value;

    public SampleOrderId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("sample order id must not be blank");
        }
        this.value = value;
    }
}
