package __DDD_BASE_PACKAGE__.trigger.exception;

import __DDD_BASE_PACKAGE__.domain.sampleorder.exception.InvalidSampleOrderStateException;
import __DDD_BASE_PACKAGE__.domain.sampleorder.exception.SampleOrderNotFoundException;
import __DDD_BASE_PACKAGE__.model.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Request validation failed");
        return ApiResponse.failure("VALIDATION_ERROR", message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException exception) {
        return ApiResponse.failure("VALIDATION_ERROR", exception.getMessage());
    }

    @ExceptionHandler(SampleOrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(SampleOrderNotFoundException exception) {
        return ApiResponse.failure("SAMPLE_ORDER_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(InvalidSampleOrderStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleInvalidState(InvalidSampleOrderStateException exception) {
        return ApiResponse.failure("INVALID_SAMPLE_ORDER_STATE", exception.getMessage());
    }
}
