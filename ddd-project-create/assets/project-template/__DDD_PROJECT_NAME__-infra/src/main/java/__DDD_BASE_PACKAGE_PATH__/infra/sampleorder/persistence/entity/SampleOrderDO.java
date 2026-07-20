package __DDD_BASE_PACKAGE__.infra.sampleorder.persistence.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SampleOrderDO {
    private String id;
    private String productCode;
    private int quantity;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
