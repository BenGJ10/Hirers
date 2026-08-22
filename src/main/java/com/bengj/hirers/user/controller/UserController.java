package com.bengj.hirers.user.controller;

import com.bengj.hirers.dto.UserDto;
import com.bengj.hirers.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    // Get all users for admin purposes
    @GetMapping(path = "/page/admin", version = "1.0")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir){

        Page<UserDto> userDtoPage = userService.
                getAllUsers(pageNumber, pageSize, sortBy, sortDir);
        return ResponseEntity.status(HttpStatus.OK).body(userDtoPage);
    }

    // Search user by email for admin purposes
    @GetMapping(path = "/search/admin", version = "1.0")
    public ResponseEntity<?> searchUserByEmail(@RequestParam String email) {
        Optional<UserDto> userOptional = userService.searchUserByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found with email: " + email));
        }
        return ResponseEntity.ok(userOptional.get());
    }

    // Elevate a user to employer role for admin purposes
    @PatchMapping(path = "/{userId}/role/employer/admin", version = "1.0")
    public ResponseEntity<?> elevateToEmployer(@PathVariable Long userId) {
        UserDto updatedUser = userService.elevateToEmployer(userId);
        return ResponseEntity.ok(updatedUser);
    }

    // Assign a company to an employer for admin purposes
    @PatchMapping(path = "/{userId}/company/{companyId}/admin", version = "1.0")
    public ResponseEntity<?> assignCompanyToEmployer(
            @PathVariable Long userId, @PathVariable Long companyId) {
        UserDto updatedUser = userService.assignCompanyToEmployer(userId, companyId);
        return ResponseEntity.ok(updatedUser);
    }
}
