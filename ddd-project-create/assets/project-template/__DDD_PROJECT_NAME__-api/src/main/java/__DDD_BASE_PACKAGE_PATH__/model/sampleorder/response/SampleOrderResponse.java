package __DDD_BASE_PACKAGE__.model.sampleorder.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleOrderResponse implements Serializable {

    private String id;
    private String productCode;
    private int quantity;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
