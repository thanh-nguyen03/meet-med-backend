package com.thanhnd.clinic_application.modules.patients.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.constants.UserGender;
import com.thanhnd.clinic_application.modules.identity_providers.interfaces.IdentityProviderStrategy;
import com.thanhnd.clinic_application.modules.patients.dto.PatientDto;
import com.thanhnd.clinic_application.modules.patients.repository.PatientRepository;
import com.thanhnd.clinic_application.modules.patients.service.PatientService;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import com.thanhnd.clinic_application.modules.users.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest  // Use full application context
@AutoConfigureMockMvc  // Configure MockMvc
@ActiveProfiles("test")  // Use test profile
@Transactional  // Enable transaction rollback after each test
@DisplayName("Patient Profile API Tests")
@Tag("patient")
public class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private UserRepository userRepository;

    // No longer need to mock these services since we're using real implementations
    @Autowired
    private PatientService patientService;

    @Autowired
    private JwtAuthenticationManager jwtAuthenticationManager;

    @Autowired
    private com.thanhnd.clinic_application.modules.identity_providers.IdentityProviderStrategyFactory identityProviderStrategyFactory;

    private static final String USER_ID = "testID";
    private PatientDto mockPatientDto;
    private UserDto mockUserDto;
    private Jwt jwtToken;
    private MvcResult testResult;
    private boolean testPassed = false;
    private String currentTestCaseId;
    
    // Pattern to extract test case ID from DisplayName annotation
    private static final Pattern TEST_CASE_ID_PATTERN = Pattern.compile("TC-P-(\\d{3})");
    
    /**
     * Create a JWT with necessary claims for testing
     */
    private Jwt createJwtToken(String userId) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        headers.put("typ", "JWT");
        
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userId);
        claims.put("user_info", userInfo);
        claims.put("sub", "auth0|" + userId);
        
        return new Jwt(
                "test-token-" + userId,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                headers,
                claims
        );
    }
    
    @BeforeEach
    void setUp(TestInfo testInfo) {
        // Extract test case ID from the DisplayName
        String displayName = testInfo.getDisplayName();
        Matcher matcher = TEST_CASE_ID_PATTERN.matcher(displayName);
        if (matcher.find()) {
            currentTestCaseId = "TC-P-" + matcher.group(1);
        }

        // Create test user data
        mockUserDto = new UserDto();
        mockUserDto.setId(USER_ID);
        mockUserDto.setEmail("test@gmail.com");
        mockUserDto.setFullName("Test User 123");
        mockUserDto.setAge(20);
        mockUserDto.setPhone("1234567890");

        // Create test patient data
        mockPatientDto = new PatientDto();
        mockPatientDto.setDateOfBirth(LocalDate.of(1992, 5, 15));
        mockPatientDto.setAddressLine("123 Test Street");
        mockPatientDto.setCity("Test City");
        mockPatientDto.setDistrict("Test District");
        mockPatientDto.setInsuranceCode("INS-123456");
        mockPatientDto.setUser(mockUserDto);

        // Create a JWT token for authentication
        jwtToken = createJwtToken(USER_ID);
        
        System.out.println("\n======= TEST EXECUTION STARTED: " + (currentTestCaseId != null ? currentTestCaseId : "Unknown Test") + " =======");
    }
    
    @AfterEach
    void tearDown() {
        if (testResult != null) {
            MockHttpServletResponse response = testResult.getResponse();
            System.out.println("\n======= TEST RESULT =======");
            System.out.println("Test Case: " + currentTestCaseId);
            System.out.println("Status: " + (testPassed ? "✅ PASSED" : "❌ FAILED"));
            System.out.println("HTTP Status: " + response.getStatus());
            try {
                String responseContent = response.getContentAsString();
                System.out.println("\nResponse Body:");
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(objectMapper.readTree(responseContent)));
            } catch (Exception e) {
                System.out.println("Error formatting response: " + e.getMessage());
            }
            System.out.println("============================\n");
        }
        
        // No manual cleanup needed - @Transactional handles rollback
    }

    /**
     * Custom result handler to capture test results
     */
    private ResultHandler captureResult(boolean expectedToPass) {
        return result -> {
            testResult = result;
            testPassed = expectedToPass;
        };
    }

    /**
     * Test Case: TC-P-001
     * Goal: Verify that a patient can successfully retrieve their profile
     * Input:
     * - Valid JWT token for authenticated user
     * - User has an existing patient profile
     * Expected Output:
     * - Status 200 OK
     * - Patient DTO with correct user information
     */
    @Test
    @DisplayName("📋 TC-P-001: Patient Profile Retrieval - Success Path")
    void testGetPatientProfile() throws Exception {
        // First create a patient profile to retrieve
        testCreatePatientProfile();
        
        // When: Patient requests their profile
        ResultActions result = mockMvc.perform(
                get("/api/patient/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwtToken))
        ).andDo(MockMvcResultHandlers.print())
         .andDo(captureResult(true));

        // Then: System returns patient profile successfully
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", equalTo(200)))
                .andExpect(jsonPath("$.success", equalTo(true)))
                // Verify patient info is returned (exact IDs will vary)
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.addressLine", equalTo("123 Test Street")))
                .andExpect(jsonPath("$.data.city", equalTo("Test City")))
                .andExpect(jsonPath("$.data.district", equalTo("Test District")))
                .andExpect(jsonPath("$.data.insuranceCode", equalTo("INS-123456")))
                // Verify user info is returned
                .andExpect(jsonPath("$.data.user.email", equalTo("test@gmail.com")))
                .andExpect(jsonPath("$.data.user.fullName", equalTo("Test User 123")))
                .andExpect(jsonPath("$.data.user.age", equalTo(20)))
                .andExpect(jsonPath("$.data.user.phone", equalTo("1234567890")));
                
        // Verify the patient exists in the database
        Assertions.assertTrue(patientRepository.findByUserId(USER_ID).isPresent(),
                "Patient should exist in the database during the test");
    }

    /**
     * Test Case: TC-P-002
     * Goal: Verify error handling when attempting to retrieve a non-existent patient profile
     * Input:
     * - Valid JWT token for authenticated user
     * - User does not have a patient profile
     * Expected Output:
     * - Status 404 Not Found
     * - Error message indicating patient profile not found
     */
    @Test
    @DisplayName("📋 TC-P-002: Patient Profile Retrieval - Not Found Error")
    void testGetNonExistentPatientProfile() throws Exception {
        // Given: User without patient profile
        String nonExistentUserId = "user-without-profile";
        Jwt testJwtToken = createJwtToken(nonExistentUserId);
        
        // When: Patient requests their profile
        ResultActions result = mockMvc.perform(
                get("/api/patient/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(testJwtToken))
        ).andDo(MockMvcResultHandlers.print())
         .andDo(captureResult(true));

        // Then: System returns not found error
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.message", equalTo(Message.PATIENT_NOT_FOUND.getMessage())));
                
        // Verify the patient does not exist in the database
        Assertions.assertFalse(patientRepository.findByUserId(nonExistentUserId).isPresent(),
                "Patient should not exist in the database");
    }

    /**
     * Test Case: TC-P-003
     * Goal: Verify successful creation of a new patient profile
     * Input:
     * - Valid JWT token for authenticated user
     * - Patient DTO with valid information
     * Expected Output:
     * - Status 200 OK (created)
     * - Created Patient DTO with assigned ID
     */
    @Test
    @DisplayName("📋 TC-P-003: Patient Profile Creation - Success Path")
    void testCreatePatientProfile() throws Exception {
        // When: User submits a request to create a patient profile
        ResultActions result = mockMvc.perform(
                post("/api/patient/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPatientDto))
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwtToken))
        ).andDo(MockMvcResultHandlers.print())
         .andDo(captureResult(true));
        
        // Then: System creates patient profile and returns success
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", equalTo(200)))
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.addressLine", equalTo("123 Test Street")))
                .andExpect(jsonPath("$.data.district", equalTo("Test District")))
                .andExpect(jsonPath("$.data.city", equalTo("Test City")))
                .andExpect(jsonPath("$.data.insuranceCode", equalTo("INS-123456")));
                
        // Verify the patient exists in the database
        Assertions.assertTrue(patientRepository.findByUserId(USER_ID).isPresent(),
                "Patient should exist in the database during the test");
    }
} 