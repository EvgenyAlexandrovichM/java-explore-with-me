package ru.practicum.main.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.EntityAlreadyExistsException;
import ru.practicum.main.exception.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class MainExceptionHandler {

    private ApiError buildError(HttpStatus status, String reason, String message, List<String> errors) {
        return ApiError.builder()
                .status(status.name())
                .reason(reason)
                .message(message)
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex) {
        log.warn("404 Not Found: {}", ex.getMessage());
        return new ResponseEntity<>(
                buildError(HttpStatus.NOT_FOUND, "Object not found", ex.getMessage(), null),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        log.warn("400 Bad Request: {}", ex.getMessage());
        return new ResponseEntity<>(
                buildError(HttpStatus.BAD_REQUEST, "Invalid data", ex.getMessage(), null),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ConflictException.class, EntityAlreadyExistsException.class})
    public ResponseEntity<ApiError> handleConflict(RuntimeException ex) {
        log.warn("409 Conflict: {}", ex.getMessage());
        return new ResponseEntity<>(
                buildError(HttpStatus.CONFLICT, "Conflict data", ex.getMessage(), null),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("400 Validation error: {}", ex.getMessage());
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.toList());
        return new ResponseEntity<>(
                buildError(HttpStatus.BAD_REQUEST, "Validation failed", "Invalid fields", errors),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleAll(RuntimeException ex) {
        log.warn("500 Internal server error: {}", ex.getMessage());
        return new ResponseEntity<>(
                buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ex.getMessage(), null),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
