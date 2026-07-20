package __DDD_BASE_PACKAGE__.trigger.rpc.sampleorder;

import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderAppService;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderQueryService;
import __DDD_BASE_PACKAGE__.domain.sampleorder.exception.InvalidSampleOrderStateException;
import __DDD_BASE_PACKAGE__.domain.sampleorder.exception.SampleOrderNotFoundException;
import __DDD_BASE_PACKAGE__.facade.SampleOrderFacade;
import __DDD_BASE_PACKAGE__.model.common.response.ApiResponse;
import __DDD_BASE_PACKAGE__.model.common.response.PageResponse;
import __DDD_BASE_PACKAGE__.model.sampleorder.request.CreateSampleOrderRequest;
import __DDD_BASE_PACKAGE__.model.sampleorder.request.SampleOrderPageRequest;
import __DDD_BASE_PACKAGE__.model.sampleorder.response.SampleOrderResponse;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

@Component
@DubboService
public class SampleOrderProvider implements SampleOrderFacade {

    private final SampleOrderAppService appService;
    private final SampleOrderQueryService queryService;

    public SampleOrderProvider(SampleOrderAppService appService, SampleOrderQueryService queryService) {
        this.appService = appService;
        this.queryService = queryService;
    }

    @Override
    public ApiResponse<SampleOrderResponse> create(CreateSampleOrderRequest request) {
        try {
            return ApiResponse.ok(SampleOrderRpcConverter.INSTANCE.toResponse(
                    appService.create(SampleOrderRpcConverter.INSTANCE.toCommand(request))));
        } catch (RuntimeException exception) {
            return failure(exception);
        }
    }

    @Override
    public ApiResponse<SampleOrderResponse> get(String id) {
        return queryService.get(SampleOrderRpcConverter.INSTANCE.toCriteria(id))
                .map(SampleOrderRpcConverter.INSTANCE::toResponse)
                .map(ApiResponse::ok)
                .orElseGet(() -> ApiResponse.failure("SAMPLE_ORDER_NOT_FOUND", "Sample order not found: " + id));
    }

    @Override
    public ApiResponse<PageResponse<SampleOrderResponse>> page(SampleOrderPageRequest request) {
        try {
            return ApiResponse.ok(SampleOrderRpcConverter.INSTANCE.toResponse(
                    queryService.page(SampleOrderRpcConverter.INSTANCE.toCriteria(request))));
        } catch (RuntimeException exception) {
            return failure(exception);
        }
    }

    @Override
    public ApiResponse<SampleOrderResponse> confirm(String id) {
        try {
            SampleOrderView view = appService.confirm(SampleOrderRpcConverter.INSTANCE.toConfirmCommand(id));
            return ApiResponse.ok(SampleOrderRpcConverter.INSTANCE.toResponse(view));
        } catch (RuntimeException exception) {
            return failure(exception);
        }
    }

    @Override
    public ApiResponse<SampleOrderResponse> cancel(String id) {
        try {
            SampleOrderView view = appService.cancel(SampleOrderRpcConverter.INSTANCE.toCancelCommand(id));
            return ApiResponse.ok(SampleOrderRpcConverter.INSTANCE.toResponse(view));
        } catch (RuntimeException exception) {
            return failure(exception);
        }
    }

    private static <T> ApiResponse<T> failure(RuntimeException exception) {
        if (exception instanceof SampleOrderNotFoundException) {
            return ApiResponse.failure("SAMPLE_ORDER_NOT_FOUND", exception.getMessage());
        }
        if (exception instanceof InvalidSampleOrderStateException) {
            return ApiResponse.failure("INVALID_SAMPLE_ORDER_STATE", exception.getMessage());
        }
        if (exception instanceof IllegalArgumentException) {
            return ApiResponse.failure("VALIDATION_ERROR", exception.getMessage());
        }
        throw exception;
    }
}
