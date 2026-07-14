package __DDD_BASE_PACKAGE__.facade.sample;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateSampleOrderResponse {

    private final String id;
    private final String status;
}
