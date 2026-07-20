package __DDD_BASE_PACKAGE__.infra.sampleorder.adapter;

import __DDD_BASE_PACKAGE__.application.sampleorder.model.PageView;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.application.sampleorder.port.SampleOrderQueryPort;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderCriteria;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderPageCriteria;
import __DDD_BASE_PACKAGE__.domain.sampleorder.exception.SampleOrderNotFoundException;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrder;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.domain.sampleorder.repository.SampleOrderRepository;
import __DDD_BASE_PACKAGE__.infra.sampleorder.mapping.SampleOrderPersistenceConverter;
import __DDD_BASE_PACKAGE__.infra.sampleorder.persistence.entity.SampleOrderDO;
import __DDD_BASE_PACKAGE__.infra.sampleorder.persistence.mapper.SampleOrderMapper;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class SampleOrderPersistenceAdapter implements SampleOrderRepository, SampleOrderQueryPort {

    private final SampleOrderMapper mapper;

    @Override
    public SampleOrder add(SampleOrder order) {
        SampleOrderDO data = SampleOrderPersistenceConverter.INSTANCE.toData(order);
        LocalDateTime now = LocalDateTime.now();
        data.setCreateTime(now);
        data.setUpdateTime(now);
        if (mapper.insert(data) != 1) {
            throw new IllegalStateException("Failed to insert sample order: " + order.getId().getValue());
        }
        return SampleOrderPersistenceConverter.INSTANCE.toDomain(data);
    }

    @Override
    public Optional<SampleOrder> findById(SampleOrderId id) {
        return Optional.ofNullable(mapper.selectById(id.getValue()))
                .map(SampleOrderPersistenceConverter.INSTANCE::toDomain);
    }

    @Override
    public SampleOrder update(SampleOrder order) {
        SampleOrderDO data = SampleOrderPersistenceConverter.INSTANCE.toData(order);
        data.setUpdateTime(LocalDateTime.now());
        if (mapper.update(data) != 1) {
            throw new SampleOrderNotFoundException(order.getId());
        }
        return SampleOrderPersistenceConverter.INSTANCE.toDomain(data);
    }

    @Override
    public Optional<SampleOrderView> find(SampleOrderCriteria criteria) {
        return Optional.ofNullable(mapper.selectOne(criteria))
                .map(SampleOrderPersistenceConverter.INSTANCE::toView);
    }

    @Override
    public PageView<SampleOrderView> page(SampleOrderPageCriteria criteria) {
        long total = mapper.count(criteria);
        List<SampleOrderView> records = mapper.selectPage(criteria).stream()
                .map(SampleOrderPersistenceConverter.INSTANCE::toView)
                .toList();
        return new PageView<>(total, criteria.getPageNum(), criteria.getPageSize(), records);
    }
}
