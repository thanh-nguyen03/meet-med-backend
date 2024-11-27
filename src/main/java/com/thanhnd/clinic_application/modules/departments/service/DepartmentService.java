package com.thanhnd.clinic_application.modules.departments.service;

import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {
	PageableResultDto<DepartmentDto> findAll(Pageable pageable);

	List<DepartmentDto> findAll();

	DepartmentDto findById(String id);

	DepartmentDto create(DepartmentDto departmentDto);

	DepartmentDto update(String id, DepartmentDto departmentDto);

	void delete(String id);

	void addHeadDoctor(String departmentId, String doctorId);

	void addDoctors(String departmentId, List<String> doctorIds);

	void removeDoctor(String departmentId, String doctorId);
}
