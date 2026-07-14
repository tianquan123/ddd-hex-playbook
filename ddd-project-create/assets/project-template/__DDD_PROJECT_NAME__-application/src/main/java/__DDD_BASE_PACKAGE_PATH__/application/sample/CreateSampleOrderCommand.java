package __DDD_BASE_PACKAGE__.application.sample;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateSampleOrderCommand {

    private final String productCode;
    private final int quantity;
}
