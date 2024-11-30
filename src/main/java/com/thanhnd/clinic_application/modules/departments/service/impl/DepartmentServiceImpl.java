package com.thanhnd.clinic_application.modules.departments.service.impl;

import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.constants.Role;
import com.thanhnd.clinic_application.entity.Department;
import com.thanhnd.clinic_application.entity.Doctor;
import com.thanhnd.clinic_application.helper.StringHelper;
import com.thanhnd.clinic_application.mapper.DepartmentMapper;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import com.thanhnd.clinic_application.modules.departments.repository.DepartmentRepository;
import com.thanhnd.clinic_application.modules.departments.service.DepartmentService;
import com.thanhnd.clinic_application.modules.doctors.repository.DoctorRepository;
import com.thanhnd.clinic_application.modules.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
	// Repositories
	private final DepartmentRepository departmentRepository;
	private final DoctorRepository doctorRepository;
	private final UserRepository userRepository;

	// Mappers
	private final DepartmentMapper departmentMapper;

	@Override
	public PageableResultDto<DepartmentDto> findAll(Pageable pageable) {
		return PageableResultDto.parse(departmentRepository.findAll(pageable).map(departmentMapper::toDto));
	}

	@Override
	public List<DepartmentDto> findAll() {
		return departmentRepository.findAll().stream().map(departmentMapper::toDto).toList();
	}

	@Override
	public DepartmentDto findById(String id) {
		return departmentRepository.findById(id)
			.map(departmentMapper::toDto)
			.orElseThrow(() -> HttpException.notFound(Message.DEPARTMENT_NOT_FOUND.getMessage()));
	}

	@Override
	public DepartmentDto create(DepartmentDto departmentDto) {
		Optional<Department> existingDepartment = departmentRepository.findByNameIgnoreCase(departmentDto.getName().trim());

		if (existingDepartment.isPresent()) {
			throw HttpException.badRequest(Message.DEPARTMENT_NAME_ALREADY_EXISTS.getMessage(departmentDto.getName()));
		}

		Department department = new Department();
		department.setName(StringHelper.toTitleCase(departmentDto.getName()));
		department.setDescription(departmentDto.getDescription().trim());
		department.setImageUrl(departmentDto.getImageUrl());

		return departmentMapper.toDto(departmentRepository.save(department));
	}

	@Override
	public DepartmentDto update(String id, DepartmentDto departmentDto) {
		Department existingDepartment = departmentRepository.findById(id)
			.orElseThrow(() -> HttpException.badRequest(Message.DEPARTMENT_NOT_FOUND.getMessage()));

		existingDepartment.setName(StringHelper.toTitleCase(departmentDto.getName()));
		existingDepartment.setDescription(departmentDto.getDescription().trim());
		existingDepartment.setImageUrl(departmentDto.getImageUrl());

		return departmentMapper.toDto(departmentRepository.save(existingDepartment));
	}

	@Override
	public void delete(String id) {
		Department existingDepartment = departmentRepository.findById(id)
			.orElseThrow(() -> HttpException.badRequest(Message.DEPARTMENT_NOT_FOUND.getMessage()));

		departmentRepository.delete(existingDepartment);
	}

	@Override
	public void addHeadDoctor(String departmentId, String doctorId) {
		Doctor doctor = doctorRepository.findById(doctorId)
			.orElseThrow(() -> HttpException.badRequest(Message.DOCTOR_NOT_FOUND.getMessage()));

		Department department = departmentRepository.findById(departmentId)
			.orElseThrow(() -> HttpException.badRequest(Message.DEPARTMENT_NOT_FOUND.getMessage()));

		if (doctor.getDepartment() != null && !doctor.getDepartment().getId().equals(departmentId)) {
			throw HttpException.badRequest(Message.DOCTOR_NOT_IN_DEPARTMENT.getMessage(doctor.getUser().getFullName()));
		}

		department.setHeadDoctor(doctor);
		departmentRepository.save(department);

		doctor.getUser().setRole(Role.HeadDoctor);
		userRepository.save(doctor.getUser());
	}

	@Override
	public void addDoctors(String departmentId, List<String> doctorIds) {
		Department department = departmentRepository.findById(departmentId)
			.orElseThrow(() -> HttpException.badRequest(Message.DEPARTMENT_NOT_FOUND.getMessage()));

		List<Doctor> newDoctors = doctorRepository.findAllById(doctorIds);
		Set<Doctor> newDoctorsSet = new HashSet<>(newDoctors);
		List<Doctor> currentDoctors = department.getDoctors();
		Set<Doctor> currentDoctorsSet = new HashSet<>(currentDoctors);

		currentDoctorsSet.addAll(newDoctorsSet);
		department.setDoctors(currentDoctorsSet.stream().toList());

		departmentRepository.save(department);
	}

	@Override
	public void removeDoctor(String departmentId, String doctorId) {
		Department department = departmentRepository.findById(departmentId)
			.orElseThrow(() -> HttpException.badRequest(Message.DEPARTMENT_NOT_FOUND.getMessage()));

		Doctor doctor = doctorRepository.findById(doctorId)
			.orElseThrow(() -> HttpException.badRequest(Message.DOCTOR_NOT_FOUND.getMessage()));

		if (doctor.getDepartment() == null || !doctor.getDepartment().getId().equals(departmentId)) {
			throw HttpException.badRequest(Message.DOCTOR_NOT_IN_DEPARTMENT.getMessage(doctor.getUser().getFullName()));
		}

		List<Doctor> doctors = department.getDoctors();
		doctors.remove(doctor);

		department.setDoctors(doctors);

		if (department.getHeadDoctor() != null && department.getHeadDoctor().getId().equals(doctorId)) {
			department.setHeadDoctor(null);
			doctor.getUser().setRole(Role.Doctor);
			userRepository.save(doctor.getUser());
		}

		departmentRepository.save(department);

		doctor.setDepartment(null);
		doctorRepository.save(doctor);
	}
}
