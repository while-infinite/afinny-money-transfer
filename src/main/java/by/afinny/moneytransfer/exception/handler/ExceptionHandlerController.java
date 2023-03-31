package by.afinny.moneytransfer.exception.handler;

import by.afinny.moneytransfer.exception.TransferOrderStatusException;
import by.afinny.moneytransfer.exception.dto.ErrorDto;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.EntityNotFoundException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@ControllerAdvice
@Slf4j
public class ExceptionHandlerController {

    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> serverExceptionHandler(Exception e) {
        log.error("Internal server error", e.getMessage());
        return createResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                new ErrorDto(Integer.toString(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                        "Internal server error"));
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> badRequestExceptionHandler(Exception e) {
        log.error("Bad request", e.getMessage());
        return createResponseEntity(
                HttpStatus.BAD_REQUEST,
                new ErrorDto(Integer.toString(HttpStatus.BAD_REQUEST.value()),
                        e.getMessage()));
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TransferOrderStatusException.class)
    public ResponseEntity<ErrorDto> creditOrderStatusExceptionHandler(TransferOrderStatusException e) {
        log.error("Bad request", e.getMessage());
        return createResponseEntity(HttpStatus.BAD_REQUEST,
                new ErrorDto(Integer.toString(HttpStatus.BAD_REQUEST.value()), e.getMessage()));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> feignExceptionHandler(FeignException exception) {
        ResponseEntity.BodyBuilder responseBuilder = getDefaultResponseEntityBuilder(exception.status());
        Optional<ByteBuffer> body = exception.responseBody();
        if (body.isPresent()) {
            String message = getDecodedResponseBody(body.get());
            return responseBuilder.body(message);
        }
        return responseBuilder.build();
    }

    private ResponseEntity<ErrorDto> createResponseEntity(HttpStatus status, ErrorDto errorDto) {
        return ResponseEntity.status(status)
                .header("Content-Type", "application/json")
                .body(errorDto);
    }

    private ResponseEntity.BodyBuilder getDefaultResponseEntityBuilder(int status) {
        return ResponseEntity.status(HttpStatus.valueOf(status)).contentType(MediaType.APPLICATION_JSON);
    }

    private String getDecodedResponseBody(ByteBuffer byteBuffer) {
        return StandardCharsets.UTF_8.decode(byteBuffer).toString();
    }
}
