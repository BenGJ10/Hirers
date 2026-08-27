package com.bengj.hirers.job.service;

import com.bengj.hirers.dto.JobApplicationDto;
import com.bengj.hirers.dto.JobDto;
import com.bengj.hirers.dto.UpdateJobApplicationDto;

import java.util.List;

public interface IJobService {

    List<JobDto> getEmployerJobs(String employerEmail);

    JobDto updateJobStatus(Long jobId, String status, String employerEmail);

    JobDto createJob(JobDto jobDto, String employerEmail);

    List<JobApplicationDto> getApplicationsByJobForEmployer(Long jobId);

    boolean updateJobApplication(UpdateJobApplicationDto updateJobApplicationDto);

}
