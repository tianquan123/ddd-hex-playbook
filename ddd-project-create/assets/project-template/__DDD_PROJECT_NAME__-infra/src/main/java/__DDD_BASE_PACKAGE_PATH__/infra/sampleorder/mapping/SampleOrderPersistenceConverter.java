package __DDD_BASE_PACKAGE__.infra.sampleorder.mapping;

import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrder;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderStatus;
import __DDD_BASE_PACKAGE__.infra.sampleorder.persistence.entity.SampleOrderDO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SampleOrderPersistenceConverter {

    SampleOrderPersistenceConverter INSTANCE = Mappers.getMapper(SampleOrderPersistenceConverter.class);

    default SampleOrderDO toData(SampleOrder source) {
        if (source == null) {
            return null;
        }
        SampleOrderDO target = new SampleOrderDO();
        target.setId(source.getId().getValue());
        target.setProductCode(source.getProductCode());
        target.setQuantity(source.getQuantity());
        target.setStatus(source.getStatus().name());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }

    default SampleOrder toDomain(SampleOrderDO source) {
        if (source == null) {
            return null;
        }
        return SampleOrder.reconstitute(
                new SampleOrderId(source.getId()),
                source.getProductCode(),
                source.getQuantity(),
                SampleOrderStatus.valueOf(source.getStatus()),
                source.getCreateTime(),
                source.getUpdateTime());
    }

    default SampleOrderView toView(SampleOrderDO source) {
        if (source == null) {
            return null;
        }
        return SampleOrderView.builder()
                .id(source.getId())
                .productCode(source.getProductCode())
                .quantity(source.getQuantity())
                .status(SampleOrderStatus.valueOf(source.getStatus()))
                .createTime(source.getCreateTime())
                .updateTime(source.getUpdateTime())
                .build();
    }
}
