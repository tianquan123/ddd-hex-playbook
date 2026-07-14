package __DDD_BASE_PACKAGE__.trigger.http;

import __DDD_BASE_PACKAGE__.application.sample.CreateSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sample.SampleOrderAppService;
import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderAggregate;
import __DDD_BASE_PACKAGE__.facade.sample.CreateSampleOrderRequest;
import __DDD_BASE_PACKAGE__.facade.sample.CreateSampleOrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sample/orders")
public class SampleOrderController {

    private final SampleOrderAppService appService;

    public SampleOrderController(SampleOrderAppService appService) {
        this.appService = appService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CreateSampleOrderResponse create(@RequestBody CreateSampleOrderRequest request) {
        SampleOrderAggregate order = appService.create(
                new CreateSampleOrderCommand(request.productCode(), request.quantity()));
        return new CreateSampleOrderResponse(order.id().value(), order.status().name());
    }
}
