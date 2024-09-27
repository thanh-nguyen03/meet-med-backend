package com.thanhnd.clinic_application.modules.doctors.controller;

import com.thanhnd.clinic_application.annotation.PermissionsAllowed;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.constants.Permissions;
import com.thanhnd.clinic_application.modules.doctors.dto.CreateDoctorDto;
import com.thanhnd.clinic_application.modules.doctors.dto.DoctorDto;
import com.thanhnd.clinic_application.modules.doctors.dto.UpdateDoctorDto;
import com.thanhnd.clinic_application.modules.doctors.service.DoctorService;
import com.thanhnd.clinic_application.modules.identity_providers.IdentityProviderStrategyFactory;
import com.thanhnd.clinic_application.modules.identity_providers.interfaces.IdentityProviderStrategy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(ControllerPath.DOCTOR_ADMIN_CONTROLLER)
@RequiredArgsConstructor
public class DoctorAdminController {
	private final DoctorService doctorService;
	private final IdentityProviderStrategyFactory identityProviderStrategyFactory;

	@GetMapping
	@PermissionsAllowed(Permissions.Doctors.READ)
	public ResponseEntity<ResponseDto> findAll() {
		return ResponseEntity.ok(ResponseDto.success(doctorService.findAll()));
	}

	@GetMapping("/{id}")
	@PermissionsAllowed(Permissions.Doctors.READ)
	public ResponseEntity<ResponseDto> findById(@PathVariable String id) {
		return ResponseEntity.ok(ResponseDto.success(doctorService.findById(id)));
	}

	@PostMapping
	@PermissionsAllowed(Permissions.Doctors.WRITE)
	public ResponseEntity<ResponseDto> create(@RequestBody @Valid CreateDoctorDto createDoctorDto) {
		Optional<String> provider = Optional.ofNullable(createDoctorDto.getIdentityProvider());
		IdentityProviderStrategy strategy = identityProviderStrategyFactory.getStrategy(
			provider.orElse(IdentityProviderStrategyFactory.DEFAULT_STRATEGY)
		);

		// Create doctor in database
		DoctorDto doctorDto = doctorService.create(createDoctorDto);

		// Create user in identity provider
		strategy.createUser(createDoctorDto.getEmail(), createDoctorDto.getPassword());


		return ResponseEntity.ok(ResponseDto.success(doctorDto));
	}

	@PutMapping("/{id}")
	@PermissionsAllowed(Permissions.Doctors.WRITE)
	public ResponseEntity<ResponseDto> update(
		@PathVariable String id,
		@RequestBody @Valid UpdateDoctorDto updateDoctorDto
	) {
		return ResponseEntity.ok(ResponseDto.success(doctorService.update(id, updateDoctorDto)));
	}

	@DeleteMapping("/{id}")
	@PermissionsAllowed(
		permissions = {Permissions.Users.DELETE, Permissions.Doctors.WRITE},
		decisionStrategy = PermissionsAllowed.DecisionStrategy.ALL
	)
	public ResponseEntity<ResponseDto> delete(
		@PathVariable String id,
		@RequestParam(required = false, defaultValue = IdentityProviderStrategyFactory.DEFAULT_STRATEGY) String identity_provider
	) {
		IdentityProviderStrategy strategy = identityProviderStrategyFactory.getStrategy(identity_provider);
		Map<String, Object> user = strategy.getUserByEmail(doctorService.findById(id).getUser().getEmail());
		String identityProviderUserId = (String) user.get(strategy.getUserIdKey());
		strategy.deleteUser(identityProviderUserId);

		doctorService.delete(id);
		return ResponseEntity.ok(ResponseDto.success());
	}
}
