package com.bengj.hirers.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

// This class is used to represent the structure of an error response that will be sent back to the client when an exception occurs in the application
// It contains information about the API path where the error occurred, the HTTP status code, a message describing the error, and a timestamp indicating when the error occurred
public record ErrorResponseDto(String apiPath, HttpStatus errorCode,
                               String errorMessage, LocalDateTime errorTimestamp) {
}
