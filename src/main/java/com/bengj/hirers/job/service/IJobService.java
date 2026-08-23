package com.bengj.hirers.job.service;

import com.bengj.hirers.dto.JobDto;

import java.util.List;

public interface IJobService {

    List<JobDto> getEmployerJobs(String employerEmail);

    JobDto updateJobStatus(Long jobId, String status, String employerEmail);

    JobDto createJob(JobDto jobDto, String employerEmail);
}
