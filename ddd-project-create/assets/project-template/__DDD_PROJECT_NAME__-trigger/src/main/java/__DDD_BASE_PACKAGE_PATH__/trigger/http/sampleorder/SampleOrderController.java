package __DDD_BASE_PACKAGE__.trigger.http.sampleorder;

import __DDD_BASE_PACKAGE__.api.SampleOrderApi;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderAppService;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderQueryService;
import __DDD_BASE_PACKAGE__.domain.sampleorder.exception.SampleOrderNotFoundException;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.model.common.response.ApiResponse;
import __DDD_BASE_PACKAGE__.model.common.response.PageResponse;
import __DDD_BASE_PACKAGE__.model.sampleorder.request.CreateSampleOrderRequest;
import __DDD_BASE_PACKAGE__.model.sampleorder.request.SampleOrderPageRequest;
import __DDD_BASE_PACKAGE__.model.sampleorder.response.SampleOrderResponse;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleOrderController implements SampleOrderApi {

    private final SampleOrderAppService appService;
    private final SampleOrderQueryService queryService;

    public SampleOrderController(
            SampleOrderAppService appService,
            SampleOrderQueryService queryService
    ) {
        this.appService = appService;
        this.queryService = queryService;
    }

    @Override
    public ApiResponse<SampleOrderResponse> create(CreateSampleOrderRequest request) {
        SampleOrderView view = appService.create(SampleOrderHttpConverter.INSTANCE.toCommand(request));
        return ApiResponse.ok(SampleOrderHttpConverter.INSTANCE.toResponse(view));
    }

    @Override
    public ApiResponse<SampleOrderResponse> get(String id) {
        SampleOrderView view = queryService.get(SampleOrderHttpConverter.INSTANCE.toCriteria(id))
                .orElseThrow(() -> new SampleOrderNotFoundException(new SampleOrderId(id)));
        return ApiResponse.ok(SampleOrderHttpConverter.INSTANCE.toResponse(view));
    }

    @Override
    public ApiResponse<PageResponse<SampleOrderResponse>> page(SampleOrderPageRequest request) {
        return ApiResponse.ok(SampleOrderHttpConverter.INSTANCE.toResponse(
                queryService.page(SampleOrderHttpConverter.INSTANCE.toCriteria(request))));
    }

    @Override
    public ApiResponse<SampleOrderResponse> confirm(String id) {
        return ApiResponse.ok(SampleOrderHttpConverter.INSTANCE.toResponse(
                appService.confirm(SampleOrderHttpConverter.INSTANCE.toConfirmCommand(id))));
    }

    @Override
    public ApiResponse<SampleOrderResponse> cancel(String id) {
        return ApiResponse.ok(SampleOrderHttpConverter.INSTANCE.toResponse(
                appService.cancel(SampleOrderHttpConverter.INSTANCE.toCancelCommand(id))));
    }
}
