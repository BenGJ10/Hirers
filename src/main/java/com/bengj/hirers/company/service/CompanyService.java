package com.bengj.hirers.company.service;

import com.bengj.hirers.dto.CompanyDto;
import com.bengj.hirers.entity.Company;
import com.bengj.hirers.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService implements ICompanyService{

    private final CompanyRepository companyRepository;

    // Method to retrieve all companies and transform them into CompanyDto objects
    @Override
    public List<CompanyDto> getAllCompanies() {
        List<Company> companyList = companyRepository.findAll();
        return companyList.stream()
                .map(this::transformToDto)
                .collect(Collectors.toList());
    }

    // Utility method to transform Company entity to CompanyDto
    private CompanyDto transformToDto(Company company){
        return new CompanyDto(
                company.getID(), company.getName(), company.getLogo(),
                company.getIndustry(), company.getSize(), company.getRating(),
                company.getLocations(), company.getFounded(), company.getDescription(),
                company.getEmployees(), company.getWebsite(), company.getCreatedAt());
    }
}
