package __DDD_BASE_PACKAGE__.application.sampleorder.model;

import lombok.Getter;

import java.util.List;

@Getter
public class PageView<T> {
    private final long total;
    private final int pageNum;
    private final int pageSize;
    private final List<T> records;

    public PageView(long total, int pageNum, int pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = List.copyOf(records);
    }
}
