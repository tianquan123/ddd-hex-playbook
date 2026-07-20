package __DDD_BASE_PACKAGE__.facade;

import __DDD_BASE_PACKAGE__.model.common.response.ApiResponse;
import __DDD_BASE_PACKAGE__.model.common.response.PageResponse;
import __DDD_BASE_PACKAGE__.model.sampleorder.request.CreateSampleOrderRequest;
import __DDD_BASE_PACKAGE__.model.sampleorder.request.SampleOrderPageRequest;
import __DDD_BASE_PACKAGE__.model.sampleorder.response.SampleOrderResponse;

public interface SampleOrderFacade {

    ApiResponse<SampleOrderResponse> create(CreateSampleOrderRequest request);

    ApiResponse<SampleOrderResponse> get(String id);

    ApiResponse<PageResponse<SampleOrderResponse>> page(SampleOrderPageRequest request);

    ApiResponse<SampleOrderResponse> confirm(String id);

    ApiResponse<SampleOrderResponse> cancel(String id);
}
