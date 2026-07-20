package __DDD_BASE_PACKAGE__.trigger.http.sampleorder;

import __DDD_BASE_PACKAGE__.application.sampleorder.command.CancelSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.command.ConfirmSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.command.CreateSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.PageView;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderCriteria;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderPageCriteria;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderStatus;
import __DDD_BASE_PACKAGE__.model.common.response.PageResponse;
import __DDD_BASE_PACKAGE__.model.sampleorder.request.CreateSampleOrderRequest;
import __DDD_BASE_PACKAGE__.model.sampleorder.request.SampleOrderPageRequest;
import __DDD_BASE_PACKAGE__.model.sampleorder.response.SampleOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SampleOrderHttpConverter {

    SampleOrderHttpConverter INSTANCE = Mappers.getMapper(SampleOrderHttpConverter.class);

    CreateSampleOrderCommand toCommand(CreateSampleOrderRequest request);

    SampleOrderResponse toResponse(SampleOrderView view);

    default SampleOrderCriteria toCriteria(String id) {
        return new SampleOrderCriteria(id);
    }

    default ConfirmSampleOrderCommand toConfirmCommand(String id) {
        return new ConfirmSampleOrderCommand(id);
    }

    default CancelSampleOrderCommand toCancelCommand(String id) {
        return new CancelSampleOrderCommand(id);
    }

    default SampleOrderPageCriteria toCriteria(SampleOrderPageRequest request) {
        SampleOrderStatus status = request.getStatus() == null || request.getStatus().isBlank()
                ? null
                : SampleOrderStatus.valueOf(request.getStatus().trim().toUpperCase());
        return new SampleOrderPageCriteria(status, request.getPageNum(), request.getPageSize());
    }

    default PageResponse<SampleOrderResponse> toResponse(PageView<SampleOrderView> page) {
        return new PageResponse<>(
                page.getTotal(),
                page.getPageNum(),
                page.getPageSize(),
                page.getRecords().stream().map(this::toResponse).toList());
    }
}
