package com.thanhnd.clinic_application.modules.doctors.service;

import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.entity.Doctor;
import com.thanhnd.clinic_application.modules.doctors.dto.CreateDoctorDto;
import com.thanhnd.clinic_application.modules.doctors.dto.DoctorDto;
import com.thanhnd.clinic_application.modules.doctors.dto.UpdateDoctorDto;
import org.springframework.data.domain.Pageable;

public interface DoctorService {
	PageableResultDto<DoctorDto> findAll(Pageable pageable, String searchName, String searchDepartment);

	DoctorDto findById(String id);

	DoctorDto create(CreateDoctorDto createDoctorDto);

	DoctorDto update(String id, UpdateDoctorDto updateDoctorDto);

	void delete(String id);

	Double calculateDoctorShiftPrice(Doctor doctor);
}
