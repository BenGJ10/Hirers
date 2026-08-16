package com.bengj.hirers.dto;

public record LoginResponseDto(String message, UserDto user, String token) {
}
