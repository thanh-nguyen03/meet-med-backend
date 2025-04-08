package com.thanhnd.clinic_application.modules.appointments.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.AppointmentStatus;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.constants.UserGender;
import com.thanhnd.clinic_application.entity.*;
import com.thanhnd.clinic_application.mapper.AppointmentMapper;
import com.thanhnd.clinic_application.modules.appointments.dto.AppointmentDto;
import com.thanhnd.clinic_application.modules.appointments.repository.AppointmentRepository;
import com.thanhnd.clinic_application.modules.doctors.repository.DoctorRepository;
import com.thanhnd.clinic_application.modules.patients.repository.PatientRepository;
import com.thanhnd.clinic_application.modules.shifts.dto.RegisteredShiftTimeSlotDto;
import com.thanhnd.clinic_application.modules.shifts.repository.RegisteredShiftRepository;
import com.thanhnd.clinic_application.modules.shifts.repository.RegisteredShiftTimeSlotRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Appointment Service Tests")
@Tag("service")
public class AppointmentServiceTest {
	private static final Logger logger = Logger.getLogger(AppointmentServiceTest.class.getName());
	private TestInfo testInfo;

	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DoctorRepository doctorRepository;

	@Autowired
	private RegisteredShiftTimeSlotRepository registeredShiftTimeSlotRepository;

	@Autowired
	private RegisteredShiftRepository registeredShiftRepository;

	@Autowired
	private AppointmentMapper appointmentMapper;

	@MockBean
	private JwtAuthenticationManager jwtAuthenticationManager;

	@PersistenceContext
	private EntityManager entityManager;

	private ObjectMapper objectMapper;
	private Map<String, Object> testInputs;
	private Map<String, Object> testOutputs;
	private HttpException testException;

	// Test data
	private static String TEST_USER_ID;
	private static String TEST_DOCTOR_USER_ID;
	private static String TEST_APPOINTMENT_ID;
	private User testUser;
	private User testDoctorUser;
	private Patient testPatient;
	private Doctor testDoctor;
	private Department testDepartment;
	private Room testRoom;
	private Shift testShift;
	private RegisteredShift testRegisteredShift;
	private RegisteredShiftTimeSlot testTimeSlot;
	private Appointment testAppointment;

	@BeforeEach
	void setUp(TestInfo testInfo) {
		this.testInfo = testInfo;
		logger.info("Starting test: " + testInfo.getDisplayName());

		// Initialize object mapper for JSON serialization
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.findAndRegisterModules(); // For date/time support

		// Initialize test data maps
		testInputs = new HashMap<>();
		testOutputs = new HashMap<>();

		// Create test data
		setupTestData();
	}

	@AfterEach
	void tearDown() {
		logger.info("Completed test: " + testInfo.getDisplayName());

		// Log test inputs and outputs for debugging
		try {
			if (!testInputs.isEmpty()) {
				logger.info("Test inputs: " + objectMapper.writeValueAsString(testInputs));
			}
			if (!testOutputs.isEmpty()) {
				logger.info("Test outputs: " + objectMapper.writeValueAsString(testOutputs));
			}
			if (testException != null) {
				logger.info("Test exception: " + testException.getMessage());
			}
		} catch (Exception e) {
			logger.warning("Error logging test data: " + e.getMessage());
		}
	}

	private void setupTestData() {
		// Create test user for patient
		testUser = new User();
		testUser.setEmail("test-patient@example.com");
		testUser.setFullName("Test Patient");
		testUser.setAge(30);
		testUser.setPhone("1234567890");
		testUser.setGender(UserGender.Male);
		testUser = entityManager.merge(testUser);
		TEST_USER_ID = testUser.getId();

		// Create test patient
		testPatient = new Patient();
		testPatient.setUser(testUser);
		testPatient = entityManager.merge(testPatient);

		// Create test user for doctor
		testDoctorUser = new User();
		testDoctorUser.setEmail("test-doctor@example.com");
		testDoctorUser.setFullName("Test Doctor");
		testDoctorUser.setAge(40);
		testDoctorUser.setPhone("0987654321");
		testDoctorUser.setGender(UserGender.Male);
		testDoctorUser = entityManager.merge(testDoctorUser);
		TEST_DOCTOR_USER_ID = testDoctorUser.getId();

		// Create test department
		testDepartment = new Department();
		testDepartment.setName("Test Department");
		testDepartment.setDescription("Test Department Description");
		testDepartment.setImageUrl("https://example.com/image.jpg");
		testDepartment = entityManager.merge(testDepartment);

		// Create test doctor
		testDoctor = new Doctor();
		testDoctor.setUser(testDoctorUser);
		testDoctor.setDepartment(testDepartment);
		testDoctor.setDescription("Test Doctor Description");
		testDoctor.setYearsOfExperience(10);
		testDoctor.setDegree("MD");
		testDoctor.setNumberOfPatients(100);
		testDoctor.setNumberOfCertificates(5);
		testDoctor.getUser().setImageUrl("https://example.com/doctor.jpg");
		testDoctor = entityManager.merge(testDoctor);

		// Create test room
		testRoom = new Room();
		testRoom.setName("Test Room");
		testRoom.setDepartment(testDepartment);
		testRoom = entityManager.merge(testRoom);

		// Create test shift
		testShift = new Shift();
		testShift.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
		testShift.setEndTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));
		testShift = entityManager.merge(testShift);

		// Create test registered shift
		testRegisteredShift = new RegisteredShift();
		testRegisteredShift.setShift(testShift);
		testRegisteredShift.setDoctor(testDoctor);
		testRegisteredShift.setRoom(testRoom);
		testRegisteredShift.setShiftPrice(100.0);
		testRegisteredShift.setMaxNumberOfPatients(10);
		testRegisteredShift.setIsApproved(true);
		testRegisteredShift.setStartTime(testShift.getStartTime());
		testRegisteredShift.setEndTime(testShift.getEndTime());
		testRegisteredShift = entityManager.merge(testRegisteredShift);

		// Create test time slot
		testTimeSlot = new RegisteredShiftTimeSlot();
		testTimeSlot.setRegisteredShift(testRegisteredShift);
		testTimeSlot.setStartTime(testShift.getStartTime());
		testTimeSlot.setEndTime(testShift.getStartTime().plus(30, ChronoUnit.MINUTES));
		testTimeSlot.setIsAvailable(true);
		testTimeSlot = entityManager.merge(testTimeSlot);

		// Create test appointment
		testAppointment = new Appointment();
		testAppointment.setPatient(testPatient);
		testAppointment.setRegisteredShiftTimeSlot(testTimeSlot);
		testAppointment.setSymptoms("Test symptoms");
		testAppointment.setStatus(AppointmentStatus.UPCOMING);
		testAppointment = entityManager.merge(testAppointment);
		TEST_APPOINTMENT_ID = testAppointment.getId();

		entityManager.flush();
	}

	/**
	 * Test Case 1: Find Appointment by ID - Success
	 * Goal: Verify successful retrieval of an appointment by ID
	 * Input: Valid appointment ID
	 * Expected Output: AppointmentDto with matching ID
	 */
	@Test
	@Order(1)
	@Transactional
	@DisplayName("TC-AS-001: Find Appointment by ID - Success")
	void testFindByIdSuccess() {
		// Given
		String appointmentId = TEST_APPOINTMENT_ID;
		testInputs.put("appointmentId", appointmentId);

		// When
		AppointmentDto result = appointmentService.findById(appointmentId);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertEquals(appointmentId, result.getId(), "Appointment ID should match");
		assertEquals(testAppointment.getSymptoms(), result.getSymptoms(), "Symptoms should match");
		assertEquals(testAppointment.getStatus(), result.getStatus(), "Status should match");
		assertNotNull(result.getRegisteredShiftTimeSlot(), "Time slot should not be null");
		assertEquals(testTimeSlot.getId(), result.getRegisteredShiftTimeSlot().getId(), "Time slot ID should match");
	}

	/**
	 * Test Case 2: Find Appointment by ID - Not Found
	 * Goal: Verify proper error handling when appointment ID doesn't exist
	 * Input: Non-existent appointment ID
	 * Expected Output: HttpException with NOT_FOUND status and "Appointment not found" message
	 */
	@Test
	@Order(2)
	@Transactional
	@DisplayName("TC-AS-002: Find Appointment by ID - Not Found")
	void testFindByIdNotFound() {
		// Given
		String nonExistentId = "non-existent-id";
		testInputs.put("appointmentId", nonExistentId);

		// When/Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			appointmentService.findById(nonExistentId);
		});
		testException = exception;

		// Then
		assertEquals(404, exception.getStatus().value(), "Status should be NOT_FOUND (404)");
		assertTrue(exception.getMessage().contains(Message.APPOINTMENT_NOT_FOUND.getMessage()),
			"Error message should indicate appointment not found");
	}

	/**
	 * Test Case 3: Find Appointments by Patient User ID - Success
	 * Goal: Verify successful retrieval of appointments for a patient
	 * Input: Valid patient user ID, pageable request
	 * Expected Output: PageableResultDto containing the patient's appointments
	 */
	@Test
	@Order(3)
	@Transactional
	@DisplayName("TC-AS-003: Find Appointments by Patient User ID - Success")
	void testFindAllByPatientUserIdSuccess() {
		// Given
		String userId = TEST_USER_ID;
		Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
		testInputs.put("userId", userId);
		testInputs.put("pageable", Map.of("page", 0, "size", 10, "sort", "createdAt,desc"));

		// When
		PageableResultDto<AppointmentDto> result = appointmentService.findAllByPatientUserId(pageable, userId, null);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertFalse(result.getContent().isEmpty(), "Result should contain appointments");
		assertEquals(1, result.getTotalElements(), "Should have 1 appointment for the patient");

		AppointmentDto appointmentDto = result.getContent().get(0);
		assertEquals(TEST_APPOINTMENT_ID, appointmentDto.getId(), "Appointment ID should match");
		assertEquals(testAppointment.getSymptoms(), appointmentDto.getSymptoms(), "Symptoms should match");
	}

	/**
	 * Test Case 4: Find Appointments by Patient User ID - With Status Filter
	 * Goal: Verify successful retrieval of appointments for a patient with status filter
	 * Input: Valid patient user ID, pageable request, status filter
	 * Expected Output: PageableResultDto containing the patient's appointments with matching status
	 */
	@Test
	@Order(4)
	@Transactional
	@DisplayName("TC-AS-004: Find Appointments by Patient User ID - With Status Filter")
	void testFindAllByPatientUserIdWithStatusFilter() {
		// Given
		String userId = TEST_USER_ID;
		Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
		AppointmentStatus status = AppointmentStatus.UPCOMING;
		testInputs.put("userId", userId);
		testInputs.put("pageable", Map.of("page", 0, "size", 10, "sort", "createdAt,desc"));
		testInputs.put("status", status);

		// When
		PageableResultDto<AppointmentDto> result = appointmentService.findAllByPatientUserId(pageable, userId, status);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertFalse(result.getContent().isEmpty(), "Result should contain appointments");

		for (AppointmentDto appointmentDto : result.getContent()) {
			assertEquals(status, appointmentDto.getStatus(), "Appointment status should match the filter");
		}
	}

	/**
	 * Test Case 5: Find Appointments for Doctor - Success
	 * Goal: Verify successful retrieval of appointments for a doctor
	 * Input: Valid doctor user ID, pageable request
	 * Expected Output: PageableResultDto containing the doctor's appointments
	 */
	@Test
	@Order(5)
	@Transactional
	@DisplayName("TC-AS-005: Find Appointments for Doctor - Success")
	void testFindAllForDoctorSuccess() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_DOCTOR_USER_ID);
		Pageable pageable = PageRequest.of(0, 10, Sort.by("registeredShiftTimeSlot.startTime").ascending());
		testInputs.put("doctorUserId", TEST_DOCTOR_USER_ID);
		testInputs.put("pageable", Map.of("page", 0, "size", 10, "sort", "registeredShiftTimeSlot.startTime,asc"));

		// When
		PageableResultDto<AppointmentDto> result = appointmentService.findAllForDoctor(pageable, null, null);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertFalse(result.getContent().isEmpty(), "Result should contain appointments");

		for (AppointmentDto appointmentDto : result.getContent()) {
			assertNotNull(appointmentDto.getDoctor(), "Doctor should not be null");
			assertEquals(testDoctor.getId(), appointmentDto.getDoctor().getId(), "Doctor ID should match");
		}
	}

	/**
	 * Test Case 6: Create Appointment - Success
	 * Goal: Verify successful creation of an appointment
	 * Input: Valid appointment data
	 * Expected Output: Created AppointmentDto
	 */
	@Test
	@Order(6)
	@Transactional
	@DisplayName("TC-AS-006: Create Appointment - Success")
	void testCreateAppointmentSuccess() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);

		// Create a new time slot for this test
		RegisteredShiftTimeSlot newTimeSlot = new RegisteredShiftTimeSlot();
		newTimeSlot.setRegisteredShift(testRegisteredShift);
		newTimeSlot.setStartTime(testShift.getStartTime().plus(1, ChronoUnit.HOURS));
		newTimeSlot.setEndTime(testShift.getStartTime().plus(1, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES));
		newTimeSlot.setIsAvailable(true);
		newTimeSlot = entityManager.merge(newTimeSlot);
		entityManager.flush();

		AppointmentDto appointmentDto = new AppointmentDto();
		RegisteredShiftTimeSlotDto timeSlotDto = new RegisteredShiftTimeSlotDto();
		timeSlotDto.setId(newTimeSlot.getId());
		appointmentDto.setRegisteredShiftTimeSlot(timeSlotDto);
		appointmentDto.setSymptoms("New test symptoms");

		testInputs.put("appointmentDto", appointmentDto);

		// When
		AppointmentDto result = appointmentService.create(appointmentDto);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertNotNull(result.getId(), "ID should be assigned");
		assertEquals(appointmentDto.getSymptoms(), result.getSymptoms(), "Symptoms should match");
		assertEquals(AppointmentStatus.UPCOMING, result.getStatus(), "Status should be UPCOMING");
		assertNotNull(result.getRegisteredShiftTimeSlot(), "Time slot should not be null");
		assertEquals(newTimeSlot.getId(), result.getRegisteredShiftTimeSlot().getId(), "Time slot ID should match");
	}

	/**
	 * Test Case 7: Create Appointment - Time Slot Not Found
	 * Goal: Verify proper error handling when time slot doesn't exist
	 * Input: Appointment with non-existent time slot
	 * Expected Output: HttpException with NOT_FOUND status and "Time slot not found" message
	 */
	@Test
	@Order(7)
	@Transactional
	@DisplayName("TC-AS-007: Create Appointment - Time Slot Not Found")
	void testCreateAppointmentTimeSlotNotFound() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);

		AppointmentDto appointmentDto = new AppointmentDto();
		RegisteredShiftTimeSlotDto timeSlotDto = new RegisteredShiftTimeSlotDto();
		timeSlotDto.setId("non-existent-time-slot");
		appointmentDto.setRegisteredShiftTimeSlot(timeSlotDto);
		appointmentDto.setSymptoms("Test symptoms");

		testInputs.put("appointmentDto", appointmentDto);

		// When/Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			appointmentService.create(appointmentDto);
		});
		testException = exception;

		// Then
		assertEquals(404, exception.getStatus().value(), "Status should be NOT_FOUND (404)");
		assertTrue(exception.getMessage().contains(Message.TIME_SLOT_NOT_FOUND.getMessage()),
			"Error message should indicate time slot not found");
	}

	/**
	 * Test Case 8: Create Appointment - Shift Not Approved
	 * Goal: Verify proper error handling when the shift is not approved
	 * Input: Appointment with time slot from unapproved shift
	 * Expected Output: HttpException with BAD_REQUEST status and "Shift not found" message
	 */
	@Test
	@Order(8)
	@Transactional
	@DisplayName("TC-AS-008: Create Appointment - Shift Not Approved")
	void testCreateAppointmentShiftNotApproved() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);

		// Create unapproved shift
		RegisteredShift unapprovedShift = new RegisteredShift();
		unapprovedShift.setShift(testShift);
		unapprovedShift.setDoctor(testDoctor);
		unapprovedShift.setRoom(testRoom);
		unapprovedShift.setShiftPrice(100.0);
		unapprovedShift.setMaxNumberOfPatients(10);
		unapprovedShift.setIsApproved(false); // Not approved
		unapprovedShift.setStartTime(testShift.getStartTime());
		unapprovedShift.setEndTime(testShift.getEndTime());
		unapprovedShift = entityManager.merge(unapprovedShift);

		// Create time slot for unapproved shift
		RegisteredShiftTimeSlot unapprovedTimeSlot = new RegisteredShiftTimeSlot();
		unapprovedTimeSlot.setRegisteredShift(unapprovedShift);
		unapprovedTimeSlot.setStartTime(testShift.getStartTime().plus(2, ChronoUnit.HOURS));
		unapprovedTimeSlot.setEndTime(testShift.getStartTime().plus(2, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES));
		unapprovedTimeSlot.setIsAvailable(true);
		unapprovedTimeSlot = entityManager.merge(unapprovedTimeSlot);
		entityManager.flush();

		AppointmentDto appointmentDto = new AppointmentDto();
		RegisteredShiftTimeSlotDto timeSlotDto = new RegisteredShiftTimeSlotDto();
		timeSlotDto.setId(unapprovedTimeSlot.getId());
		appointmentDto.setRegisteredShiftTimeSlot(timeSlotDto);
		appointmentDto.setSymptoms("Test symptoms");

		testInputs.put("appointmentDto", appointmentDto);

		// When/Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			appointmentService.create(appointmentDto);
		});
		testException = exception;

		// Then
		assertEquals(400, exception.getStatus().value(), "Status should be BAD_REQUEST (400)");
		assertTrue(exception.getMessage().contains(Message.SHIFT_NOT_FOUND.getMessage()),
			"Error message should indicate shift not found");
	}

	/**
	 * Test Case 9: Update Appointment Status - Success
	 * Goal: Verify successful update of appointment status
	 * Input: Valid appointment ID and status
	 * Expected Output: Updated AppointmentDto with new status
	 */
	@Test
	@Order(9)
	@Transactional
	@DisplayName("TC-AS-009: Update Appointment Status - Success")
	void testUpdateAppointmentStatusSuccess() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_DOCTOR_USER_ID);

		String appointmentId = TEST_APPOINTMENT_ID;
		AppointmentStatus newStatus = AppointmentStatus.COMPLETED;

		testInputs.put("appointmentId", appointmentId);
		testInputs.put("status", newStatus);

		// When
		AppointmentDto result = appointmentService.updateStatus(appointmentId, newStatus);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertEquals(appointmentId, result.getId(), "Appointment ID should match");
		assertEquals(newStatus, result.getStatus(), "Status should be updated");
	}

	/**
	 * Test Case 10: Update Appointment Status - Appointment Not Found
	 * Goal: Verify proper error handling when appointment doesn't exist
	 * Input: Non-existent appointment ID
	 * Expected Output: HttpException with NOT_FOUND status and "Appointment not found" message
	 */
	@Test
	@Order(10)
	@Transactional
	@DisplayName("TC-AS-010: Update Appointment Status - Appointment Not Found")
	void testUpdateAppointmentStatusNotFound() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_DOCTOR_USER_ID);

		String nonExistentId = "non-existent-id";
		AppointmentStatus newStatus = AppointmentStatus.COMPLETED;

		testInputs.put("appointmentId", nonExistentId);
		testInputs.put("status", newStatus);

		// When/Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			appointmentService.updateStatus(nonExistentId, newStatus);
		});
		testException = exception;

		// Then
		assertEquals(404, exception.getStatus().value(), "Status should be NOT_FOUND (404)");
		assertTrue(exception.getMessage().contains(Message.APPOINTMENT_NOT_FOUND.getMessage()),
			"Error message should indicate appointment not found");
	}

	/**
	 * Test Case 11: Delete Appointment - Success
	 * Goal: Verify successful deletion of an appointment
	 * Input: Valid appointment ID
	 * Expected Output: No exception thrown, appointment removed from database
	 */
	@Test
	@Order(11)
	@Transactional
	@DisplayName("TC-AS-011: Delete Appointment - Success")
	void testDeleteAppointmentSuccess() {
		// Given
		String appointmentId = TEST_APPOINTMENT_ID;
		testInputs.put("appointmentId", appointmentId);

		// When
		appointmentService.delete(appointmentId);

		// Then
		assertFalse(appointmentRepository.existsById(appointmentId), "Appointment should be deleted");
	}

	/**
	 * Test Case 12: Delete Appointment - Not Found
	 * Goal: Verify proper error handling when trying to delete non-existent appointment
	 * Input: Non-existent appointment ID
	 * Expected Output: HttpException with NOT_FOUND status and "Appointment not found" message
	 */
	@Test
	@Order(12)
	@Transactional
	@DisplayName("TC-AS-012: Delete Appointment - Not Found")
	void testDeleteAppointmentNotFound() {
		// Given
		String nonExistentId = "non-existent-id";
		testInputs.put("appointmentId", nonExistentId);

		// When/Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			appointmentService.delete(nonExistentId);
		});
		testException = exception;

		// Then
		assertEquals(404, exception.getStatus().value(), "Status should be NOT_FOUND (404)");
		assertTrue(exception.getMessage().contains(Message.APPOINTMENT_NOT_FOUND.getMessage()),
			"Error message should indicate appointment not found");
	}
}
