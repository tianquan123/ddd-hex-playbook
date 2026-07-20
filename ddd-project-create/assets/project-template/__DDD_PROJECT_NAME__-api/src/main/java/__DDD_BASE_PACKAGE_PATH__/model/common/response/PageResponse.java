package __DDD_BASE_PACKAGE__.model.common.response;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class PageResponse<T> implements Serializable {

    private final long total;
    private final int pageNum;
    private final int pageSize;
    private final List<T> records;

    public PageResponse(long total, int pageNum, int pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = List.copyOf(records);
    }
}
