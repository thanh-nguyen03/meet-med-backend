package com.thanhnd.clinic_application.modules.patients.controller;

import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.modules.identity_providers.IdentityProviderStrategyFactory;
import com.thanhnd.clinic_application.modules.identity_providers.interfaces.IdentityProviderStrategy;
import com.thanhnd.clinic_application.modules.patients.dto.PatientDto;
import com.thanhnd.clinic_application.modules.patients.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ControllerPath.PATIENT_CONTROLLER)
@RequiredArgsConstructor
public class PatientController extends BaseController {
	private final PatientService patientService;
	private final IdentityProviderStrategyFactory identityProviderStrategyFactory;

	@GetMapping("/profile")
	public ResponseEntity<ResponseDto> getProfile() {
		String userId = jwtAuthenticationManager.getUserId();
		return createSuccessResponse(ResponseDto.success(patientService.findByUserId(userId)));
	}

	@PostMapping("/profile")
	public ResponseEntity<ResponseDto> createProfile(
		@RequestBody @Valid PatientDto patientDto,
		@RequestParam(required = false, defaultValue = IdentityProviderStrategyFactory.DEFAULT_STRATEGY) String identity_provider
	) {
		if (patientDto.getUser().getFullName() != null) {
			IdentityProviderStrategy strategy = identityProviderStrategyFactory.getStrategy(identity_provider);

			strategy.updateUser(jwtAuthenticationManager.getClaim("sub"), Map.of("fullName", patientDto.getUser().getFullName()));
		}

		return createSuccessResponse(ResponseDto.success(patientService.create(patientDto)));
	}

	@PutMapping("/profile")
	public ResponseEntity<ResponseDto> updateProfile(
		@RequestBody @Valid PatientDto patientDto,
		@RequestParam(required = false, defaultValue = IdentityProviderStrategyFactory.DEFAULT_STRATEGY) String identity_provider
	) {
		if (patientDto.getUser().getFullName() != null) {
			IdentityProviderStrategy strategy = identityProviderStrategyFactory.getStrategy(identity_provider);

			strategy.updateUser(jwtAuthenticationManager.getClaim("sub"), Map.of("fullName", patientDto.getUser().getFullName()));
		}
		return createSuccessResponse(ResponseDto.success(patientService.update(patientDto)));
	}
}
