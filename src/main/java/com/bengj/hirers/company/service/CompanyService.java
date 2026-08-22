package com.bengj.hirers.company.service;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.dto.CompanyDto;
import com.bengj.hirers.dto.JobDto;
import com.bengj.hirers.entity.Company;
import com.bengj.hirers.entity.Job;
import com.bengj.hirers.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService implements ICompanyService{

    private final CompanyRepository companyRepository;

    // Method to retrieve all companies and transform them into CompanyDto objects
    @Override
    public List<CompanyDto> getAllCompanies() {
        List<Company> companyList = companyRepository.fetchCompaniesWithJobsByStatus(ApplicationConstants.JOB_STATUS_ACTIVE);
        return companyList.stream()
                .map(this::transformCompanyToDto)
                .collect(Collectors.toList());
    }

    // Method to retrieve all companies for admin and transform them into CompanyDto objects
    @Override
    public List<CompanyDto> getAllCompaniesForAdmin() {
        List<Company> companyList = companyRepository.findAll();
        return companyList.stream()
                .map(this::transformCompanyToDtoForAdmin)
                .collect(Collectors.toList());
    }

    // Method to create a new company and return true if successful, false otherwise
    @Override
    @Transactional
    public boolean createCompany(CompanyDto companyDto){
        Company company = transformCompanyDtoToEntity(companyDto);
        Company savedCompany = companyRepository.save(company);
        return savedCompany.getId() != null;
    }

    // Method to update an existing company and return true if successful, false otherwise
    @Override
    @Transactional
    public boolean updateCompany(Long id, CompanyDto companyDto) {
        int updatedRecords = companyRepository.updateCompany(
                id,companyDto.name(),companyDto.logo(),
                companyDto.industry(),companyDto.size(),companyDto.rating(),
                companyDto.locations(),companyDto.founded(),companyDto.description(),
                companyDto.employees(),companyDto.website()
        );
        return updatedRecords > 0;
    }

    // Method to delete a company by its ID
    @Override
    @Transactional
    public void deleteCompanyById(Long id){
        companyRepository.deleteById(id);
    }


    // Utility method to transform Company entity to CompanyDto
    private CompanyDto transformCompanyToDto(Company company){
        List<JobDto> jobDtos = company.getJobs().stream()
                .map(this::transformToJobDto)
                .toList();

        return new CompanyDto(
                company.getId(), company.getName(), company.getLogo(),
                company.getIndustry(), company.getSize(), company.getRating(),
                company.getLocations(), company.getFounded(), company.getDescription(),
                company.getEmployees(), company.getWebsite(), company.getCreatedAt(),
                jobDtos);
    }

    // Utility method to transform Job entity to JobDto
    private JobDto transformToJobDto(Job job){
        return new JobDto(
                job.getId(),
                job.getTitle(),
                job.getCompany().getId(),
                job.getCompany().getName(),
                job.getCompany().getLogo(),
                job.getLocation(),
                job.getWorkType(),
                job.getJobType(),
                job.getCategory(),
                job.getExperienceLevel(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getSalaryCurrency(),
                job.getSalaryPeriod(),
                job.getDescription(),
                job.getRequirements(),
                job.getBenefits(),
                job.getPostedDate(),
                job.getApplicationDeadline(),
                job.getApplicationsCount(),
                job.getFeatured(),
                job.getUrgent(),
                job.getRemote(),
                job.getStatus()
        );
    }

    // Utility method to transform CompanyDto to Company entity
    private Company transformCompanyDtoToEntity(CompanyDto companyDto) {
        Company company = new Company();
        BeanUtils.copyProperties(companyDto, company);
        return company;
    }
    
    // Utility method to transform Company entity to CompanyDto for admin
    private CompanyDto transformCompanyToDtoForAdmin(Company company) {
        return new CompanyDto(company.getId(), company.getName(), company.getLogo(),
                company.getIndustry(), company.getSize(), company.getRating(),
                company.getLocations(), company.getFounded(), company.getDescription(),
                company.getEmployees(), company.getWebsite(), company.getCreatedAt(),null);
    }

}
