package __DDD_BASE_PACKAGE__.application.sampleorder.mapping;

import __DDD_BASE_PACKAGE__.application.sampleorder.command.CreateSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrder;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SampleOrderApplicationConverter {

    SampleOrderApplicationConverter INSTANCE = Mappers.getMapper(SampleOrderApplicationConverter.class);

    default SampleOrder toDomain(CreateSampleOrderCommand command, SampleOrderId id) {
        return SampleOrder.create(id, command.getProductCode(), command.getQuantity());
    }

    @Mapping(target = "id", source = "id.value")
    SampleOrderView toView(SampleOrder source);
}
