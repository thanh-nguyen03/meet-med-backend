package com.thanhnd.clinic_application.modules.shifts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.Permissions;
import com.thanhnd.clinic_application.modules.shifts.dto.ShiftDto;
import com.thanhnd.clinic_application.modules.shifts.dto.request.GenerateShiftTableRequestDto;
import com.thanhnd.clinic_application.modules.shifts.dto.request.GetShiftListRequestDto;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminShiftController.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Admin Shift Controller Tests")
@ExtendWith(MockitoExtension.class)
public class AdminShiftControllerTest {
	private static final Logger logger = Logger.getLogger(AdminShiftControllerTest.class.getName());
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
	 * Test Case ID: TC-ASC-001
	 * Description: Verify that shift list can be successfully retrieved when the user has read permission
	 * Input:
	 * - Valid date range (startDate, endDate)
	 * - User has Shift.READ permission
	 * Expected Output:
	 * - Status: 200 OK
	 * - Response contains list of shifts
	 */
	@Test
	@Order(1)
	@DisplayName("TC-ASC-001: Get Shift List - Success")
	void getShiftList_Success() throws Exception {
		// Arrange
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusDays(7);

		GetShiftListRequestDto requestDto = new GetShiftListRequestDto();
		requestDto.setStartDate(startDate);
		requestDto.setEndDate(endDate);
		testInputs.put("requestDto", requestDto);

		// Set up permissions
		setPermissions(List.of(Permissions.Shift.READ));

		// Mock service response
		List<ShiftDto> shiftDtos = new ArrayList<>();
		ShiftDto shiftDto = new ShiftDto();
		shiftDto.setId("shift-1");
		shiftDto.setStartTime(Instant.now());
		shiftDto.setEndTime(Instant.now().plusSeconds(14400)); // 4 hours later
		shiftDtos.add(shiftDto);
		testInputs.put("shiftDtos", shiftDtos);

		when(shiftService.getShiftList(eq(startDate), eq(endDate))).thenReturn(shiftDtos);

		// Act & Assert
		MockHttpServletRequestBuilder request = get("/api/admin/shifts")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto));

		performRequest(mockMvc.perform(request))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].id").value("shift-1"));
	}

	/**
	 * Test Case ID: TC-ASC-002
	 * Description: Verify that shift list request fails when user lacks required permission
	 * Input:
	 * - Valid date range (startDate, endDate)
	 * - User does not have Shift.READ permission
	 * Expected Output:
	 * - Status: 403 Forbidden
	 */
	@Test
	@Order(2)
	@DisplayName("TC-ASC-002: Get Shift List - Permission Denied")
	void getShiftList_PermissionDenied() throws Exception {
		// Arrange
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusDays(7);

		GetShiftListRequestDto requestDto = new GetShiftListRequestDto();
		requestDto.setStartDate(startDate);
		requestDto.setEndDate(endDate);
		testInputs.put("requestDto", requestDto);

		// Set up permissions (without Shift.READ)
		setPermissions(List.of("some:other:permission"));

		// Act & Assert
		MockHttpServletRequestBuilder request = get("/api/admin/shifts")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto));

		performRequest(mockMvc.perform(request))
			.andExpect(status().isForbidden());
	}

	/**
	 * Test Case ID: TC-ASC-003
	 * Description: Verify that shift list request fails with invalid date range
	 * Input:
	 * - Invalid date range (null startDate)
	 * - User has Shift.READ permission
	 * Expected Output:
	 * - Status: 400 Bad Request
	 */
	@Test
	@Order(3)
	@DisplayName("TC-ASC-003: Get Shift List - Invalid Date Range")
	void getShiftList_InvalidDateRange() throws Exception {
		// Arrange
		GetShiftListRequestDto requestDto = new GetShiftListRequestDto();
		requestDto.setStartDate(null); // Invalid - null startDate
		requestDto.setEndDate(LocalDate.now().plusDays(7));
		testInputs.put("requestDto", requestDto);

		// Set up permissions
		setPermissions(List.of(Permissions.Shift.READ));

		// Act & Assert
		MockHttpServletRequestBuilder request = get("/api/admin/shifts")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto));

		performRequest(mockMvc.perform(request))
			.andExpect(status().isBadRequest());
	}

	/**
	 * Test Case ID: TC-ASC-004
	 * Description: Verify that shift table can be successfully generated when user has write permission
	 * Input:
	 * - Valid month and year
	 * - User has Shift.WRITE permission
	 * Expected Output:
	 * - Status: 200 OK
	 * - Success response
	 */
	@Test
	@Order(4)
	@DisplayName("TC-ASC-004: Generate Shift Table - Success")
	void generateShiftTable_Success() throws Exception {
		// Arrange
		GenerateShiftTableRequestDto requestDto = new GenerateShiftTableRequestDto();
		requestDto.setMonth(5); // May
		requestDto.setYear(2023);
		testInputs.put("requestDto", requestDto);

		// Set up permissions
		setPermissions(List.of(Permissions.Shift.WRITE));

		// Mock service
		doNothing().when(shiftService).createShiftTable(eq(5), eq(2023));

		// Act & Assert
		MockHttpServletRequestBuilder request = post("/api/admin/shifts/generate")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto));

		performRequest(mockMvc.perform(request))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));
	}

	/**
	 * Test Case ID: TC-ASC-005
	 * Description: Verify that shift table generation fails when user lacks required permission
	 * Input:
	 * - Valid month and year
	 * - User does not have Shift.WRITE permission
	 * Expected Output:
	 * - Status: 403 Forbidden
	 */
	@Test
	@Order(5)
	@DisplayName("TC-ASC-005: Generate Shift Table - Permission Denied")
	void generateShiftTable_PermissionDenied() throws Exception {
		// Arrange
		GenerateShiftTableRequestDto requestDto = new GenerateShiftTableRequestDto();
		requestDto.setMonth(5);
		requestDto.setYear(2023);
		testInputs.put("requestDto", requestDto);

		// Set up permissions (without Shift.WRITE)
		setPermissions(List.of("some:other:permission"));

		// Act & Assert
		MockHttpServletRequestBuilder request = post("/api/admin/shifts/generate")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto));

		performRequest(mockMvc.perform(request))
			.andExpect(status().isForbidden());
	}

	/**
	 * Test Case ID: TC-ASC-006
	 * Description: Verify that shift table generation fails with invalid month
	 * Input:
	 * - Invalid month (13)
	 * - Valid year
	 * - User has Shift.WRITE permission
	 * Expected Output:
	 * - Status: 400 Bad Request
	 */
	@Test
	@Order(6)
	@DisplayName("TC-ASC-006: Generate Shift Table - Invalid Month")
	void generateShiftTable_InvalidMonth() throws Exception {
		// Arrange
		GenerateShiftTableRequestDto requestDto = new GenerateShiftTableRequestDto();
		requestDto.setMonth(13); // Invalid month
		requestDto.setYear(2023);
		testInputs.put("requestDto", requestDto);

		// Set up permissions
		setPermissions(List.of(Permissions.Shift.WRITE));

		// Act & Assert
		MockHttpServletRequestBuilder request = post("/api/admin/shifts/generate")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto));

		performRequest(mockMvc.perform(request))
			.andExpect(status().isBadRequest());
	}
}
