package __DDD_BASE_PACKAGE__.infra.sampleorder.persistence.mapper;

import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderCriteria;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderPageCriteria;
import __DDD_BASE_PACKAGE__.infra.sampleorder.persistence.entity.SampleOrderDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SampleOrderMapper {
    int insert(SampleOrderDO order);

    int update(SampleOrderDO order);

    SampleOrderDO selectById(String id);

    SampleOrderDO selectOne(SampleOrderCriteria criteria);

    long count(SampleOrderPageCriteria criteria);

    List<SampleOrderDO> selectPage(SampleOrderPageCriteria criteria);
}
