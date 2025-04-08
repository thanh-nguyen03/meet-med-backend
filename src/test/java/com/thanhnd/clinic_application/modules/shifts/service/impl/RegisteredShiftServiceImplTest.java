package com.thanhnd.clinic_application.modules.shifts.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.UserGender;
import com.thanhnd.clinic_application.entity.*;
import com.thanhnd.clinic_application.mapper.RegisteredShiftMapper;
import com.thanhnd.clinic_application.modules.doctors.repository.DoctorRepository;
import com.thanhnd.clinic_application.modules.doctors.service.DoctorService;
import com.thanhnd.clinic_application.modules.rooms.dto.RoomDto;
import com.thanhnd.clinic_application.modules.rooms.repository.RoomRepository;
import com.thanhnd.clinic_application.modules.shifts.dto.RegisteredShiftDto;
import com.thanhnd.clinic_application.modules.shifts.dto.SearchShiftForBookingResultDto;
import com.thanhnd.clinic_application.modules.shifts.dto.request.ApproveRegisteredShiftRequestDto;
import com.thanhnd.clinic_application.modules.shifts.dto.request.RegisterShiftRequestDto;
import com.thanhnd.clinic_application.modules.shifts.repository.RegisteredShiftRepository;
import com.thanhnd.clinic_application.modules.shifts.repository.RegisteredShiftTimeSlotRepository;
import com.thanhnd.clinic_application.modules.shifts.repository.ShiftRepository;
import com.thanhnd.clinic_application.modules.shifts.service.RegisteredShiftService;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Registered Shift Service Implementation Tests")
@Tag("service")
public class RegisteredShiftServiceImplTest {
	private static final Logger logger = Logger.getLogger(RegisteredShiftServiceImplTest.class.getName());
	private TestInfo testInfo;

	@Autowired
	private RegisteredShiftService registeredShiftService;

	@Autowired
	private RegisteredShiftRepository registeredShiftRepository;

	@Autowired
	private RegisteredShiftTimeSlotRepository registeredShiftTimeSlotRepository;

	@Autowired
	private ShiftRepository shiftRepository;

	@Autowired
	private DoctorRepository doctorRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private RegisteredShiftMapper registeredShiftMapper;

	@MockBean
	private JwtAuthenticationManager jwtAuthenticationManager;

	@MockBean
	private DoctorService doctorService;

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
	private String TEST_REGISTERED_SHIFT_ID;

	private User testUser;
	private Doctor testDoctor;
	private Department testDepartment;
	private Room testRoom;
	private Shift testShift;
	private RegisteredShift testRegisteredShift;

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
		testUser.setEmail("registered-shift-test@example.com");
		testUser.setFullName("Registered Shift Test User");
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

		// Create a test shift (future shift)
		testShift = new Shift();
		testShift.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS)); // Tomorrow
		testShift.setEndTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS)); // Tomorrow + 4 hours
		testShift = entityManager.merge(testShift);
		TEST_SHIFT_ID = testShift.getId();

		// Create a test registered shift
		testRegisteredShift = new RegisteredShift();
		testRegisteredShift.setDoctor(testDoctor);
		testRegisteredShift.setShift(testShift);
		testRegisteredShift.setIsApproved(false);
		testRegisteredShift.setMaxNumberOfPatients(16);
		testRegisteredShift.setShiftPrice(100.0);
		testRegisteredShift.setStartTime(testShift.getStartTime());
		testRegisteredShift.setEndTime(testShift.getEndTime());
		testRegisteredShift = entityManager.merge(testRegisteredShift);
		TEST_REGISTERED_SHIFT_ID = testRegisteredShift.getId();

		// Flush to ensure all entities are persisted
		entityManager.flush();
	}

	/**
	 * Test Case 1: Find All By Doctor For Booking - Success
	 * Goal: Verify successful retrieval of shifts available for booking by doctor ID
	 * Input: Valid doctor ID
	 * Expected Output: List of shifts available for booking
	 */
	@Test
	@Transactional
	@Order(1)
	@DisplayName("TC-RSS-001: Find All By Doctor For Booking - Success")
	void testFindAllByDoctorForBookingSuccess() {
		// Given
		String doctorId = TEST_DOCTOR_ID;
		testInputs.put("doctorId", doctorId);

		// Create additional registered shifts for the doctor
		for (int i = 0; i < 3; i++) {
			Shift shift = new Shift();
			shift.setStartTime(Instant.now().plus(i + 2, ChronoUnit.DAYS)); // Future shifts
			shift.setEndTime(Instant.now().plus(i + 2, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));
			shift = entityManager.merge(shift);

			RegisteredShift registeredShift = new RegisteredShift();
			registeredShift.setDoctor(testDoctor);
			registeredShift.setShift(shift);
			registeredShift.setIsApproved(true); // Approved shifts
			registeredShift.setRoom(testRoom);
			registeredShift.setMaxNumberOfPatients(16);
			registeredShift.setShiftPrice(100.0);
			registeredShift.setStartTime(shift.getStartTime());
			registeredShift.setEndTime(shift.getEndTime());
			entityManager.persist(registeredShift);
		}

		entityManager.flush();

		// When
		List<SearchShiftForBookingResultDto> result = registeredShiftService.findAllByDoctorForBooking(doctorId);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertFalse(result.isEmpty(), "Should return available shifts");

		// Verify that all shifts are for the specified doctor
		for (SearchShiftForBookingResultDto dto : result) {
			assertEquals(doctorId, dto.getDoctorId(), "Shift should be for the specified doctor");
			assertNotNull(dto.getShift(), "Shift should be set");
			assertNotNull(dto.getShift().getId(), "Shift ID should be set");
			assertNotNull(dto.getShift().getStartTime(), "Start time should be set");
			assertNotNull(dto.getShift().getEndTime(), "End time should be set");
			assertTrue(dto.getShift().getStartTime().isAfter(Instant.now()), "Shift should be in the future");
		}
	}

	/**
	 * Test Case 2: Find By ID - Success
	 * Goal: Verify successful retrieval of a registered shift by ID
	 * Input: Valid registered shift ID
	 * Expected Output: RegisteredShiftDto with matching ID
	 */
	@Test
	@Transactional
	@Order(2)
	@DisplayName("TC-RSS-002: Find By ID - Success")
	void testFindByIdSuccess() {
		// Given
		String registeredShiftId = TEST_REGISTERED_SHIFT_ID;
		testInputs.put("registeredShiftId", registeredShiftId);

		// When
		RegisteredShiftDto result = registeredShiftService.findById(registeredShiftId);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertEquals(registeredShiftId, result.getId(), "Should return the correct registered shift");
		assertNotNull(result.getShift(), "Shift should be included");
		assertNotNull(result.getDoctor(), "Doctor should be included");
		assertEquals(TEST_DOCTOR_ID, result.getDoctor().getId(), "Should have the correct doctor");
		assertEquals(TEST_SHIFT_ID, result.getShift().getId(), "Should have the correct shift");
		assertEquals(false, result.getIsApproved(), "Should have the correct approval status");
	}

	/**
	 * Test Case 3: Find By ID - Not Found
	 * Goal: Verify error handling when trying to find a non-existent registered shift
	 * Input: Non-existent registered shift ID
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(3)
	@DisplayName("TC-RSS-003: Find By ID - Not Found")
	void testFindByIdNotFound() {
		// Given
		String nonExistentId = "non-existent-id";
		testInputs.put("registeredShiftId", nonExistentId);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			registeredShiftService.findById(nonExistentId);
		});
		testException = exception;

		assertEquals(404, exception.getStatus().value(), "Should return 404 Not Found");
		assertTrue(exception.getMessage().contains("Registered shift not found"),
			"Error message should indicate that the registered shift was not found");
	}

	/**
	 * Test Case 4: Create Registered Shifts - Success
	 * Goal: Verify successful creation of registered shifts
	 * Input: Valid list of shift IDs to register, authenticated doctor user
	 * Expected Output: List of created RegisteredShiftDto objects
	 */
	@Test
	@Transactional
	@Order(4)
	@DisplayName("TC-RSS-004: Create Registered Shifts - Success")
	void testCreateRegisteredShiftsSuccess() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
		when(doctorService.calculateDoctorShiftPrice(any(Doctor.class))).thenReturn(120.0);
		testInputs.put("userId", TEST_USER_ID);

		// Create additional shifts to register
		List<RegisterShiftRequestDto> requestDtos = new ArrayList<>();
		List<String> shiftIds = new ArrayList<>();

		for (int i = 0; i < 3; i++) {
			Shift shift = new Shift();
			shift.setStartTime(Instant.now().plus(i + 2, ChronoUnit.DAYS)); // Future shifts
			shift.setEndTime(Instant.now().plus(i + 2, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));
			shift = entityManager.merge(shift);

			RegisterShiftRequestDto requestDto = new RegisterShiftRequestDto();
			requestDto.setId(shift.getId());
			requestDtos.add(requestDto);
			shiftIds.add(shift.getId());
		}

		testInputs.put("requestDtos", requestDtos);
		testInputs.put("shiftIds", shiftIds);

		entityManager.flush();

		// When
		List<RegisteredShiftDto> result = registeredShiftService.create(requestDtos);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertEquals(requestDtos.size(), result.size(), "Should create the correct number of registered shifts");

		// Verify that all shifts were registered correctly
		for (int i = 0; i < result.size(); i++) {
			RegisteredShiftDto dto = result.get(i);
			String shiftId = shiftIds.get(i);

			assertNotNull(dto.getId(), "Registered shift ID should be set");
			assertEquals(shiftId, dto.getShift().getId(), "Should be registered for the correct shift");
			assertEquals(TEST_DOCTOR_ID, dto.getDoctor().getId(), "Should be registered for the correct doctor");
			assertEquals(false, dto.getIsApproved(), "Should not be approved initially");
			assertEquals(120.0, dto.getShiftPrice(), "Should have the correct shift price");
			assertNotNull(dto.getTimeSlots(), "Time slots should be created");
			assertFalse(dto.getTimeSlots().isEmpty(), "Time slots should not be empty");
		}
	}

	/**
	 * Test Case 5: Create Registered Shifts - Doctor Not Found
	 * Goal: Verify error handling when the authenticated user is not a doctor
	 * Input: Valid list of shift IDs to register, authenticated user who is not a doctor
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(5)
	@DisplayName("TC-RSS-005: Create Registered Shifts - Doctor Not Found")
	void testCreateRegisteredShiftsDoctorNotFound() {
		// Given
		String nonDoctorUserId = "non-doctor-user";
		when(jwtAuthenticationManager.getUserId()).thenReturn(nonDoctorUserId);
		testInputs.put("userId", nonDoctorUserId);

		List<RegisterShiftRequestDto> requestDtos = new ArrayList<>();
		RegisterShiftRequestDto requestDto = new RegisterShiftRequestDto();
		requestDto.setId(TEST_SHIFT_ID);
		requestDtos.add(requestDto);
		testInputs.put("requestDtos", requestDtos);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			registeredShiftService.create(requestDtos);
		});
		testException = exception;

		assertEquals(403, exception.getStatus().value(), "Should return 403 Forbidden");
		assertTrue(exception.getMessage().contains("Permission denied"),
			"Error message should indicate permission denied");
	}

	/**
	 * Test Case 6: Create Registered Shifts - Shift Not Found
	 * Goal: Verify error handling when trying to register for a non-existent shift
	 * Input: Non-existent shift ID, authenticated doctor user
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(6)
	@DisplayName("TC-RSS-006: Create Registered Shifts - Shift Not Found")
	void testCreateRegisteredShiftsShiftNotFound() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
		testInputs.put("userId", TEST_USER_ID);

		List<RegisterShiftRequestDto> requestDtos = new ArrayList<>();
		RegisterShiftRequestDto requestDto = new RegisterShiftRequestDto();
		requestDto.setId("non-existent-shift");
		requestDtos.add(requestDto);
		testInputs.put("requestDtos", requestDtos);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			registeredShiftService.create(requestDtos);
		});
		testException = exception;

		assertEquals(400, exception.getStatus().value(), "Should return 400 Bad Request");
		assertTrue(exception.getMessage().contains("Shift not found"),
			"Error message should indicate that the shift was not found");
	}

	/**
	 * Test Case 7: Create Registered Shifts - Already Registered
	 * Goal: Verify error handling when trying to register for a shift that the doctor is already registered for
	 * Input: Shift ID that the doctor is already registered for, authenticated doctor user
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(7)
	@DisplayName("TC-RSS-007: Create Registered Shifts - Already Registered")
	void testCreateRegisteredShiftsAlreadyRegistered() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
		testInputs.put("userId", TEST_USER_ID);

		// Use the shift that the doctor is already registered for
		List<RegisterShiftRequestDto> requestDtos = new ArrayList<>();
		RegisterShiftRequestDto requestDto = new RegisterShiftRequestDto();
		requestDto.setId(TEST_SHIFT_ID);
		requestDtos.add(requestDto);
		testInputs.put("requestDtos", requestDtos);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			registeredShiftService.create(requestDtos);
		});
		testException = exception;

		assertEquals(400, exception.getStatus().value(), "Should return 400 Bad Request");
		assertTrue(exception.getMessage().contains("Doctor is already registered for this shift"),
			"Error message should indicate that the doctor is already registered for the shift");
	}

	/**
	 * Test Case 8: Approve Registered Shift - Success
	 * Goal: Verify successful approval of a registered shift
	 * Input: Valid registered shift ID, valid room information
	 * Expected Output: RegisteredShiftDto with approved status and assigned room
	 */
	@Test
	@Transactional
	@Order(8)
	@DisplayName("TC-RSS-008: Approve Registered Shift - Success")
	void testApproveRegisteredShiftSuccess() {
		// Given
		String registeredShiftId = TEST_REGISTERED_SHIFT_ID;
		testInputs.put("registeredShiftId", registeredShiftId);

		ApproveRegisteredShiftRequestDto requestDto = new ApproveRegisteredShiftRequestDto();
		RoomDto roomDto = new RoomDto();
		roomDto.setId(TEST_ROOM_ID);
		requestDto.setRoom(roomDto);
		testInputs.put("requestDto", requestDto);

		// When
		RegisteredShiftDto result = registeredShiftService.approvedRegisteredShift(registeredShiftId, requestDto);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertEquals(registeredShiftId, result.getId(), "Should return the correct registered shift");
		assertTrue(result.getIsApproved(), "Shift should be approved");
		assertNotNull(result.getRoom(), "Room should be assigned");
		assertEquals(TEST_ROOM_ID, result.getRoom().getId(), "Should be assigned to the correct room");
	}

	/**
	 * Test Case 9: Approve Registered Shift - Not Found
	 * Goal: Verify error handling when trying to approve a non-existent registered shift
	 * Input: Non-existent registered shift ID, valid room information
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(9)
	@DisplayName("TC-RSS-009: Approve Registered Shift - Not Found")
	void testApproveRegisteredShiftNotFound() {
		// Given
		String nonExistentId = "non-existent-id";
		testInputs.put("registeredShiftId", nonExistentId);

		ApproveRegisteredShiftRequestDto requestDto = new ApproveRegisteredShiftRequestDto();
		RoomDto roomDto = new RoomDto();
		roomDto.setId(TEST_ROOM_ID);
		requestDto.setRoom(roomDto);
		testInputs.put("requestDto", requestDto);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			registeredShiftService.approvedRegisteredShift(nonExistentId, requestDto);
		});
		testException = exception;

		assertEquals(404, exception.getStatus().value(), "Should return 404 Not Found");
		assertTrue(exception.getMessage().contains("Registered shift not found"),
			"Error message should indicate that the registered shift was not found");
	}

	/**
	 * Test Case 10: Approve Registered Shift - Already Approved
	 * Goal: Verify error handling when trying to approve an already approved registered shift
	 * Input: ID of an already approved registered shift, valid room information
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(10)
	@DisplayName("TC-RSS-010: Approve Registered Shift - Already Approved")
	void testApproveRegisteredShiftAlreadyApproved() {
		// Given
		// First, approve the shift
		ApproveRegisteredShiftRequestDto initialRequestDto = new ApproveRegisteredShiftRequestDto();
		RoomDto initialRoomDto = new RoomDto();
		initialRoomDto.setId(TEST_ROOM_ID);
		initialRequestDto.setRoom(initialRoomDto);
		registeredShiftService.approvedRegisteredShift(TEST_REGISTERED_SHIFT_ID, initialRequestDto);

		// Then try to approve it again
		String registeredShiftId = TEST_REGISTERED_SHIFT_ID;
		testInputs.put("registeredShiftId", registeredShiftId);

		ApproveRegisteredShiftRequestDto requestDto = new ApproveRegisteredShiftRequestDto();
		RoomDto roomDto = new RoomDto();
		roomDto.setId(TEST_ROOM_ID);
		requestDto.setRoom(roomDto);
		testInputs.put("requestDto", requestDto);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			registeredShiftService.approvedRegisteredShift(registeredShiftId, requestDto);
		});
		testException = exception;

		assertEquals(400, exception.getStatus().value(), "Should return 400 Bad Request");
		assertTrue(exception.getMessage().contains("Shift is already approved"),
			"Error message should indicate that the shift is already approved");
	}

	/**
	 * Test Case 11: Approve Registered Shift - Room Not Found
	 * Goal: Verify error handling when trying to approve a registered shift with a non-existent room
	 * Input: Valid registered shift ID, non-existent room ID
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(11)
	@DisplayName("TC-RSS-011: Approve Registered Shift - Room Not Found")
	void testApproveRegisteredShiftRoomNotFound() {
		// Given
		String registeredShiftId = TEST_REGISTERED_SHIFT_ID;
		testInputs.put("registeredShiftId", registeredShiftId);

		ApproveRegisteredShiftRequestDto requestDto = new ApproveRegisteredShiftRequestDto();
		RoomDto roomDto = new RoomDto();
		roomDto.setId("non-existent-room");
		requestDto.setRoom(roomDto);
		testInputs.put("requestDto", requestDto);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			registeredShiftService.approvedRegisteredShift(registeredShiftId, requestDto);
		});
		testException = exception;

		assertEquals(400, exception.getStatus().value(), "Should return 400 Bad Request");
		assertTrue(exception.getMessage().contains("Room not found"),
			"Error message should indicate that the room was not found");
	}

	/**
	 * Test Case 12: Approve Registered Shift - Room Not In Department
	 * Goal: Verify error handling when trying to approve a registered shift with a room from a different department
	 * Input: Valid registered shift ID, room from a different department
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(12)
	@DisplayName("TC-RSS-012: Approve Registered Shift - Room Not In Department")
	void testApproveRegisteredShiftRoomNotInDepartment() {
		// Given
		String registeredShiftId = TEST_REGISTERED_SHIFT_ID;
		testInputs.put("registeredShiftId", registeredShiftId);

		// Create a room in a different department
		Department otherDepartment = new Department();
		otherDepartment.setName("Other Department");
		otherDepartment.setDescription("Other Department Description");
		otherDepartment.setImageUrl("http://example.com/other-image.jpg");
		otherDepartment = entityManager.merge(otherDepartment);

		Room otherRoom = new Room();
		otherRoom.setName("Other Room");
		otherRoom.setDepartment(otherDepartment);
		otherRoom = entityManager.merge(otherRoom);

		entityManager.flush();

		ApproveRegisteredShiftRequestDto requestDto = new ApproveRegisteredShiftRequestDto();
		RoomDto roomDto = new RoomDto();
		roomDto.setId(otherRoom.getId());
		requestDto.setRoom(roomDto);
		testInputs.put("requestDto", requestDto);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			registeredShiftService.approvedRegisteredShift(registeredShiftId, requestDto);
		});
		testException = exception;

		assertEquals(400, exception.getStatus().value(), "Should return 400 Bad Request");
		assertTrue(exception.getMessage().contains("Room is not in the doctor's department"),
			"Error message should indicate that the room is not in the doctor's department");
	}

	/**
	 * Test Case 13: Create Registered Shifts - Duplicate Shifts
	 * Goal: Verify error handling when trying to register for duplicate shifts
	 * Input: List with duplicate shift IDs, authenticated doctor user
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(13)
	@DisplayName("TC-RSS-013: Create Registered Shifts - Duplicate Shifts")
	void testCreateRegisteredShiftsDuplicateShifts() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
		testInputs.put("userId", TEST_USER_ID);

		// Create a list with duplicate shift IDs
		List<RegisterShiftRequestDto> requestDtos = new ArrayList<>();

		// Create a new shift
		Shift shift = new Shift();
		shift.setStartTime(Instant.now().plus(2, ChronoUnit.DAYS));
		shift.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));
		shift = entityManager.merge(shift);
		entityManager.flush();

		// Add the same shift ID twice
		RegisterShiftRequestDto requestDto1 = new RegisterShiftRequestDto();
		requestDto1.setId(shift.getId());
		RegisterShiftRequestDto requestDto2 = new RegisterShiftRequestDto();
		requestDto2.setId(shift.getId());
		requestDtos.add(requestDto1);
		requestDtos.add(requestDto2);

		testInputs.put("requestDtos", requestDtos);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			registeredShiftService.create(requestDtos);
		});
		testException = exception;

		assertEquals(400, exception.getStatus().value(), "Should return 400 Bad Request");
		assertTrue(exception.getMessage().contains("Duplicate shifts"),
			"Error message should indicate duplicate shifts in request");
	}

	/**
	 * Test Case 14: Create Registered Shifts - Shift Full
	 * Goal: Verify error handling when trying to register for a shift that is already full
	 * Input: Valid shift ID, authenticated doctor user, but shift is full
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(14)
	@DisplayName("TC-RSS-014: Create Registered Shifts - Shift Full")
	void testCreateRegisteredShiftsShiftFull() {
		// Given
		when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
		testInputs.put("userId", TEST_USER_ID);

		// Create a new shift
		Shift shift = new Shift();
		shift.setStartTime(Instant.now().plus(3, ChronoUnit.DAYS));
		shift.setEndTime(Instant.now().plus(3, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));
		shift = entityManager.merge(shift);

		// Fill all rooms in the department with registered shifts for this shift
		List<Room> departmentRooms = roomRepository.findAllByDepartmentId(testDoctor.getDepartment().getId());

		// Create a different doctor in the same department
		User otherUser = new User();
		otherUser.setEmail("other-doctor@example.com");
		otherUser.setFullName("Other Doctor");
		otherUser.setAge(35);
		otherUser.setPhone("5559876543");
		otherUser.setGender(UserGender.Male);
		otherUser = entityManager.merge(otherUser);

		Doctor otherDoctor = new Doctor();
		otherDoctor.setUser(otherUser);
		otherDoctor.setDepartment(testDoctor.getDepartment());
		otherDoctor.setYearsOfExperience(8);
		otherDoctor.setDegree("PhD");
		otherDoctor.setNumberOfPatients(75);
		otherDoctor.setNumberOfCertificates(5);
		otherDoctor.setDescription("Other doctor description");
		otherDoctor = entityManager.merge(otherDoctor);

		// Register shifts for all rooms in the department
		for (Room room : departmentRooms) {
			RegisteredShift registeredShift = new RegisteredShift();
			registeredShift.setDoctor(otherDoctor);
			registeredShift.setShift(shift);
			registeredShift.setRoom(room);
			registeredShift.setIsApproved(true);
			registeredShift.setMaxNumberOfPatients(16);
			registeredShift.setShiftPrice(100.0);
			registeredShift.setStartTime(shift.getStartTime());
			registeredShift.setEndTime(shift.getEndTime());
			entityManager.persist(registeredShift);
		}

		entityManager.flush();

		// Create request DTO
		List<RegisterShiftRequestDto> requestDtos = new ArrayList<>();
		RegisterShiftRequestDto requestDto = new RegisterShiftRequestDto();
		requestDto.setId(shift.getId());
		requestDtos.add(requestDto);
		testInputs.put("requestDtos", requestDtos);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			registeredShiftService.create(requestDtos);
		});
		testException = exception;

		assertEquals(400, exception.getStatus().value(), "Should return 400 Bad Request");
		assertTrue(exception.getMessage().contains("Shift is full"),
			"Error message should indicate that the shift is full");
	}

	/**
	 * Test Case 15: Approve Registered Shift - Room Already Assigned
	 * Goal: Verify error handling when trying to approve a registered shift with a room that is already assigned
	 * Input: Valid registered shift ID, room already assigned to another shift
	 * Expected Output: HttpException with appropriate error message
	 */
	@Test
	@Transactional
	@Order(15)
	@DisplayName("TC-RSS-015: Approve Registered Shift - Room Already Assigned")
	void testApproveRegisteredShiftRoomAlreadyAssigned() {
		// Given
		String registeredShiftId = TEST_REGISTERED_SHIFT_ID;
		testInputs.put("registeredShiftId", registeredShiftId);

		// Create another registered shift for the same shift but with a different doctor
		User otherUser = new User();
		otherUser.setEmail("another-doctor@example.com");
		otherUser.setFullName("Another Doctor");
		otherUser.setAge(40);
		otherUser.setPhone("5551112222");
		otherUser.setGender(UserGender.Male);
		otherUser = entityManager.merge(otherUser);

		Doctor otherDoctor = new Doctor();
		otherDoctor.setUser(otherUser);
		otherDoctor.setDepartment(testDoctor.getDepartment());
		otherDoctor.setYearsOfExperience(10);
		otherDoctor.setDegree("MD");
		otherDoctor.setNumberOfPatients(100);
		otherDoctor.setNumberOfCertificates(7);
		otherDoctor.setDescription("Another doctor description");
		otherDoctor = entityManager.merge(otherDoctor);

		RegisteredShift otherRegisteredShift = new RegisteredShift();
		otherRegisteredShift.setDoctor(otherDoctor);
		otherRegisteredShift.setShift(testShift);
		otherRegisteredShift.setRoom(testRoom);
		otherRegisteredShift.setIsApproved(true);
		otherRegisteredShift.setMaxNumberOfPatients(16);
		otherRegisteredShift.setShiftPrice(100.0);
		otherRegisteredShift.setStartTime(testShift.getStartTime());
		otherRegisteredShift.setEndTime(testShift.getEndTime());
		entityManager.persist(otherRegisteredShift);

		entityManager.flush();

		ApproveRegisteredShiftRequestDto requestDto = new ApproveRegisteredShiftRequestDto();
		RoomDto roomDto = new RoomDto();
		roomDto.setId(testRoom.getId());
		requestDto.setRoom(roomDto);
		testInputs.put("requestDto", requestDto);

		// When & Then
		HttpException exception = assertThrows(HttpException.class, () -> {
			registeredShiftService.approvedRegisteredShift(registeredShiftId, requestDto);
		});
		testException = exception;

		assertEquals(400, exception.getStatus().value(), "Should return 400 Bad Request");
		assertTrue(exception.getMessage().contains("Room already assigned"),
			"Error message should indicate that the room is already assigned");
	}

	/**
	 * Test Case 16: Find All By Doctor For Booking - No Shifts
	 * Goal: Verify behavior when doctor has no shifts available for booking
	 * Input: Valid doctor ID, but no shifts available for booking
	 * Expected Output: Empty list
	 */
	@Test
	@Transactional
	@Order(16)
	@DisplayName("TC-RSS-016: Find All By Doctor For Booking - No Shifts")
	void testFindAllByDoctorForBookingNoShifts() {
		// Given
		String doctorId = TEST_DOCTOR_ID;
		testInputs.put("doctorId", doctorId);

		// Remove all registered shifts for the doctor
		List<RegisteredShift> existingRegisteredShifts = registeredShiftRepository.findAllByDoctorId(doctorId);
		for (RegisteredShift registeredShift : existingRegisteredShifts) {
			entityManager.remove(registeredShift);
		}
		entityManager.flush();

		// When
		List<SearchShiftForBookingResultDto> result = registeredShiftService.findAllByDoctorForBooking(doctorId);
		testOutputs.put("result", result);

		// Then
		assertNotNull(result, "Result should not be null");
		assertTrue(result.isEmpty(), "Should return an empty list when no shifts are available for booking");
	}
}
