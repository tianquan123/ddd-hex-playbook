package __DDD_BASE_PACKAGE__.application.sampleorder.model;

import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleOrderView {
    private String id;
    private String productCode;
    private int quantity;
    private SampleOrderStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
