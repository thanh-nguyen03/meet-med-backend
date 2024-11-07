package com.thanhnd.clinic_application.modules.doctors.service;

import com.thanhnd.clinic_application.entity.Doctor;
import com.thanhnd.clinic_application.modules.doctors.dto.CreateDoctorDto;
import com.thanhnd.clinic_application.modules.doctors.dto.DoctorDto;
import com.thanhnd.clinic_application.modules.doctors.dto.UpdateDoctorDto;

import java.util.List;

public interface DoctorService {
	List<DoctorDto> findAll();

	DoctorDto findById(String id);

	DoctorDto create(CreateDoctorDto createDoctorDto);

	DoctorDto update(String id, UpdateDoctorDto updateDoctorDto);

	void delete(String id);

	Double calculateDoctorShiftPrice(Doctor doctor);
}
