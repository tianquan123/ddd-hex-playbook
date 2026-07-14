package __DDD_BASE_PACKAGE__.infra.sample.persistence;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SampleOrderPO {

    private String id;
    private String productCode;
    private int quantity;
    private String status;
}
