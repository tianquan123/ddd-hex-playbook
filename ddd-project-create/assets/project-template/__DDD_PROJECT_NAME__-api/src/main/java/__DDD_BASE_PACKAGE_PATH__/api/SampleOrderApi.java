package __DDD_BASE_PACKAGE__.api;

import __DDD_BASE_PACKAGE__.model.common.response.ApiResponse;
import __DDD_BASE_PACKAGE__.model.common.response.PageResponse;
import __DDD_BASE_PACKAGE__.model.sampleorder.request.CreateSampleOrderRequest;
import __DDD_BASE_PACKAGE__.model.sampleorder.request.SampleOrderPageRequest;
import __DDD_BASE_PACKAGE__.model.sampleorder.response.SampleOrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("/sample/orders")
public interface SampleOrderApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<SampleOrderResponse> create(@Valid @RequestBody CreateSampleOrderRequest request);

    @GetMapping("/{id}")
    ApiResponse<SampleOrderResponse> get(@PathVariable("id") String id);

    @GetMapping
    ApiResponse<PageResponse<SampleOrderResponse>> page(@Valid SampleOrderPageRequest request);

    @PostMapping("/{id}/confirm")
    ApiResponse<SampleOrderResponse> confirm(@PathVariable("id") String id);

    @PostMapping("/{id}/cancel")
    ApiResponse<SampleOrderResponse> cancel(@PathVariable("id") String id);
}
