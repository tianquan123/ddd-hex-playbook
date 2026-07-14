package __DDD_BASE_PACKAGE__.infra.sample.persistence;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SampleOrderMapper {

    int upsert(SampleOrderPO order);

    SampleOrderPO selectById(String id);
}
