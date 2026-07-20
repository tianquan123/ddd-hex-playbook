package __DDD_BASE_PACKAGE__.domain.sampleorder.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public final class SampleOrderId {

    private final String value;

    public SampleOrderId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("sampleOrderId must not be blank");
        }
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
