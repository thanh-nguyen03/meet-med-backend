package com.thanhnd.clinic_application.modules.shifts.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.UserGender;
import com.thanhnd.clinic_application.entity.*;
import com.thanhnd.clinic_application.helper.DateHelper;
import com.thanhnd.clinic_application.mapper.RegisteredShiftMapper;
import com.thanhnd.clinic_application.mapper.ShiftMapper;
import com.thanhnd.clinic_application.modules.doctors.repository.DoctorRepository;
import com.thanhnd.clinic_application.modules.rooms.repository.RoomRepository;
import com.thanhnd.clinic_application.modules.shifts.dto.CanRegisterShiftDto;
import com.thanhnd.clinic_application.modules.shifts.dto.ShiftDto;
import com.thanhnd.clinic_application.modules.shifts.repository.RegisteredShiftRepository;
import com.thanhnd.clinic_application.modules.shifts.repository.ShiftRepository;
import com.thanhnd.clinic_application.modules.shifts.service.ShiftService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Shift Service Implementation Tests")
@Tag("service")
public class ShiftServiceImplTest {
	private static final Logger logger = Logger.getLogger(ShiftServiceImplTest.class.getName());
	private TestInfo testInfo;

	@Autowired
	private ShiftService shiftService;

	@Autowired
	private ShiftRepository shiftRepository;

	@Autowired
	private RegisteredShiftRepository registeredShiftRepository;

	@Autowired
	private DoctorRepository doctorRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private ShiftMapper shiftMapper;

	@Autowired
	private RegisteredShiftMapper registeredShiftMapper;

	@MockBean
	private JwtAuthenticationManager jwtAuthenticationManager;

	@PersistenceContext
	private EntityManager entityManager;

	private ObjectMapper objectMapper;
	private final Map<String, Object> testInputs = new HashMap<>();
	private final Map<String, Object> testOutputs = new HashMap<>();
	private Exception testException = null;

	private String TEST_USER_ID;
	private String TEST_DOCTOR_ID;
	private String TEST_DEPARTMENT_ID;
	private String TEST_ROOM_ID;
	private String TEST_SHIFT_ID;

	private User testUser;
	private Doctor testDoctor;
	private Department testDepartment;
	private Room testRoom;
	private Shift testShift;

	@BeforeEach
	void setUp(TestInfo testInfo) {
		this.testInfo = testInfo;
		logger.info("Starting test: " + testInfo.getDisplayName());

		// Initialize object mapper for JSON serialization
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.findAndRegisterModules(); // For date/time support

		// Create test data
		setupTestData();
	}

	@AfterEach
	void tearDown() {
		logger.info("Completed test: " + testInfo.getDisplayName());

		// Log test inputs, outputs, and exceptions for debugging
		try {
			System.out.println("\n==========================");
			System.out.println("TEST: " + testInfo.getDisplayName());

			if (!testInputs.isEmpty()) {
				System.out.println("\nINPUTS:");
				System.out.println(objectMapper.writeValueAsString(testInputs));
			}

			if (!testOutputs.isEmpty()) {
				System.out.println("\nOUTPUTS:");
				System.out.println(objectMapper.writeValueAsString(testOutputs));
			}

			if (testException != null) {
				System.out.println("\nEXCEPTION:");
				System.out.println(testException.getClass().getName() + ": " + testException.getMessage());
			}
		} catch (Exception e) {
			System.out.println("Error printing test result: " + e.getMessage());
		}

		System.out.println("==========================\n");

		// Clear maps for next test
		testInputs.clear();
		testOutputs.clear();
		testException = null;
	}

	private void setupTestData() {
		// Create a test department
		testDepartment = new Department();
		testDepartment.setName("Test Department");
		testDepartment.setDescription("Test Department Description");
		testDepartment.setImageUrl("http://example.com/image.jpg");
		testDepartment = entityManager.merge(testDepartment);
		TEST_DEPARTMENT_ID = testDepartment.getId();

		// Create a test user
		testUser = new User();
		testUser.setEmail("shift-test@example.com");
		testUser.setFullName("Shift Test User");
		testUser.setAge(30);
		testUser.setPhone("5551234567");
		testUser.setGender(UserGender.Male);
		testUser = entityManager.merge(testUser);
		TEST_USER_ID = testUser.getId();

		// Create a test doctor
		testDoctor = new Doctor();
		testDoctor.setUser(testUser);
		testDoctor.setDepartment(testDepartment);
		testDoctor.setYearsOfExperience(5);
		testDoctor.setDegree("MD");
		testDoctor.setNumberOfPatients(50);
		testDoctor.setNumberOfCertificates(3);
		testDoctor.setDescription("Test doctor description");
		testDoctor = entityManager.merge(testDoctor);
		TEST_DOCTOR_ID = testDoctor.getId();

		// Create a test room
		testRoom = new Room();
		testRoom.setName("Test Room");
		testRoom.setDepartment(testDepartment);
		testRoom = entityManager.merge(testRoom);
		TEST_ROOM_ID = testRoom.getId();

		// Create a test shift
		testShift = new Shift();
		testShift.setStartTime(Instant.now().plusSeconds(86400)); // Tomorrow
		testShift.setEndTime(Instant.now().plusSeconds(100800)); // Tomorrow + 4 hours
		testShift = entityManager.merge(testShift);
		TEST_SHIFT_ID = testShift.getId();

		// Flush to ensure all entities are persisted
		entityManager.flush();
	}

	/**
	 * Test Case 1: Get Shift List - Success
	 * Goal: Verify successful retrieval of shifts within a date range
	 * Input: Valid start and end dates
	 * Expected Output: List of shifts within the date range
	 */
	@Test
	@Transactional
	@Order(1)
	@DisplayName("TC-SS-001: Get Shift List - Success")
	void testGetShiftListSuccess() {
		// Given
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusDays(7);
		testInputs.put("startDate", startDate);
		testInputs.put("endDate", endDate);

		// Create additional test shifts within the date range
		Shift shift1 = new Shift();
		shift1.setStartTime(DateHelper.getStartOfDay(startDate.plusDays(1)));
		shift1.setEndTime(DateHelper.getStartOfDay(startDate.plusDays(1)).plusSeconds(14400)); // +4 hours
		shift1 = entityManager.merge(shift1);

		Shift shift2 = new Shift();
		shift2.setStartTime(DateHelper.getStartOfDay(startDate.plusDays(2)));
		shift2.setEndTime(DateHelper.getStartOfDay(startDate.plusDays(2)).plusSeconds(14400)); // +4 hours
		shift2 = entityManager.merge(shift2);

		entityManager.flush();

		// When
		List<ShiftDto> result = shiftService.getShiftList(startDate, endDate);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertTrue(result.size() >= 2, "Should return at least 2 shifts");

		// Verify that shifts are within the date range
		for (ShiftDto shiftDto : result) {
			Instant shiftStart = shiftDto.getStartTime();
			LocalDate shiftDate = LocalDateTime.ofInstant(shiftStart, ZoneId.systemDefault()).toLocalDate();
			assertTrue(
				(shiftDate.isEqual(startDate) || shiftDate.isAfter(startDate)) &&
					(shiftDate.isEqual(endDate) || shiftDate.isBefore(endDate)),
				"Shift date should be within the requested range"
			);
		}
	}

	/**
	 * Test Case 2: Create Shift Table - Success
	 * Goal: Verify successful creation of shift table for a month
	 * Input: Valid month and year
	 * Expected Output: Shifts created for all days in the month
	 */
	@Test
	@Transactional
	@Order(2)
	@DisplayName("TC-SS-002: Create Shift Table - Success")
	void testCreateShiftTableSuccess() {
		// Given
		int month = LocalDate.now().getMonthValue() + 1; // Next month
		int year = LocalDate.now().getYear();
		testInputs.put("month", month);
		testInputs.put("year", year);

		// When
		shiftService.createShiftTable(month, year);

		// Then
		Instant firstDayOfMonth = DateHelper.getFirstDayOfMonth(month, year);
		Instant lastDayOfMonth = DateHelper.getLastDayOfMonth(month, year);

		List<Shift> createdShifts = shiftRepository.findAllByTimeBetween(firstDayOfMonth, lastDayOfMonth);
		testOutputs.put("createdShifts", createdShifts.size());

		// Calculate expected number of shifts (2 shifts per day * number of days in month)
		LocalDate date = LocalDate.of(year, month, 1);
		int daysInMonth = date.lengthOfMonth();
		int expectedShifts = daysInMonth * 2; // Morning and afternoon shifts

		assertEquals(expectedShifts, createdShifts.size(), "Should create 2 shifts for each day in the month");

		// Verify shift times
		for (Shift shift : createdShifts) {
			LocalDateTime shiftStart = LocalDateTime.ofInstant(shift.getStartTime(), ZoneId.systemDefault());
			int hour = shiftStart.getHour();

			// Check that shifts start at either 8 AM or 1 PM
			assertTrue(hour == ShiftServiceImpl.MORNING_START_HOUR || hour == ShiftServiceImpl.AFTERNOON_START_HOUR,
				"Shifts should start at either 8 AM or 1 PM");

			// Check shift duration (4 hours)
			long durationSeconds = shift.getEndTime().getEpochSecond() - shift.getStartTime().getEpochSecond();
			assertEquals(ShiftServiceImpl.MAX_SHIFT_HOUR * 3600, durationSeconds, "Shift duration should be 4 hours");
		}
	}

	/**
	 * Test Case 3: Create Shift Table - Already Created
	 * Goal: Verify error handling when trying to create shifts for a month that already has shifts
	 * Input: Month and year that already has shifts
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(3)
	@DisplayName("TC-SS-003: Create Shift Table - Already Created")
	void testCreateShiftTableAlreadyCreated() {
		// Given
		int month = LocalDate.now().getMonthValue() + 1; // Next month
		int year = LocalDate.now().getYear();
		testInputs.put("month", month);
		testInputs.put("year", year);

		// Create shifts for the month first
		shiftService.createShiftTable(month, year);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			shiftService.createShiftTable(month, year);
		});
		testException = exception;

		assertEquals(400, exception.getStatus().value(), "Should return 400 Bad Request");
		assertTrue(exception.getMessage().contains("Shift table for month " + month + " year " + year + " already created"),
			"Error message should indicate that shifts for the month are already created");
	}

	/**
	 * Test Case 4: Get List Shift Can Register - Success
	 * Goal: Verify successful retrieval of shifts that a doctor can register for
	 * Input: Authenticated doctor user
	 * Expected Output: List of shifts available for registration
	 */
	@Test
	@Transactional
	@Order(4)
	@DisplayName("TC-SS-004: Get List Shift Can Register - Success")
	void testGetListShiftCanRegisterSuccess() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
		testInputs.put("userId", TEST_USER_ID);

		// Create additional shifts for the next week
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		LocalDate nextWeek = tomorrow.plusDays(6);

		for (int i = 0; i < 5; i++) {
			Shift shift = new Shift();
			shift.setStartTime(DateHelper.getStartOfDay(tomorrow.plusDays(i)));
			shift.setEndTime(DateHelper.getStartOfDay(tomorrow.plusDays(i)).plusSeconds(14400)); // +4 hours
			entityManager.persist(shift);
		}

		entityManager.flush();

		// When
		List<CanRegisterShiftDto> result = shiftService.getListShiftCanRegister();
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertFalse(result.isEmpty(), "Should return available shifts");

		// Verify that shifts are within the next week
		for (CanRegisterShiftDto shiftDto : result) {
			Instant shiftStart = shiftDto.getStartTime();
			LocalDate shiftDate = LocalDateTime.ofInstant(shiftStart, ZoneId.systemDefault()).toLocalDate();
			assertTrue(
				(shiftDate.isEqual(tomorrow) || shiftDate.isAfter(tomorrow)) &&
					(shiftDate.isEqual(nextWeek) || shiftDate.isBefore(nextWeek)),
				"Shift date should be within the next week"
			);

			// Verify that remainingNumberOfRoomsAvailable is set
			assertNotNull(shiftDto.getRemainingNumberOfRoomsAvailable(), "Remaining rooms should be set");
		}
	}

	/**
	 * Test Case 5: Get List Shift Can Register - Doctor Not Found
	 * Goal: Verify error handling when the authenticated user is not a doctor
	 * Input: Authenticated user who is not a doctor
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(5)
	@DisplayName("TC-SS-005: Get List Shift Can Register - Doctor Not Found")
	void testGetListShiftCanRegisterDoctorNotFound() {
		// Given
		String nonDoctorUserId = "non-doctor-user";
		when(jwtAuthenticationManager.getUserId()).thenReturn(nonDoctorUserId);
		testInputs.put("userId", nonDoctorUserId);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			shiftService.getListShiftCanRegister();
		});
		testException = exception;

		assertEquals(403, exception.getStatus().value(), "Should return 403 Forbidden");
		assertTrue(exception.getMessage().contains("Permission denied"),
			"Error message should indicate permission denied");
	}

	/**
	 * Test Case 6: Get Week Shift Can Register - Current Week Success
	 * Goal: Verify successful retrieval of current week shifts
	 * Input: Authenticated doctor user, isNextWeek=false
	 * Expected Output: List of current week shifts
	 */
	@Test
	@Transactional
	@Order(6)
	@DisplayName("TC-SS-006: Get Week Shift Can Register - Current Week Success")
	void testGetWeekShiftCanRegisterCurrentWeekSuccess() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
		testInputs.put("userId", TEST_USER_ID);
		testInputs.put("isNextWeek", false);

		// Create shifts for the current week
		LocalDate startOfWeek = DateHelper.getStartOfWeek(LocalDate.now());
		LocalDate endOfWeek = DateHelper.getEndOfWeek(LocalDate.now());

		for (int i = 0; i < 5; i++) {
			Shift shift = new Shift();
			shift.setStartTime(DateHelper.getStartOfDay(startOfWeek.plusDays(i)));
			shift.setEndTime(DateHelper.getStartOfDay(startOfWeek.plusDays(i)).plusSeconds(14400)); // +4 hours
			shift = entityManager.merge(shift);

			// Create registered shift for this shift
			RegisteredShift registeredShift = new RegisteredShift();
			registeredShift.setDoctor(testDoctor);
			registeredShift.setShift(shift);
			registeredShift.setIsApproved(false);
			registeredShift.setMaxNumberOfPatients(16);
			registeredShift.setShiftPrice(100.0);
			registeredShift.setStartTime(shift.getStartTime());
			registeredShift.setEndTime(shift.getEndTime());
			entityManager.persist(registeredShift);
		}

		entityManager.flush();

		// When
		List<CanRegisterShiftDto> result = shiftService.getWeekShiftCanRegister(false);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertFalse(result.isEmpty(), "Should return registered shifts");

		// Verify that shifts are within the current week
		for (CanRegisterShiftDto shiftDto : result) {
			Instant shiftStart = shiftDto.getStartTime();
			LocalDate shiftDate = LocalDateTime.ofInstant(shiftStart, ZoneId.systemDefault()).toLocalDate();
			assertTrue(
				(shiftDate.isEqual(startOfWeek) || shiftDate.isAfter(startOfWeek)) &&
					(shiftDate.isEqual(endOfWeek) || shiftDate.isBefore(endOfWeek)),
				"Shift date should be within the current week"
			);

			// Verify that registeredShift is set
			assertNotNull(shiftDto.getRegisteredShift(), "Registered shift should be set");
		}
	}

	/**
	 * Test Case 7: Get Week Shift Can Register - Next Week Success
	 * Goal: Verify successful retrieval of next week shifts
	 * Input: Authenticated doctor user, isNextWeek=true
	 * Expected Output: List of next week shifts
	 */
	@Test
	@Transactional
	@Order(7)
	@DisplayName("TC-SS-007: Get Week Shift Can Register - Next Week Success")
	void testGetWeekShiftCanRegisterNextWeekSuccess() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
		testInputs.put("userId", TEST_USER_ID);
		testInputs.put("isNextWeek", true);

		// Create shifts for the next week
		LocalDate startOfNextWeek = DateHelper.getStartOfWeek(LocalDate.now().plusWeeks(1));
		LocalDate endOfNextWeek = DateHelper.getEndOfWeek(LocalDate.now().plusWeeks(1));

		for (int i = 0; i < 5; i++) {
			Shift shift = new Shift();
			shift.setStartTime(DateHelper.getStartOfDay(startOfNextWeek.plusDays(i)));
			shift.setEndTime(DateHelper.getStartOfDay(startOfNextWeek.plusDays(i)).plusSeconds(14400)); // +4 hours
			shift = entityManager.merge(shift);

			// Create registered shift for this shift
			RegisteredShift registeredShift = new RegisteredShift();
			registeredShift.setDoctor(testDoctor);
			registeredShift.setShift(shift);
			registeredShift.setIsApproved(false);
			registeredShift.setMaxNumberOfPatients(16);
			registeredShift.setShiftPrice(100.0);
			registeredShift.setStartTime(shift.getStartTime());
			registeredShift.setEndTime(shift.getEndTime());
			entityManager.persist(registeredShift);
		}

		entityManager.flush();

		// When
		List<CanRegisterShiftDto> result = shiftService.getWeekShiftCanRegister(true);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertFalse(result.isEmpty(), "Should return registered shifts");

		// Verify that shifts are within the next week
		for (CanRegisterShiftDto shiftDto : result) {
			Instant shiftStart = shiftDto.getStartTime();
			LocalDate shiftDate = LocalDateTime.ofInstant(shiftStart, ZoneId.systemDefault()).toLocalDate();
			assertTrue(
				(shiftDate.isEqual(startOfNextWeek) || shiftDate.isAfter(startOfNextWeek)) &&
					(shiftDate.isEqual(endOfNextWeek) || shiftDate.isBefore(endOfNextWeek)),
				"Shift date should be within the next week"
			);

			// Verify that registeredShift is set
			assertNotNull(shiftDto.getRegisteredShift(), "Registered shift should be set");
		}
	}

	/**
	 * Test Case 8: Get Week Shift Can Register - Doctor Not Found
	 * Goal: Verify error handling when the authenticated user is not a doctor
	 * Input: Authenticated user who is not a doctor, isNextWeek=false
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(8)
	@DisplayName("TC-SS-008: Get Week Shift Can Register - Doctor Not Found")
	void testGetWeekShiftCanRegisterDoctorNotFound() {
		// Given
		String nonDoctorUserId = "non-doctor-user";
		when(jwtAuthenticationManager.getUserId()).thenReturn(nonDoctorUserId);
		testInputs.put("userId", nonDoctorUserId);
		testInputs.put("isNextWeek", false);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			shiftService.getWeekShiftCanRegister(false);
		});
		testException = exception;

		assertEquals(403, exception.getStatus().value(), "Should return 403 Forbidden");
		assertTrue(exception.getMessage().contains("Permission denied"),
			"Error message should indicate permission denied");
	}

	/**
	 * Test Case 9: Get Shift List - Invalid Date Range
	 * Goal: Verify error handling when trying to get shifts with an invalid date range
	 * Input: End date before start date
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(9)
	@DisplayName("TC-SS-009: Get Shift List - Invalid Date Range")
	void testGetShiftListInvalidDateRange() {
		// Given
		LocalDate startDate = LocalDate.now().plusDays(7);
		LocalDate endDate = LocalDate.now(); // End date before start date
		testInputs.put("startDate", startDate);
		testInputs.put("endDate", endDate);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			shiftService.getShiftList(startDate, endDate);
		});
		testException = exception;

		assertEquals(400, exception.getStatus().value(), "Should return 400 Bad Request");
		assertTrue(exception.getMessage().contains("Invalid date range"),
			"Error message should indicate invalid date range");
	}

	/**
	 * Test Case 10: Create Shift Table - Invalid Month
	 * Goal: Verify error handling when trying to create shifts with an invalid month
	 * Input: Month = 13 (invalid)
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(10)
	@DisplayName("TC-SS-010: Create Shift Table - Invalid Month")
	void testCreateShiftTableInvalidMonth() {
		// Given
		int month = 13; // Invalid month
		int year = LocalDate.now().getYear();
		testInputs.put("month", month);
		testInputs.put("year", year);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			shiftService.createShiftTable(month, year);
		});
		testException = exception;

		assertEquals(400, exception.getStatus().value(), "Should return 400 Bad Request");
		assertTrue(exception.getMessage().contains("Invalid month"),
			"Error message should indicate invalid month");
	}

	/**
	 * Test Case 11: Get List Shift Can Register - No Available Shifts
	 * Goal: Verify behavior when no shifts are available for registration
	 * Input: Authenticated doctor user, but no shifts available
	 * Expected Output: Empty list of shifts
	 */
	@Test
	@Transactional
	@Order(11)
	@DisplayName("TC-SS-011: Get List Shift Can Register - No Available Shifts")
	void testGetListShiftCanRegisterNoAvailableShifts() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
		testInputs.put("userId", TEST_USER_ID);

		// Clear any existing shifts in the relevant time range
		LocalDate nextDay = LocalDate.now().plusDays(1);
		LocalDate nextWeek = nextDay.plusDays(6);
		Instant start = DateHelper.getStartOfDay(nextDay);
		Instant end = DateHelper.getEndOfDay(nextWeek);
		List<Shift> existingShifts = shiftRepository.findAllByTimeBetween(start, end);
		for (Shift shift : existingShifts) {
			entityManager.remove(shift);
		}
		entityManager.flush();

		// When
		List<CanRegisterShiftDto> result = shiftService.getListShiftCanRegister();
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertTrue(result.isEmpty(), "Should return an empty list when no shifts are available");
	}

	/**
	 * Test Case 12: Get Week Shift Can Register - No Registered Shifts
	 * Goal: Verify behavior when doctor has no registered shifts for the week
	 * Input: Authenticated doctor user, isNextWeek=false, but no registered shifts
	 * Expected Output: Empty list of shifts
	 */
	@Test
	@Transactional
	@Order(12)
	@DisplayName("TC-SS-012: Get Week Shift Can Register - No Registered Shifts")
	void testGetWeekShiftCanRegisterNoRegisteredShifts() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
		testInputs.put("userId", TEST_USER_ID);
		testInputs.put("isNextWeek", false);

		// Clear any existing registered shifts for the doctor
		List<RegisteredShift> existingRegisteredShifts = registeredShiftRepository.findAllByDoctorId(TEST_DOCTOR_ID);
		for (RegisteredShift registeredShift : existingRegisteredShifts) {
			entityManager.remove(registeredShift);
		}
		entityManager.flush();

		// When
		List<CanRegisterShiftDto> result = shiftService.getWeekShiftCanRegister(false);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		// The result might not be empty if there are shifts available for the week,
		// but none of them should have a registered shift for the doctor
		for (CanRegisterShiftDto shiftDto : result) {
			assertNull(shiftDto.getRegisteredShift(), "Should not have a registered shift");
			assertFalse(shiftDto.getIsApproved(), "Should not be approved");
		}
	}
}
