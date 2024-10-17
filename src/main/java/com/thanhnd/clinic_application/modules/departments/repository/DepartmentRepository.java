package com.thanhnd.clinic_application.modules.departments.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.Department;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends BaseRepository<Department, String> {
	Optional<Department> findByNameIgnoreCase(String name);
}
