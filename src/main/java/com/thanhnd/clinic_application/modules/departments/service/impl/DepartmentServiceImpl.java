package com.thanhnd.clinic_application.modules.departments.service.impl;

import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.entity.Department;
import com.thanhnd.clinic_application.helper.StringHelper;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import com.thanhnd.clinic_application.modules.departments.repository.DepartmentRepository;
import com.thanhnd.clinic_application.modules.departments.service.DepartmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
	private final DepartmentRepository departmentRepository;
	private final ModelMapper modelMapper;

	@Override
	public List<DepartmentDto> findAll() {
		return departmentRepository.findAll()
			.stream()
			.map((element) -> modelMapper.map(element, DepartmentDto.class))
			.collect(Collectors.toList());
	}

	@Override
	public DepartmentDto findById(String id) {
		return departmentRepository.findById(id)
			.map((element) -> modelMapper.map(element, DepartmentDto.class))
			.orElseThrow(() -> HttpException.notFound(Message.DEPARTMENT_NOT_FOUND.getMessage()));
	}

	@Override
	public DepartmentDto create(DepartmentDto departmentDto) {
		Optional<Department> existingCategory = departmentRepository.findByNameIgnoreCase(departmentDto.getName().trim());

		if (existingCategory.isPresent()) {
			throw HttpException.badRequest(Message.DEPARTMENT_NAME_ALREADY_EXISTS.getMessage(departmentDto.getName()));
		}

		Department department = new Department();
		department.setName(StringHelper.totTitleCase(departmentDto.getName()));
		department.setDescription(departmentDto.getDescription().trim());

		return modelMapper.map(departmentRepository.save(department), DepartmentDto.class);
	}

	@Override
	public DepartmentDto update(String id, DepartmentDto departmentDto) {
		Department existingDepartment = departmentRepository.findById(id)
			.orElseThrow(() -> HttpException.badRequest(Message.DEPARTMENT_NOT_FOUND.getMessage()));

		existingDepartment.setName(StringHelper.totTitleCase(departmentDto.getName()));
		existingDepartment.setDescription(departmentDto.getDescription().trim());

		return modelMapper.map(departmentRepository.save(existingDepartment), DepartmentDto.class);
	}

	@Override
	public void delete(String id) {
		Department existingDepartment = departmentRepository.findById(id)
			.orElseThrow(() -> HttpException.badRequest(Message.DEPARTMENT_NOT_FOUND.getMessage()));

		departmentRepository.delete(existingDepartment);
	}
}
