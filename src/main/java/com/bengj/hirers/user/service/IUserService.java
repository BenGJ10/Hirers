package com.bengj.hirers.user.service;

import com.bengj.hirers.dto.UserDto;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface IUserService {

    Optional<UserDto> searchUserByEmail(String email);

    UserDto elevateToEmployer(Long userId);

    UserDto assignCompanyToEmployer(Long userId, Long companyId);

    Page<UserDto> getAllUsers(int pageNumber, int pageSize, String sortBy, String sortDir);
}
