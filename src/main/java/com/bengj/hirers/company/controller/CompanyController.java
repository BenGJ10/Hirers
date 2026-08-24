package com.bengj.hirers.company.controller;

import com.bengj.hirers.company.service.ICompanyService;
import com.bengj.hirers.dto.CompanyDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final ICompanyService companyService;

    // Get all companies
    @GetMapping(path = "/public", version = "1.0")
    public ResponseEntity<List<CompanyDto>> getAllCompanies(){
        List<CompanyDto> companyList = companyService.getAllCompanies();
        return ResponseEntity.ok().body(companyList);
    }

    // Get all companies for admin
    @GetMapping(path = "/admin", version = "1.0")
    public ResponseEntity<List<CompanyDto>> getAllCompaniesForAdmin(){
        List<CompanyDto> companyList = companyService.getAllCompaniesForAdmin();
        return ResponseEntity.ok().body(companyList);
    }

    // Create a new company
    @PostMapping(path = "/admin", version = "1.0")
    public ResponseEntity<String> createCompany(@RequestBody @Valid CompanyDto companyDto) {
        boolean isCreated = companyService.createCompany(companyDto);
        if (isCreated) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Company created successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create Company");
        }
    }

    // Update an existing company
    @PutMapping(path = "/{id}/admin", version = "1.0")
    public ResponseEntity<String> updateCompany(@PathVariable @NotBlank String id,
                                                @RequestBody @Valid CompanyDto companyDto) {
        boolean isUpdated = companyService.updateCompany(Long.valueOf(id), companyDto);
        if (isUpdated) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Successfully updated Company details");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to update Company details");
        }
    }

    // Delete a company
    @DeleteMapping(path = "/{id}/admin", version = "1.0")
    public ResponseEntity<String> deleteCompany(@PathVariable @NotBlank String id) {
        companyService.deleteCompanyById(Long.valueOf(id));
        return ResponseEntity.status(HttpStatus.OK).body("Company deleted successfully");
    }
}
