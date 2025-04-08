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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Appointment Controller Tests")
@Tag("controller")
public class AppointmentControllerTest {
	private static final Logger logger = Logger.getLogger(AppointmentControllerTest.class.getName());
	private TestInfo testInfo;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AppointmentService appointmentService;

	@MockBean
	private JwtAuthenticationManager jwtAuthenticationManager;

	private static final String TEST_USER_ID = "test-user-id";
	private static final String TEST_APPOINTMENT_ID = "test-appointment-id";

	@BeforeEach
	void setUp(TestInfo testInfo) {
		this.testInfo = testInfo;
		logger.info("Starting test: " + testInfo.getDisplayName());

		// Mock JWT authentication
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
	}

	@AfterEach
	void tearDown() {
		logger.info("Completed test: " + testInfo.getDisplayName());
	}

	/**
	 * Test Case 1: Get My Appointments - Success
	 * Goal: Verify successful retrieval of patient's appointments
	 * Input: Valid JWT token, pagination parameters
	 * Expected Output: 200 OK with list of appointments
	 */
	@Test
	@Order(1)
	@Transactional
	@DisplayName("TC-AC-001: Get My Appointments - Success")
	void testGetMyAppointmentsSuccess() throws Exception {
		// Given
		PageableResultDto<AppointmentDto> mockResult = PageableResultDto.of(0, 0, 0, new ArrayList<>());
		List<AppointmentDto> appointments = new ArrayList<>();

		AppointmentDto appointment = createMockAppointment();
		appointments.add(appointment);

		mockResult.setContent(appointments);
		mockResult.setTotalElements(1);
		mockResult.setTotalPage(1);
		mockResult.setPageSize(10);


		when(appointmentService.findAllByPatientUserId(any(Pageable.class), eq(TEST_USER_ID), isNull()))
			.thenReturn(mockResult);

		// When/Then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilderHelper.get(ControllerPath.APPOINTMENT_CONTROLLER)
					.param("page", "0")
					.param("size", "10")
					.param("orderBy", "createdAt")
					.param("order", "desc")
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
	 * Test Case 2: Get My Appointments - With Status Filter
	 * Goal: Verify successful retrieval of patient's appointments with status filter
	 * Input: Valid JWT token, pagination parameters, status filter
	 * Expected Output: 200 OK with filtered list of appointments
	 */
	@Test
	@Order(2)
	@Transactional
	@DisplayName("TC-AC-002: Get My Appointments - With Status Filter")
	void testGetMyAppointmentsWithStatusFilter() throws Exception {
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


		when(appointmentService.findAllByPatientUserId(any(Pageable.class), eq(TEST_USER_ID), eq(AppointmentStatus.UPCOMING)))
			.thenReturn(mockResult);

		// When/Then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilderHelper.get(ControllerPath.APPOINTMENT_CONTROLLER)
					.param("page", "0")
					.param("size", "10")
					.param("orderBy", "createdAt")
					.param("order", "desc")
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
		verify(appointmentService).findAllByPatientUserId(any(Pageable.class), eq(TEST_USER_ID), eq(AppointmentStatus.UPCOMING));
	}

	/**
	 * Test Case 3: Get Appointment by ID - Success
	 * Goal: Verify successful retrieval of an appointment by ID
	 * Input: Valid JWT token, valid appointment ID
	 * Expected Output: 200 OK with appointment details
	 */
	@Test
	@Order(3)
	@Transactional
	@DisplayName("TC-AC-003: Get Appointment by ID - Success")
	void testGetAppointmentByIdSuccess() throws Exception {
		// Given
		AppointmentDto mockAppointment = createMockAppointment();
		when(appointmentService.findById(TEST_APPOINTMENT_ID)).thenReturn(mockAppointment);

		// When/Then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilderHelper.get(ControllerPath.APPOINTMENT_CONTROLLER + "/" + TEST_APPOINTMENT_ID)
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
		verify(appointmentService).findById(TEST_APPOINTMENT_ID);
	}

	/**
	 * Test Case 4: Create Appointment - Success
	 * Goal: Verify successful creation of an appointment
	 * Input: Valid JWT token, valid appointment data
	 * Expected Output: 200 OK with created appointment details
	 */
	@Test
	@Order(4)
	@Transactional
	@DisplayName("TC-AC-004: Create Appointment - Success")
	void testCreateAppointmentSuccess() throws Exception {
		// Given
		AppointmentDto inputAppointment = new AppointmentDto();
		RegisteredShiftTimeSlotDto timeSlotDto = new RegisteredShiftTimeSlotDto();
		timeSlotDto.setId("test-time-slot-id");
		inputAppointment.setRegisteredShiftTimeSlot(timeSlotDto);
		inputAppointment.setSymptoms("Test symptoms");

		AppointmentDto createdAppointment = createMockAppointment();
		when(appointmentService.create(any(AppointmentDto.class))).thenReturn(createdAppointment);

		// When/Then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilderHelper.post(ControllerPath.APPOINTMENT_CONTROLLER)
					.content(objectMapper.writeValueAsString(inputAppointment))
			)
			.andExpect(status().isOk())
			.andReturn();

		// Then
		String responseContent = result.getResponse().getContentAsString();
		ResponseDto responseDto = objectMapper.readValue(responseContent, ResponseDto.class);

		assertNotNull(responseDto, "Response should not be null");
		assertTrue(responseDto.isSuccess(), "Response should be successful");
		assertNotNull(responseDto.getData(), "Response data should not be null");

		// Verify service was called
		verify(appointmentService).create(any(AppointmentDto.class));
	}

	/**
	 * Test Case 5: Update Appointment - Success
	 * Goal: Verify successful update of an appointment
	 * Input: Valid JWT token, valid appointment ID, updated appointment data
	 * Expected Output: 200 OK with updated appointment details
	 */
	@Test
	@Order(5)
	@Transactional
	@DisplayName("TC-AC-005: Update Appointment - Success")
	void testUpdateAppointmentSuccess() throws Exception {
		// Given
		AppointmentDto inputAppointment = new AppointmentDto();
		RegisteredShiftTimeSlotDto timeSlotDto = new RegisteredShiftTimeSlotDto();
		timeSlotDto.setId("test-time-slot-id");
		inputAppointment.setRegisteredShiftTimeSlot(timeSlotDto);
		inputAppointment.setSymptoms("Updated symptoms");

		AppointmentDto updatedAppointment = createMockAppointment();
		updatedAppointment.setSymptoms("Updated symptoms");
		when(appointmentService.update(any(AppointmentDto.class))).thenReturn(updatedAppointment);

		// When/Then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilderHelper.put(ControllerPath.APPOINTMENT_CONTROLLER + "/" + TEST_APPOINTMENT_ID)
					.content(objectMapper.writeValueAsString(inputAppointment))
			)
			.andExpect(status().isOk())
			.andReturn();

		// Then
		String responseContent = result.getResponse().getContentAsString();
		ResponseDto responseDto = objectMapper.readValue(responseContent, ResponseDto.class);

		assertNotNull(responseDto, "Response should not be null");
		assertTrue(responseDto.isSuccess(), "Response should be successful");
		assertNotNull(responseDto.getData(), "Response data should not be null");

		// Verify service was called with ID set
		verify(appointmentService).update(argThat(dto -> TEST_APPOINTMENT_ID.equals(dto.getId())));
	}

	/**
	 * Test Case 6: Delete Appointment - Success
	 * Goal: Verify successful deletion of an appointment
	 * Input: Valid JWT token, valid appointment ID
	 * Expected Output: 200 OK with success response
	 */
	@Test
	@Order(6)
	@Transactional
	@DisplayName("TC-AC-006: Delete Appointment - Success")
	void testDeleteAppointmentSuccess() throws Exception {
		// Given
		doNothing().when(appointmentService).delete(TEST_APPOINTMENT_ID);

		// When/Then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilderHelper.delete(ControllerPath.APPOINTMENT_CONTROLLER + "/" + TEST_APPOINTMENT_ID)
			)
			.andExpect(status().isOk())
			.andReturn();

		// Then
		String responseContent = result.getResponse().getContentAsString();
		ResponseDto responseDto = objectMapper.readValue(responseContent, ResponseDto.class);

		assertNotNull(responseDto, "Response should not be null");
		assertTrue(responseDto.isSuccess(), "Response should be successful");

		// Verify service was called
		verify(appointmentService).delete(TEST_APPOINTMENT_ID);
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
