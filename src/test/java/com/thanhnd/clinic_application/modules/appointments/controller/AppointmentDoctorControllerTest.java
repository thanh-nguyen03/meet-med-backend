package com.thanhnd.clinic_application.modules.appointments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.AppointmentStatus;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.modules.appointments.dto.AppointmentDto;
import com.thanhnd.clinic_application.modules.appointments.service.AppointmentService;
import com.thanhnd.clinic_application.modules.doctors.dto.DoctorDto;
import com.thanhnd.clinic_application.modules.patients.dto.PatientDto;
import com.thanhnd.clinic_application.modules.shifts.dto.RegisteredShiftTimeSlotDto;
import com.thanhnd.clinic_application.util.MockMvcRequestBuilderHelper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Appointment Doctor Controller Tests")
@Tag("controller")
public class AppointmentDoctorControllerTest {
	private static final Logger logger = Logger.getLogger(AppointmentDoctorControllerTest.class.getName());
	private TestInfo testInfo;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AppointmentService appointmentService;

	@MockBean
	private JwtAuthenticationManager jwtAuthenticationManager;

	private static final String TEST_DOCTOR_USER_ID = "test-doctor-user-id";
	private static final String TEST_APPOINTMENT_ID = "test-appointment-id";
	private static final String TEST_REGISTERED_SHIFT_ID = "test-registered-shift-id";

	@BeforeEach
	void setUp(TestInfo testInfo) {
		this.testInfo = testInfo;
		logger.info("Starting test: " + testInfo.getDisplayName());

		// Mock JWT authentication
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_DOCTOR_USER_ID);
	}

	@AfterEach
	void tearDown() {
		logger.info("Completed test: " + testInfo.getDisplayName());
	}

	/**
	 * Test Case 1: Get Doctor Appointments - Success
	 * Goal: Verify successful retrieval of doctor's appointments
	 * Input: Valid JWT token, pagination parameters
	 * Expected Output: 200 OK with list of appointments
	 */
	@Test
	@Order(1)
	@Transactional
	@DisplayName("TC-ADC-001: Get Doctor Appointments - Success")
	void testGetDoctorAppointmentsSuccess() throws Exception {
		// Given
		PageableResultDto<AppointmentDto> mockResult = PageableResultDto.of(0, 0, 0, new ArrayList<>());
		List<AppointmentDto> appointments = new ArrayList<>();

		AppointmentDto appointment = createMockAppointment();
		appointments.add(appointment);

		mockResult.setContent(appointments);
		mockResult.setTotalElements(1);
		mockResult.setTotalPage(1);
		mockResult.setPageSize(10);


		when(appointmentService.findAllForDoctor(any(Pageable.class), isNull(), isNull()))
			.thenReturn(mockResult);

		// When/Then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilderHelper.get(ControllerPath.APPOINTMENT_DOCTOR_CONTROLLER)
					.param("page", "0")
					.param("size", "10")
					.param("orderBy", "registeredShiftTimeSlot.startTime")
					.param("order", "asc")
			)
			.andExpect(status().isOk())
			.andReturn();

		// Then
		String responseContent = result.getResponse().getContentAsString();
		ResponseDto responseDto = objectMapper.readValue(responseContent, ResponseDto.class);

		assertNotNull(responseDto, "Response should not be null");
		assertTrue(responseDto.isSuccess(), "Response should be successful");
		assertNotNull(responseDto.getData(), "Response data should not be null");
	}

	/**
	 * Test Case 2: Get Doctor Appointments - With Registered Shift Filter
	 * Goal: Verify successful retrieval of doctor's appointments with registered shift filter
	 * Input: Valid JWT token, pagination parameters, registered shift ID
	 * Expected Output: 200 OK with filtered list of appointments
	 */
	@Test
	@Order(2)
	@Transactional
	@DisplayName("TC-ADC-002: Get Doctor Appointments - With Registered Shift Filter")
	void testGetDoctorAppointmentsWithRegisteredShiftFilter() throws Exception {
		// Given
		PageableResultDto<AppointmentDto> mockResult = PageableResultDto.of(0, 0, 0, new ArrayList<>());
		List<AppointmentDto> appointments = new ArrayList<>();

		AppointmentDto appointment = createMockAppointment();
		appointments.add(appointment);

		mockResult.setContent(appointments);
		mockResult.setTotalElements(1);
		mockResult.setTotalPage(1);
		mockResult.setPageSize(10);


		when(appointmentService.findAllForDoctor(any(Pageable.class), eq(TEST_REGISTERED_SHIFT_ID), isNull()))
			.thenReturn(mockResult);

		// When/Then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilderHelper.get(ControllerPath.APPOINTMENT_DOCTOR_CONTROLLER)
					.param("page", "0")
					.param("size", "10")
					.param("orderBy", "registeredShiftTimeSlot.startTime")
					.param("order", "asc")
					.param("registeredShiftId", TEST_REGISTERED_SHIFT_ID)
			)
			.andExpect(status().isOk())
			.andReturn();

		// Then
		String responseContent = result.getResponse().getContentAsString();
		ResponseDto responseDto = objectMapper.readValue(responseContent, ResponseDto.class);

		assertNotNull(responseDto, "Response should not be null");
		assertTrue(responseDto.isSuccess(), "Response should be successful");
		assertNotNull(responseDto.getData(), "Response data should not be null");

		// Verify service was called with correct registered shift ID
		verify(appointmentService).findAllForDoctor(any(Pageable.class), eq(TEST_REGISTERED_SHIFT_ID), isNull());
	}

	/**
	 * Test Case 3: Get Doctor Appointments - With Status Filter
	 * Goal: Verify successful retrieval of doctor's appointments with status filter
	 * Input: Valid JWT token, pagination parameters, status filter
	 * Expected Output: 200 OK with filtered list of appointments
	 */
	@Test
	@Order(3)
	@Transactional
	@DisplayName("TC-ADC-003: Get Doctor Appointments - With Status Filter")
	void testGetDoctorAppointmentsWithStatusFilter() throws Exception {
		// Given
		PageableResultDto<AppointmentDto> mockResult = PageableResultDto.of(0, 0, 0, new ArrayList<>());
		List<AppointmentDto> appointments = new ArrayList<>();

		AppointmentDto appointment = createMockAppointment();
		appointment.setStatus(AppointmentStatus.UPCOMING);
		appointments.add(appointment);

		mockResult.setContent(appointments);
		mockResult.setTotalElements(1);
		mockResult.setTotalPage(1);
		mockResult.setPageSize(10);


		when(appointmentService.findAllForDoctor(any(Pageable.class), isNull(), eq(AppointmentStatus.UPCOMING)))
			.thenReturn(mockResult);

		// When/Then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilderHelper.get(ControllerPath.APPOINTMENT_DOCTOR_CONTROLLER)
					.param("page", "0")
					.param("size", "10")
					.param("orderBy", "registeredShiftTimeSlot.startTime")
					.param("order", "asc")
					.param("status", "UPCOMING")
			)
			.andExpect(status().isOk())
			.andReturn();

		// Then
		String responseContent = result.getResponse().getContentAsString();
		ResponseDto responseDto = objectMapper.readValue(responseContent, ResponseDto.class);

		assertNotNull(responseDto, "Response should not be null");
		assertTrue(responseDto.isSuccess(), "Response should be successful");
		assertNotNull(responseDto.getData(), "Response data should not be null");

		// Verify service was called with correct status
		verify(appointmentService).findAllForDoctor(any(Pageable.class), isNull(), eq(AppointmentStatus.UPCOMING));
	}

	/**
	 * Test Case 4: Get Appointment by ID for Doctor - Success
	 * Goal: Verify successful retrieval of an appointment by ID for a doctor
	 * Input: Valid JWT token, valid appointment ID
	 * Expected Output: 200 OK with appointment details
	 */
	@Test
	@Order(4)
	@Transactional
	@DisplayName("TC-ADC-004: Get Appointment by ID for Doctor - Success")
	void testGetAppointmentByIdForDoctorSuccess() throws Exception {
		// Given
		AppointmentDto mockAppointment = createMockAppointment();
		when(appointmentService.findByIdForDoctor(TEST_APPOINTMENT_ID)).thenReturn(mockAppointment);

		// When/Then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilderHelper.get(ControllerPath.APPOINTMENT_DOCTOR_CONTROLLER + "/" + TEST_APPOINTMENT_ID)
			)
			.andExpect(status().isOk())
			.andReturn();

		// Then
		String responseContent = result.getResponse().getContentAsString();
		ResponseDto responseDto = objectMapper.readValue(responseContent, ResponseDto.class);

		assertNotNull(responseDto, "Response should not be null");
		assertTrue(responseDto.isSuccess(), "Response should be successful");
		assertNotNull(responseDto.getData(), "Response data should not be null");

		// Verify service was called with correct ID
		verify(appointmentService).findByIdForDoctor(TEST_APPOINTMENT_ID);
	}

	/**
	 * Test Case 5: Complete Appointment - Success
	 * Goal: Verify successful completion of an appointment
	 * Input: Valid JWT token, valid appointment ID
	 * Expected Output: 200 OK with updated appointment details
	 */
	@Test
	@Order(5)
	@Transactional
	@DisplayName("TC-ADC-005: Complete Appointment - Success")
	void testCompleteAppointmentSuccess() throws Exception {
		// Given
		AppointmentDto mockAppointment = createMockAppointment();
		mockAppointment.setStatus(AppointmentStatus.COMPLETED);
		when(appointmentService.updateStatus(TEST_APPOINTMENT_ID, AppointmentStatus.COMPLETED)).thenReturn(mockAppointment);

		// When/Then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilderHelper.put(ControllerPath.APPOINTMENT_DOCTOR_CONTROLLER + "/" + TEST_APPOINTMENT_ID + "/complete")
			)
			.andExpect(status().isOk())
			.andReturn();

		// Then
		String responseContent = result.getResponse().getContentAsString();
		ResponseDto responseDto = objectMapper.readValue(responseContent, ResponseDto.class);

		assertNotNull(responseDto, "Response should not be null");
		assertTrue(responseDto.isSuccess(), "Response should be successful");
		assertNotNull(responseDto.getData(), "Response data should not be null");

		// Verify service was called with correct ID and status
		verify(appointmentService).updateStatus(TEST_APPOINTMENT_ID, AppointmentStatus.COMPLETED);
	}

	/**
	 * Helper method to create a mock appointment for testing
	 */
	private AppointmentDto createMockAppointment() {
		AppointmentDto appointment = new AppointmentDto();
		appointment.setId(TEST_APPOINTMENT_ID);
		appointment.setSymptoms("Test symptoms");
		appointment.setStatus(AppointmentStatus.UPCOMING);

		RegisteredShiftTimeSlotDto timeSlotDto = new RegisteredShiftTimeSlotDto();
		timeSlotDto.setId("test-time-slot-id");
		timeSlotDto.setStartTime(Instant.now().toString());
		timeSlotDto.setEndTime(Instant.now().plusSeconds(1800).toString());
		timeSlotDto.setIsAvailable(true);
		appointment.setRegisteredShiftTimeSlot(timeSlotDto);

		DoctorDto doctorDto = new DoctorDto();
		doctorDto.setId("test-doctor-id");
		doctorDto.setDescription("Test Doctor");
		appointment.setDoctor(doctorDto);

		PatientDto patientDto = new PatientDto();
		patientDto.setId("test-patient-id");
		appointment.setPatient(patientDto);

		return appointment;
	}
}
