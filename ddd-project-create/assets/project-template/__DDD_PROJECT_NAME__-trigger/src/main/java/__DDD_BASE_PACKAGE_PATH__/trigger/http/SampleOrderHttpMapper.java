package __DDD_BASE_PACKAGE__.trigger.http;

import __DDD_BASE_PACKAGE__.application.sample.CreateSampleOrderCommand;
import __DDD_BASE_PACKAGE__.facade.sample.CreateSampleOrderRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SampleOrderHttpMapper {

    SampleOrderHttpMapper INSTANCE = Mappers.getMapper(SampleOrderHttpMapper.class);

    CreateSampleOrderCommand toCommand(CreateSampleOrderRequest request);
}
