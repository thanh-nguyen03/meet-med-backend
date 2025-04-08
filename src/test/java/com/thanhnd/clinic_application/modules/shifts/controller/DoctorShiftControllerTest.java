package com.thanhnd.clinic_application.modules.shifts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.constants.Permissions;
import com.thanhnd.clinic_application.modules.rooms.dto.RoomDto;
import com.thanhnd.clinic_application.modules.shifts.dto.CanRegisterShiftDto;
import com.thanhnd.clinic_application.modules.shifts.dto.RegisteredShiftDto;
import com.thanhnd.clinic_application.modules.shifts.dto.request.ApproveRegisteredShiftRequestDto;
import com.thanhnd.clinic_application.modules.shifts.dto.request.RegisterShiftRequestDto;
import com.thanhnd.clinic_application.modules.shifts.service.RegisteredShiftService;
import com.thanhnd.clinic_application.modules.shifts.service.ShiftService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorShiftController.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Doctor Shift Controller Tests")
@ExtendWith(MockitoExtension.class)
public class DoctorShiftControllerTest {
	private static final Logger logger = Logger.getLogger(DoctorShiftControllerTest.class.getName());
	private TestInfo testInfo;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ShiftService shiftService;

	@MockBean
	private RegisteredShiftService registeredShiftService;

	@MockBean
	private JwtAuthenticationManager jwtAuthenticationManager;

	private final Map<String, Object> testInputs = new HashMap<>();
	private final Map<String, Object> testOutputs = new HashMap<>();

	@BeforeEach
	void setUp(TestInfo testInfo) {
		this.testInfo = testInfo;
		logger.info("Starting test: " + testInfo.getDisplayName());

		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

		// Configure ObjectMapper for better test output
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.findAndRegisterModules(); // For LocalDate support
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
		} catch (Exception e) {
			logger.warning("Error logging test data: " + e.getMessage());
		}

		// Clear maps for next test
		testInputs.clear();
		testOutputs.clear();
	}

	private ResultActions performRequest(ResultActions resultActions) throws Exception {
		return resultActions.andDo(print());
	}

	private void setPermissions(List<String> permissions) {
		Jwt jwt = createJwtToken("admin-user", permissions);
		List<SimpleGrantedAuthority> authorities = permissions.stream()
			.map(SimpleGrantedAuthority::new)
			.toList();
		JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

		// Mock the SecurityContext to set the authentication token
		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(token);
		SecurityContextHolder.setContext(securityContext);
	}

	private Jwt createJwtToken(String userId, List<String> permissions) {
		Map<String, Object> headers = Map.of("alg", "HS256", "typ", "JWT");
		Map<String, Object> claims = new HashMap<>();
		claims.put("permissions", permissions);
		claims.put("user_info", Map.of("id", userId));
		claims.put("sub", "auth0|" + userId);

		return new Jwt(
			"test-token-" + userId,
			Instant.now(),
			Instant.now().plusSeconds(3600),
			headers,
			claims
		);
	}

	/**
	 * Test Case ID: TC-DSC-001
	 * Description: Verify that shifts available for registration can be successfully retrieved
	 * Input:
	 * - User has RegisteredShift.READ permission
	 * Expected Output:
	 * - Status: 200 OK
	 * - Response contains list of shifts available for registration
	 */
	@Test
	@Order(1)
	@DisplayName("TC-DSC-001: Get Can Register Shifts - Success")
	void getCanRegisterShifts_Success() throws Exception {
		// Arrange
		setPermissions(List.of(Permissions.RegisteredShift.READ));

		// Mock service response
		List<CanRegisterShiftDto> shiftDtos = new ArrayList<>();
		CanRegisterShiftDto shiftDto = new CanRegisterShiftDto(null);
		shiftDto.setId("shift-1");
		shiftDto.setStartTime(Instant.now());
		shiftDto.setEndTime(Instant.now().plusSeconds(14400)); // 4 hours later
		shiftDto.setRemainingNumberOfRoomsAvailable(3);
		shiftDtos.add(shiftDto);
		testInputs.put("shiftDtos", shiftDtos);

		when(shiftService.getListShiftCanRegister()).thenReturn(shiftDtos);

		// Act & Assert
		MockHttpServletRequestBuilder request = get("/api/doctor/shifts/can-register")
			.contentType(MediaType.APPLICATION_JSON);

		performRequest(mockMvc.perform(request))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].id").value("shift-1"))
			.andExpect(jsonPath("$.data[0].remainingNumberOfRoomsAvailable").value(3));
	}

	/**
	 * Test Case ID: TC-DSC-002
	 * Description: Verify that shifts available for registration fails when user lacks required permission
	 * Input:
	 * - User does not have RegisteredShift.READ permission
	 * Expected Output:
	 * - Status: 403 Forbidden
	 */
	@Test
	@Order(2)
	@DisplayName("TC-DSC-002: Get Can Register Shifts - Permission Denied")
	void getCanRegisterShifts_PermissionDenied() throws Exception {
		// Arrange
		setPermissions(List.of("some:other:permission"));

		// Act & Assert
		MockHttpServletRequestBuilder request = get("/api/doctor/shifts/can-register")
			.contentType(MediaType.APPLICATION_JSON);

		performRequest(mockMvc.perform(request))
			.andExpect(status().isForbidden());
	}

	/**
	 * Test Case ID: TC-DSC-003
	 * Description: Verify that current week shifts can be successfully retrieved
	 * Input:
	 * - User has RegisteredShift.READ permission
	 * - isNextWeek parameter set to false
	 * Expected Output:
	 * - Status: 200 OK
	 * - Response contains list of current week shifts
	 */
	@Test
	@Order(3)
	@DisplayName("TC-DSC-003: Get Current Week Shifts - Success")
	void getCurrentWeekShifts_Success() throws Exception {
		// Arrange
		setPermissions(List.of(Permissions.RegisteredShift.READ));

		// Mock service response
		List<CanRegisterShiftDto> shiftDtos = new ArrayList<>();
		CanRegisterShiftDto shiftDto = new CanRegisterShiftDto(null);
		shiftDto.setId("shift-1");
		shiftDto.setStartTime(Instant.now());
		shiftDto.setEndTime(Instant.now().plusSeconds(14400)); // 4 hours later
		shiftDto.setRemainingNumberOfRoomsAvailable(3);
		shiftDto.setIsApproved(true);
		shiftDtos.add(shiftDto);
		testInputs.put("shiftDtos", shiftDtos);

		when(shiftService.getWeekShiftCanRegister(false)).thenReturn(shiftDtos);

		// Act & Assert
		MockHttpServletRequestBuilder request = get("/api/doctor/shifts/get-week")
			.param("isNextWeek", "false")
			.contentType(MediaType.APPLICATION_JSON);

		performRequest(mockMvc.perform(request))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].id").value("shift-1"))
			.andExpect(jsonPath("$.data[0].isApproved").value(true));
	}

	/**
	 * Test Case ID: TC-DSC-004
	 * Description: Verify that next week shifts can be successfully retrieved
	 * Input:
	 * - User has RegisteredShift.READ permission
	 * - isNextWeek parameter set to true
	 * Expected Output:
	 * - Status: 200 OK
	 * - Response contains list of next week shifts
	 */
	@Test
	@Order(4)
	@DisplayName("TC-DSC-004: Get Next Week Shifts - Success")
	void getNextWeekShifts_Success() throws Exception {
		// Arrange
		setPermissions(List.of(Permissions.RegisteredShift.READ));

		// Mock service response
		List<CanRegisterShiftDto> shiftDtos = new ArrayList<>();
		CanRegisterShiftDto shiftDto = new CanRegisterShiftDto(null);
		shiftDto.setId("shift-next-week");
		shiftDto.setStartTime(Instant.now().plusSeconds(604800)); // 1 week later
		shiftDto.setEndTime(Instant.now().plusSeconds(619200)); // 1 week + 4 hours later
		shiftDto.setRemainingNumberOfRoomsAvailable(2);
		shiftDto.setIsApproved(false);
		shiftDtos.add(shiftDto);
		testInputs.put("shiftDtos", shiftDtos);

		when(shiftService.getWeekShiftCanRegister(true)).thenReturn(shiftDtos);

		// Act & Assert
		MockHttpServletRequestBuilder request = get("/api/doctor/shifts/get-week")
			.param("isNextWeek", "true")
			.contentType(MediaType.APPLICATION_JSON);

		performRequest(mockMvc.perform(request))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].id").value("shift-next-week"))
			.andExpect(jsonPath("$.data[0].isApproved").value(false));
	}

	/**
	 * Test Case ID: TC-DSC-005
	 * Description: Verify that shifts can be successfully registered
	 * Input:
	 * - User has RegisteredShift.WRITE permission
	 * - Valid list of shift IDs to register
	 * Expected Output:
	 * - Status: 200 OK
	 * - Response contains list of registered shifts
	 */
	@Test
	@Order(5)
	@DisplayName("TC-DSC-005: Register Shifts - Success")
	void registerShifts_Success() throws Exception {
		// Arrange
		setPermissions(List.of(Permissions.RegisteredShift.WRITE));

		// Create request DTOs
		List<RegisterShiftRequestDto> requestDtos = new ArrayList<>();
		RegisterShiftRequestDto requestDto1 = new RegisterShiftRequestDto();
		requestDto1.setId("shift-1");
		RegisterShiftRequestDto requestDto2 = new RegisterShiftRequestDto();
		requestDto2.setId("shift-2");
		requestDtos.add(requestDto1);
		requestDtos.add(requestDto2);
		testInputs.put("requestDtos", requestDtos);

		// Mock service response
		List<RegisteredShiftDto> registeredShiftDtos = new ArrayList<>();
		RegisteredShiftDto registeredShiftDto1 = new RegisteredShiftDto();
		registeredShiftDto1.setId("registered-shift-1");
		registeredShiftDto1.setIsApproved(false);
		RegisteredShiftDto registeredShiftDto2 = new RegisteredShiftDto();
		registeredShiftDto2.setId("registered-shift-2");
		registeredShiftDto2.setIsApproved(false);
		registeredShiftDtos.add(registeredShiftDto1);
		registeredShiftDtos.add(registeredShiftDto2);
		testInputs.put("registeredShiftDtos", registeredShiftDtos);

		when(registeredShiftService.create(any())).thenReturn(registeredShiftDtos);

		// Act & Assert
		MockHttpServletRequestBuilder request = post("/api/doctor/shifts/register")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDtos));

		performRequest(mockMvc.perform(request))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].id").value("registered-shift-1"))
			.andExpect(jsonPath("$.data[1].id").value("registered-shift-2"));
	}

	/**
	 * Test Case ID: TC-DSC-006
	 * Description: Verify that shift registration fails when user lacks required permission
	 * Input:
	 * - User does not have RegisteredShift.WRITE permission
	 * - Valid list of shift IDs to register
	 * Expected Output:
	 * - Status: 403 Forbidden
	 */
	@Test
	@Order(6)
	@DisplayName("TC-DSC-006: Register Shifts - Permission Denied")
	void registerShifts_PermissionDenied() throws Exception {
		// Arrange
		setPermissions(List.of("some:other:permission"));

		// Create request DTOs
		List<RegisterShiftRequestDto> requestDtos = new ArrayList<>();
		RegisterShiftRequestDto requestDto = new RegisterShiftRequestDto();
		requestDto.setId("shift-1");
		requestDtos.add(requestDto);
		testInputs.put("requestDtos", requestDtos);

		// Act & Assert
		MockHttpServletRequestBuilder request = post("/api/doctor/shifts/register")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDtos));

		performRequest(mockMvc.perform(request))
			.andExpect(status().isForbidden());
	}

	/**
	 * Test Case ID: TC-DSC-007
	 * Description: Verify that shift registration fails with invalid shift ID
	 * Input:
	 * - User has RegisteredShift.WRITE permission
	 * - Invalid shift ID (empty)
	 * Expected Output:
	 * - Status: 400 Bad Request
	 */
	@Test
	@Order(7)
	@DisplayName("TC-DSC-007: Register Shifts - Invalid Shift ID")
	void registerShifts_InvalidShiftId() throws Exception {
		// Arrange
		setPermissions(List.of(Permissions.RegisteredShift.WRITE));

		// Create request DTOs with invalid ID
		List<RegisterShiftRequestDto> requestDtos = new ArrayList<>();
		RegisterShiftRequestDto requestDto = new RegisterShiftRequestDto();
		requestDto.setId(""); // Invalid - empty ID
		requestDtos.add(requestDto);
		testInputs.put("requestDtos", requestDtos);

		// Act & Assert
		MockHttpServletRequestBuilder request = post("/api/doctor/shifts/register")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDtos));

		performRequest(mockMvc.perform(request))
			.andExpect(status().isBadRequest());
	}

	/**
	 * Test Case ID: TC-DSC-008
	 * Description: Verify that registered shift can be successfully approved
	 * Input:
	 * - User has RegisteredShift.APPROVE permission
	 * - Valid registered shift ID
	 * - Valid room information
	 * Expected Output:
	 * - Status: 200 OK
	 * - Response contains approved registered shift
	 */
	@Test
	@Order(8)
	@DisplayName("TC-DSC-008: Approve Registered Shift - Success")
	void approveRegisteredShift_Success() throws Exception {
		// Arrange
		String registeredShiftId = "registered-shift-1";
		testInputs.put("registeredShiftId", registeredShiftId);

		setPermissions(List.of(Permissions.RegisteredShift.APPROVE));

		// Create request DTO
		ApproveRegisteredShiftRequestDto requestDto = new ApproveRegisteredShiftRequestDto();
		RoomDto roomDto = new RoomDto();
		roomDto.setId("room-1");
		roomDto.setName("Room 1");
		requestDto.setRoom(roomDto);
		testInputs.put("requestDto", requestDto);

		// Mock service response
		RegisteredShiftDto approvedShiftDto = new RegisteredShiftDto();
		approvedShiftDto.setId(registeredShiftId);
		approvedShiftDto.setIsApproved(true);
		approvedShiftDto.setRoom(roomDto);
		testInputs.put("approvedShiftDto", approvedShiftDto);

		when(registeredShiftService.approvedRegisteredShift(eq(registeredShiftId), any(ApproveRegisteredShiftRequestDto.class)))
			.thenReturn(approvedShiftDto);

		// Act & Assert
		MockHttpServletRequestBuilder request = put("/api/doctor/shifts/{registeredShiftId}/approve", registeredShiftId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto));

		performRequest(mockMvc.perform(request))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.id").value(registeredShiftId))
			.andExpect(jsonPath("$.data.isApproved").value(true));
	}

	/**
	 * Test Case ID: TC-DSC-009
	 * Description: Verify that registered shift approval fails when user lacks required permission
	 * Input:
	 * - User does not have RegisteredShift.APPROVE permission
	 * - Valid registered shift ID
	 * - Valid room information
	 * Expected Output:
	 * - Status: 403 Forbidden
	 */
	@Test
	@Order(9)
	@DisplayName("TC-DSC-009: Approve Registered Shift - Permission Denied")
	void approveRegisteredShift_PermissionDenied() throws Exception {
		// Arrange
		String registeredShiftId = "registered-shift-1";
		testInputs.put("registeredShiftId", registeredShiftId);

		setPermissions(List.of("some:other:permission"));

		// Create request DTO
		ApproveRegisteredShiftRequestDto requestDto = new ApproveRegisteredShiftRequestDto();
		RoomDto roomDto = new RoomDto();
		roomDto.setId("room-1");
		requestDto.setRoom(roomDto);
		testInputs.put("requestDto", requestDto);

		// Act & Assert
		MockHttpServletRequestBuilder request = put("/api/doctor/shifts/{registeredShiftId}/approve", registeredShiftId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto));

		performRequest(mockMvc.perform(request))
			.andExpect(status().isForbidden());
	}

	/**
	 * Test Case ID: TC-DSC-010
	 * Description: Verify that registered shift approval fails with invalid room
	 * Input:
	 * - User has RegisteredShift.APPROVE permission
	 * - Valid registered shift ID
	 * - Invalid room information (null)
	 * Expected Output:
	 * - Status: 400 Bad Request
	 */
	@Test
	@Order(10)
	@DisplayName("TC-DSC-010: Approve Registered Shift - Invalid Room")
	void approveRegisteredShift_InvalidRoom() throws Exception {
		// Arrange
		String registeredShiftId = "registered-shift-1";
		testInputs.put("registeredShiftId", registeredShiftId);

		setPermissions(List.of(Permissions.RegisteredShift.APPROVE));

		// Create request DTO with null room
		ApproveRegisteredShiftRequestDto requestDto = new ApproveRegisteredShiftRequestDto();
		requestDto.setRoom(null); // Invalid - null room
		testInputs.put("requestDto", requestDto);

		// Act & Assert
		MockHttpServletRequestBuilder request = put("/api/doctor/shifts/{registeredShiftId}/approve", registeredShiftId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto));

		performRequest(mockMvc.perform(request))
			.andExpect(status().isBadRequest());
	}

	/**
	 * Test Case ID: TC-DSC-011
	 * Description: Verify that registered shift approval fails with non-existent shift ID
	 * Input:
	 * - User has RegisteredShift.APPROVE permission
	 * - Non-existent registered shift ID
	 * - Valid room information
	 * Expected Output:
	 * - Status: 404 Not Found
	 */
	@Test
	@Order(11)
	@DisplayName("TC-DSC-011: Approve Registered Shift - Shift Not Found")
	void approveRegisteredShift_ShiftNotFound() throws Exception {
		// Arrange
		String nonExistentShiftId = "non-existent-shift";
		testInputs.put("registeredShiftId", nonExistentShiftId);

		setPermissions(List.of(Permissions.RegisteredShift.APPROVE));

		// Create request DTO
		ApproveRegisteredShiftRequestDto requestDto = new ApproveRegisteredShiftRequestDto();
		RoomDto roomDto = new RoomDto();
		roomDto.setId("room-1");
		requestDto.setRoom(roomDto);
		testInputs.put("requestDto", requestDto);

		// Mock service to throw exception
		when(registeredShiftService.approvedRegisteredShift(eq(nonExistentShiftId), any(ApproveRegisteredShiftRequestDto.class)))
			.thenThrow(HttpException.notFound(Message.REGISTERED_SHIFT_NOT_FOUND.getMessage()));

		// Act & Assert
		MockHttpServletRequestBuilder request = put("/api/doctor/shifts/{registeredShiftId}/approve", nonExistentShiftId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto));

		performRequest(mockMvc.perform(request))
			.andExpect(status().isNotFound());
	}
}
