package ru.practicum.stats.handler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.stats.exception.ApiError;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class StatsExceptionHandler {

    private ApiError buildError(HttpStatus status, String reason, String message, List<String> errors) {
        return ApiError.builder()
                .status(status.name())
                .reason(reason)
                .message(message)
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParams(MissingServletRequestParameterException ex) {
        log.warn("Returning BAD_REQUEST with status code {}", HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(
                buildError(HttpStatus.BAD_REQUEST,
                        "Missing required parameter",
                        "Required request parameter '" + ex.getParameterName() + "' is not present",
                        null),
                HttpStatus.BAD_REQUEST
        );
    }
}
