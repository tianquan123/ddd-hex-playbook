package __DDD_BASE_PACKAGE__.application.sampleorder.command;

import lombok.Value;

@Value
public class CreateSampleOrderCommand {
    String productCode;
    int quantity;
}
