package com.thanhnd.clinic_application.modules.departments.service;

import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;

import java.util.List;

public interface DepartmentService {
	List<DepartmentDto> findAll();

	DepartmentDto findById(String id);

	DepartmentDto create(DepartmentDto departmentDto);

	DepartmentDto update(String id, DepartmentDto departmentDto);

	void delete(String id);
}
