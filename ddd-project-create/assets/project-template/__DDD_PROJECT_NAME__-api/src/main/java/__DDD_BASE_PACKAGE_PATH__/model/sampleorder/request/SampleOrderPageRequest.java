package __DDD_BASE_PACKAGE__.model.sampleorder.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class SampleOrderPageRequest implements Serializable {

    private String status;

    @Positive
    private int pageNum = 1;

    @Positive
    private int pageSize = 20;
}
