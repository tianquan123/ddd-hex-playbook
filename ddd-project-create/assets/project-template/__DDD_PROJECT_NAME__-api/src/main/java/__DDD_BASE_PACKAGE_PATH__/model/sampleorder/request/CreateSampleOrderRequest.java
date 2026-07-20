package __DDD_BASE_PACKAGE__.model.sampleorder.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class CreateSampleOrderRequest implements Serializable {

    @NotBlank
    private String productCode;

    @Positive
    private int quantity;
}
