package __DDD_BASE_PACKAGE__.facade.sample;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateSampleOrderRequest {

    @NotBlank
    private String productCode;

    @Positive
    private int quantity;
}
