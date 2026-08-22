package com.bengj.hirers.user.service;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.dto.UserDto;
import com.bengj.hirers.entity.Company;
import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.entity.Role;
import com.bengj.hirers.repository.CompanyRepository;
import com.bengj.hirers.repository.HirersUserRepository;
import com.bengj.hirers.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements IUserService{

    private final HirersUserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;

    // Method to search for a user by email and return an Optional<UserDto>
    @Override
    public Optional<UserDto> searchUserByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .map(this::mapToUserDto);
    }

    // Method to elevate a user to employer role based on the provided userId
    @Override
    @Transactional
    public UserDto elevateToEmployer(Long userId) {
        HirersUser user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found with id: " + userId));

        // Check if the user is already having the employer role
        if (user.getRole().getName().equals(ApplicationConstants.ROLE_EMPLOYER)) {
            return mapToUserDto(user);
        }

        // Check if the user is already an admin
        if (user.getRole().getName().equals(ApplicationConstants.ROLE_ADMIN)) {
            throw new RuntimeException("Cannot elevate admin to employer");
        }

        // Elevate the user to employer role
        Role employerRole = roleRepository.findRoleByName(ApplicationConstants.ROLE_EMPLOYER).orElseThrow(
                () -> new RuntimeException("Role not found: " + ApplicationConstants.ROLE_EMPLOYER));
        user.setRole(employerRole);
        return mapToUserDto(user);
    }   

    // Method to assign a company to an employer based on the provided userId and companyId
    @Override
    @Transactional
    public UserDto assignCompanyToEmployer(Long userId, Long companyId) {
        HirersUser user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found with id: " + userId));

        // Verify that the user is an employer
        if (!user.getRole().getName().equals(ApplicationConstants.ROLE_EMPLOYER)) {
            throw new RuntimeException("User is not an employer");
        }

        // Verify that the company exists
        Company company = companyRepository.findById(companyId).orElseThrow(
                () -> new RuntimeException("Company not found with id: " + companyId));

        user.setCompany(company);
        return mapToUserDto(user);
    }

    
    // Utility method to transform User entity to UserDTO
    private UserDto mapToUserDto(HirersUser user) {
        UserDto dto = new UserDto();
        BeanUtils.copyProperties(user, dto);
        dto.setUserId(user.getId());
        dto.setRole(user.getRole() != null ? user.getRole().getName() : null);
        dto.setCompanyId(user.getCompany() != null ? user.getCompany().getId() : null);
        dto.setCompanyName(user.getCompany() != null ? user.getCompany().getName() : null);
        return dto;
    }
}
