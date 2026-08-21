package com.bengj.hirers.company.service;

import com.bengj.hirers.dto.CompanyDto;

import java.util.List;

public interface ICompanyService {

    List<CompanyDto> getAllCompanies();

    List<CompanyDto> getAllCompaniesForAdmin();

    boolean createCompany(CompanyDto companyDto);

    boolean updateCompany(Long id, CompanyDto companyDto);

    void deleteCompanyById(Long id);
}
