package com.thanhnd.clinic_application.modules.patients.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.constants.UserGender;
import com.thanhnd.clinic_application.modules.identity_providers.IdentityProviderStrategyFactory;
import com.thanhnd.clinic_application.modules.identity_providers.interfaces.IdentityProviderStrategy;
import com.thanhnd.clinic_application.modules.patients.dto.PatientDto;
import com.thanhnd.clinic_application.modules.patients.service.PatientService;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Patient Controller Tests")
public class PatientControllerTest {

    private static final Logger logger = Logger.getLogger(PatientControllerTest.class.getName());
    private TestInfo testInfo;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private PatientService patientService;
    
    @MockBean
    private JwtAuthenticationManager jwtAuthenticationManager;
    
    @MockBean
    private IdentityProviderStrategyFactory identityProviderStrategyFactory;
    
    @MockBean
    private IdentityProviderStrategy identityProviderStrategy;
    
    private PatientDto mockPatientDto;
    private UserDto mockUserDto;
    
    // Maps to track inputs and outputs for each test
    private Map<String, Object> testInputs = new HashMap<>();
    private Map<String, Object> testOutputs = new HashMap<>();
    private Exception testException = null;
    
    // TestWatcher to log test results
    @RegisterExtension
    TestWatcher testWatcher = new TestWatcher() {
        @Override
        public void testSuccessful(ExtensionContext context) {
            printTestResults(context, true);
        }

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            testException = (cause instanceof Exception) ? (Exception) cause : new Exception(cause);
            printTestResults(context, false);
        }
        
        private void printTestResults(ExtensionContext context, boolean passed) {
            String testName = context.getDisplayName();
            
            System.out.println("\n======= TEST RESULT =======");
            System.out.println("Test: " + testName);
            System.out.println("Status: " + (passed ? "✅ PASSED" : "❌ FAILED"));
            
            try {
                System.out.println("\nTest Input:");
                for (Map.Entry<String, Object> entry : testInputs.entrySet()) {
                    System.out.println("- " + entry.getKey() + ": " + 
                        (entry.getValue() instanceof String 
                            ? entry.getValue() 
                            : objectMapper.writeValueAsString(entry.getValue())));
                }
                
                System.out.println("\nTest Output:");
                if (testOutputs.isEmpty() && testException != null) {
                    System.out.println("- Exception: " + testException.getMessage());
                    testException.printStackTrace(System.out);
                } else {
                    for (Map.Entry<String, Object> entry : testOutputs.entrySet()) {
                        System.out.println("- " + entry.getKey() + ": " + 
                            (entry.getValue() instanceof String 
                                ? entry.getValue() 
                                : objectMapper.writeValueAsString(entry.getValue())));
                    }
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
    };
    
    @BeforeEach
    void setUp(TestInfo testInfo) {
        this.testInfo = testInfo;
        logger.info("Starting test: " + testInfo.getDisplayName());
        
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Configure ObjectMapper for better test output
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.findAndRegisterModules(); // For LocalDate support
        
        // Setup mock user
        mockUserDto = new UserDto();
        mockUserDto.setId("uuid-patient-1");
        mockUserDto.setEmail("patient1@clinic.com");
        mockUserDto.setFullName("Alice Smith");
        mockUserDto.setAge(34);
        mockUserDto.setPhone("555-1111");
        mockUserDto.setGender(UserGender.Female);
        
        // Setup mock patient
        mockPatientDto = new PatientDto();
        mockPatientDto.setId("uuid-patient-1-profile");
        mockPatientDto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        mockPatientDto.setAddressLine("123 Health St");
        mockPatientDto.setDistrict("Wellness");
        mockPatientDto.setCity("MedCity");
        mockPatientDto.setInsuranceCode("INS12345");
        mockPatientDto.setUser(mockUserDto);
        
        // Setup identity provider strategy
        when(identityProviderStrategyFactory.getStrategy(anyString())).thenReturn(identityProviderStrategy);
        when(identityProviderStrategy.updateUser(anyString(), any(Map.class))).thenReturn(Map.of("success", true));
        
        // Track standard inputs for reporting
        testInputs.put("mockPatientDto", mockPatientDto);
    }

    @AfterEach
    void tearDown() {
        logger.info("Completed test: " + testInfo.getDisplayName());
    }

    // Helper method to perform request and track results
    private ResultActions performRequest(ResultActions resultActions) throws Exception {
        MvcResult result = resultActions.andReturn();
        
        // Record request details
        testInputs.put("requestMethod", result.getRequest().getMethod());
        testInputs.put("requestURI", result.getRequest().getRequestURI());
        
        // Safely log request content - GET requests don't have content
        try {
            if (result.getRequest().getContentLength() > 0) {
                testInputs.put("requestBody", result.getRequest().getContentAsString());
            }
        } catch (Exception e) {
            // Ignore content reading errors
        }
        
        // Record response details
        testOutputs.put("responseStatus", result.getResponse().getStatus());
        
        // Safely log response content
        try {
            if (result.getResponse().getContentLength() > 0) {
                testOutputs.put("responseBody", result.getResponse().getContentAsString());
            }
        } catch (Exception e) {
            // Ignore content reading errors
        }
        
        return resultActions;
    }
    
    /**
     * Testcase 1 `testGetProfile_Success`
     * - Goal: Verify a patient can successfully retrieve their own profile information when authenticated.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication for an existing patient user (userId: "uuid-patient-1").
     *   2. Mock patientService.findByUserId() to return a valid PatientDto for the user.
     *   3. Perform a GET request to "/api/patient/profile".
     *   4. Assert the response status is 200 OK.
     *   5. Assert the response body contains the correct PatientDto data.
     * - Input: Valid JWT token for user "uuid-patient-1".
     * - Expected Output: HTTP Status 200 OK with PatientDto matching the data for user "uuid-patient-1".
     * - Note: Assumes the user associated with the JWT has an existing patient profile.
     */
    @Test
    @Order(1)
    @DisplayName("TC-PC-001: Patient Profile Retrieval - Success")
    void testGetProfile_Success() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-1");
        testInputs.put("userId", "uuid-patient-1");
        
        // Mock patientService to return a valid PatientDto
        when(patientService.findByUserId("uuid-patient-1")).thenReturn(mockPatientDto);
        
        // Perform GET request
        performRequest(mockMvc.perform(get("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(mockPatientDto.getId()))
                .andExpect(jsonPath("$.data.addressLine").value(mockPatientDto.getAddressLine()))
                .andExpect(jsonPath("$.data.district").value(mockPatientDto.getDistrict()))
                .andExpect(jsonPath("$.data.city").value(mockPatientDto.getCity()))
                .andExpect(jsonPath("$.data.insuranceCode").value(mockPatientDto.getInsuranceCode()))
                .andExpect(jsonPath("$.data.user.id").value(mockPatientDto.getUser().getId()))
                .andExpect(jsonPath("$.data.user.fullName").value(mockPatientDto.getUser().getFullName()));
    }

    
    /**
     * Testcase 2 `testGetProfile_NotFound`
     * - Goal: Verify error handling when a patient tries to get a profile but doesn't have one created yet.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication for a user who exists but has no associated patient profile.
     *   2. Mock patientService.findByUserId() to throw an HttpException for the user ID.
     *   3. Perform a GET request to "/api/patient/profile".
     *   4. Assert the response status is 400 Bad Request.
     *   5. Assert the response body contains an appropriate error message.
     * - Input: Valid JWT token for user without a profile.
     * - Expected Output: HTTP Status 400 Bad Request with error message.
     * - Note: Tests error case for GET endpoint when profile doesn't exist.
     */
    @Test
    @Order(2)
    @DisplayName("TC-PC-002: Patient Profile Retrieval - Not Found")
    void testGetProfile_NotFound() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-user-no-profile");
        
        // Mock service to throw exception
        when(patientService.findByUserId("uuid-user-no-profile"))
                .thenThrow(HttpException.badRequest(Message.PATIENT_NOT_FOUND.getMessage()));
        
        // Perform GET request
        performRequest(mockMvc.perform(get("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Message.PATIENT_NOT_FOUND.getMessage()));
    }
    
    /**
     * Testcase 3 `testCreateProfile_Success`
     * - Goal: Verify a new patient profile can be successfully created for an existing user.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication for a user without a profile.
     *   2. Mock patientService.create() to successfully create and return a new PatientDto.
     *   3. Perform a POST request to "/api/patient/profile" with a valid PatientDto.
     *   4. Assert the response status is 200 OK.
     *   5. Assert the response body contains the created PatientDto with generated ID.
     *   6. Verify the service was called.
     * - Input: Valid JWT token and PatientDto with user details.
     * - Expected Output: HTTP Status 200 OK with the created PatientDto.
     * - Note: Tests happy path for POST endpoint.
     */
    @Test
    @Order(3)
    @DisplayName("TC-PC-003: Patient Profile Creation - Success")
    void testCreateProfile_Success() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-user-new");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-user-new");
        
        // Mock service to return created profile
        when(patientService.create(any(PatientDto.class))).thenReturn(mockPatientDto);
        
        // Perform POST request
        performRequest(mockMvc.perform(post("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPatientDto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(mockPatientDto.getId()));
        
        // Verify service was called
        verify(patientService).create(any(PatientDto.class));
    }
    
    /**
     * Testcase 4 `testCreateProfile_ProfileAlreadyExists`
     * - Goal: Verify creating a patient profile fails if the user already has one.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication for a user who already has a profile.
     *   2. Mock patientService.create() to throw an HttpException with "profile already exists" message.
     *   3. Perform a POST request to "/api/patient/profile" with a PatientDto.
     *   4. Assert the response status is 400 Bad Request.
     *   5. Assert the response body contains an error message.
     * - Input: Valid JWT token for a user with existing profile and PatientDto.
     * - Expected Output: HTTP Status 400 Bad Request with error message.
     * - Note: Tests business rule enforcement (one profile per user).
     */
    @Test
    @Order(4)
    @DisplayName("TC-PC-004: Patient Profile Creation - Profile Already Exists")
    void testCreateProfile_ProfileAlreadyExists() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-1");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-patient-1");
        
        // Mock service to throw exception
        when(patientService.create(any(PatientDto.class)))
                .thenThrow(HttpException.badRequest(Message.PATIENT_ALREADY_EXISTS.getMessage()));
        
        // Perform POST request
        performRequest(mockMvc.perform(post("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPatientDto))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Message.PATIENT_ALREADY_EXISTS.getMessage()));
    }
    
    /**
     * Testcase 5 `testCreateProfile_UserNotFound`
     * - Goal: Verify creating a patient profile fails if the associated user is not found.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication for a supposedly non-existent user.
     *   2. Mock patientService.create() to throw an HttpException with "user not found" message.
     *   3. Perform a POST request to "/api/patient/profile" with a PatientDto.
     *   4. Assert the response status is 400 Bad Request.
     *   5. Assert the response body contains an error message.
     * - Input: Valid JWT token (conceptually for a non-existent user) and PatientDto.
     * - Expected Output: HTTP Status 400 Bad Request with error message.
     * - Note: Tests error case where user data might be inconsistent.
     */
    @Test
    @Order(5)
    @DisplayName("TC-PC-005: Patient Profile Creation - User Not Found")
    void testCreateProfile_UserNotFound() throws Exception {
        // Mock JWT authentication for non-existent user
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-user-nonexistent");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-user-nonexistent");
        
        // Mock service to throw exception when user not found
        when(patientService.create(any(PatientDto.class)))
                .thenThrow(HttpException.badRequest(Message.USER_NOT_FOUND.getMessage()));
        
        // Perform POST request
        performRequest(mockMvc.perform(post("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPatientDto))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Message.USER_NOT_FOUND.getMessage()));
    }
    
    /**
     * Testcase 6 `testCreateProfile_ValidationErrors`
     * - Goal: Verify creating a profile fails with validation errors (e.g., missing required field).
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication.
     *   2. Prepare a PatientDto request body missing a required field (user is null).
     *   3. Perform a POST request to "/api/patient/profile".
     *   4. Assert the response status is 400 Bad Request.
     * - Input: Valid JWT token and invalid PatientDto (missing required field).
     * - Expected Output: HTTP Status 400 Bad Request.
     * - Note: Tests data validation for required fields.
     */
    @Test
    @Order(6)
    @DisplayName("TC-PC-006: Patient Profile Creation - Validation Errors")
    void testCreateProfile_ValidationErrors() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-user-new");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-user-new");
        
        // Create invalid PatientDto (missing required field - user)
        PatientDto invalidPatientDto = new PatientDto();
        invalidPatientDto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        invalidPatientDto.setAddressLine("123 Health St");
        invalidPatientDto.setUser(null); // Missing required field
        
        // Perform POST request with invalid DTO
        performRequest(mockMvc.perform(post("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPatientDto))))
                .andExpect(status().isBadRequest());
    }
    
    /**
     * Testcase 7 `testUpdateProfile_Success`
     * - Goal: Verify a patient can successfully update their existing profile.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication for an existing patient user.
     *   2. Update mock data with new values for address and district.
     *   3. Mock patientService.update() to return the updated PatientDto.
     *   4. Perform a PUT request to "/api/patient/profile" with the updated DTO.
     *   5. Assert the response status is 200 OK.
     *   6. Assert the response body contains the updated fields.
     *   7. Verify the service was called.
     * - Input: Valid JWT token and updated PatientDto.
     * - Expected Output: HTTP Status 200 OK with the updated PatientDto.
     * - Note: Tests happy path for PUT endpoint.
     */
    @Test
    @Order(7)
    @DisplayName("TC-PC-007: Patient Profile Update - Success")
    void testUpdateProfile_Success() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-1");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-patient-1");
        
        // Update mock profile data
        mockPatientDto.setAddressLine("123 New Health St");
        mockPatientDto.setDistrict("Updated Wellness");
        
        // Mock service to return updated profile
        when(patientService.update(any(PatientDto.class))).thenReturn(mockPatientDto);
        
        // Perform PUT request
        performRequest(mockMvc.perform(put("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPatientDto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.addressLine").value("123 New Health St"))
                .andExpect(jsonPath("$.data.district").value("Updated Wellness"));
        
        // Verify service was called
        verify(patientService).update(any(PatientDto.class));
    }
    
    /**
     * Testcase 8 `testUpdateProfile_NotFound`
     * - Goal: Verify updating a profile fails if the patient profile does not exist yet.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication for a user without a profile.
     *   2. Mock patientService.update() to throw an HttpException.
     *   3. Perform a PUT request to "/api/patient/profile".
     *   4. Assert the response status is 400 Bad Request.
     *   5. Assert the response body contains an error message.
     * - Input: Valid JWT token for user without a profile and PatientDto.
     * - Expected Output: HTTP Status 400 Bad Request with error message.
     * - Note: Tests error case for PUT endpoint when profile doesn't exist.
     */
    @Test
    @Order(8)
    @DisplayName("TC-PC-008: Patient Profile Update - Not Found")
    void testUpdateProfile_NotFound() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-user-no-profile");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-user-no-profile");
        
        // Mock service to throw exception
        when(patientService.update(any(PatientDto.class)))
                .thenThrow(HttpException.badRequest(Message.PATIENT_HAS_NOT_BEEN_CREATED.getMessage()));
        
        // Perform PUT request
        performRequest(mockMvc.perform(put("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPatientDto))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Message.PATIENT_HAS_NOT_BEEN_CREATED.getMessage()));
    }
    
    /**
     * Testcase 9 `testUpdateProfile_ValidationErrors`
     * - Goal: Verify updating a profile fails with validation errors (e.g., invalid date format).
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication for an existing patient user.
     *   2. Modify the serialized PatientDto to contain an invalid date format.
     *   3. Perform a PUT request to "/api/patient/profile".
     *   4. Assert the response status is 400 Bad Request.
     * - Input: Valid JWT token and PatientDto with invalid date format.
     * - Expected Output: HTTP Status 400 Bad Request.
     * - Note: Tests data validation for date formats during update.
     */
    @Test
    @Order(9)
    @DisplayName("TC-PC-009: Patient Profile Update - Validation Errors")
    void testUpdateProfile_ValidationErrors() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-1");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-patient-1");
        
        // Create invalid date format
        String invalidPatientJson = objectMapper.writeValueAsString(mockPatientDto)
                .replace("1990-05-15", "invalid-date");
        
        // Perform PUT request with invalid data
        performRequest(mockMvc.perform(put("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPatientJson)))
                .andExpect(status().isBadRequest());
    }

    
    /**
     * Testcase 10 `testCreateProfile_InvalidEmailFormat`
     * - Goal: Verify creating a profile fails if the user's email in the DTO is invalid.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication.
     *   2. Set an invalid email format in the UserDto.
     *   3. Perform a POST request to "/api/patient/profile".
     *   4. Assert the response status is 400 Bad Request.
     * - Input: Valid JWT token and PatientDto with invalid email format.
     * - Expected Output: HTTP Status 400 Bad Request.
     * - Note: Tests email validation during profile creation.
     */
    @Test
    @Order(10)
    @DisplayName("TC-PC-010: Patient Profile Creation - Invalid Email Format")
    void testCreateProfile_InvalidEmailFormat() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-user-new");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-user-new");
        
        // Create invalid email
        mockUserDto.setEmail("invalid-email");
        
        // Perform POST request with invalid email
        performRequest(mockMvc.perform(post("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPatientDto))))
                .andExpect(status().isBadRequest());
    }
    
    /**
     * Testcase 11 `testUpdateProfile_InvalidPhoneFormat`
     * - Goal: Verify updating a profile fails if the user's phone number in the DTO is invalid.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication for an existing patient.
     *   2. Set an invalid phone number format in the UserDto.
     *   3. Mock patientService.update() to throw an exception for invalid phone.
     *   4. Perform a PUT request to "/api/patient/profile".
     *   5. Assert the response status is 400 Bad Request.
     * - Input: Valid JWT token and PatientDto with invalid phone format.
     * - Expected Output: HTTP Status 400 Bad Request.
     * - Note: Tests phone number validation during profile update.
     */
    @Test
    @Order(11)
    @DisplayName("TC-PC-011: Patient Profile Update - Invalid Phone Format")
    void testUpdateProfile_InvalidPhoneFormat() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-1");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-patient-1");
        
        // Assuming there's validation for phone number format
        mockUserDto.setPhone("invalid-phone-number-format-abc");
        
        // Mock service to throw exception for invalid phone
        when(patientService.update(any(PatientDto.class)))
                .thenThrow(HttpException.badRequest("Invalid phone number format"));
        
        // Perform PUT request with invalid phone
        performRequest(mockMvc.perform(put("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPatientDto))))
                .andExpect(status().isBadRequest());
    }
    
    /**
     * Testcase 12 `testGetProfile_WithOptionalFieldsNull`
     * - Goal: Verify GET "/api/patient/profile" returns correct structure with optional fields set to null.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication.
     *   2. Create a PatientDto with minimal data (some optional fields set to null).
     *   3. Mock patientService.findByUserId() to return this minimal PatientDto.
     *   4. Perform a GET request to "/api/patient/profile".
     *   5. Assert the response status is 200 OK.
     *   6. Assert the response body shows null values for the optional fields.
     * - Input: Valid JWT token for a user with minimal profile data.
     * - Expected Output: HTTP Status 200 OK with PatientDto containing null values for optional fields.
     * - Note: Tests handling of optional fields in API responses.
     */
    @Test
    @Order(12)
    @DisplayName("TC-PC-012: Patient Profile Retrieval - With Optional Fields Null")
    void testGetProfile_WithOptionalFieldsNull() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-minimal");
        
        // Create patient with minimal data
        PatientDto minimalPatientDto = new PatientDto();
        minimalPatientDto.setId("uuid-patient-minimal-profile");
        minimalPatientDto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        // Optional fields set to null
        minimalPatientDto.setInsuranceCode(null);
        minimalPatientDto.setDistrict(null);
        
        UserDto minimalUserDto = new UserDto();
        minimalUserDto.setId("uuid-patient-minimal");
        minimalUserDto.setEmail("minimal@clinic.com");
        minimalUserDto.setFullName("Min Patient");
        
        minimalPatientDto.setUser(minimalUserDto);
        
        // Mock service to return minimal profile
        when(patientService.findByUserId("uuid-patient-minimal")).thenReturn(minimalPatientDto);
        
        // Perform GET request
        performRequest(mockMvc.perform(get("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(minimalPatientDto.getId()))
                .andExpect(jsonPath("$.data.insuranceCode").value(nullValue()))
                .andExpect(jsonPath("$.data.district").value(nullValue()));
    }
    
    /**
     * Testcase 13 `testCreateProfile_MinimalRequiredData`
     * - Goal: Verify POST "/api/patient/profile" accepts the minimal required data.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication.
     *   2. Create a PatientDto with only the required fields.
     *   3. Mock patientService.create() to return a created PatientDto with generated ID.
     *   4. Perform a POST request to "/api/patient/profile".
     *   5. Assert the response status is 200 OK.
     *   6. Assert the response body contains the created PatientDto with generated ID.
     * - Input: Valid JWT token and minimal valid PatientDto.
     * - Expected Output: HTTP Status 200 OK with created PatientDto.
     * - Note: Tests that only required fields are sufficient for creation.
     */
    @Test
    @Order(13)
    @DisplayName("TC-PC-013: Patient Profile Creation - Minimal Required Data")
    void testCreateProfile_MinimalRequiredData() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-user-new");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-user-new");
        
        // Create minimal valid PatientDto
        PatientDto minimalPatientDto = new PatientDto();
        minimalPatientDto.setDateOfBirth(LocalDate.of(1995, 2, 10));
        
        UserDto minimalUserDto = new UserDto();
        minimalUserDto.setEmail("minimal@clinic.com");
        minimalUserDto.setFullName("Min Patient");
        
        minimalPatientDto.setUser(minimalUserDto);
        
        // Mock service to return created profile with generated ID
        PatientDto createdPatientDto = new PatientDto();
        createdPatientDto.setId("generated-uuid");
        createdPatientDto.setDateOfBirth(LocalDate.of(1995, 2, 10));
        createdPatientDto.setUser(minimalUserDto);
        
        when(patientService.create(any(PatientDto.class))).thenReturn(createdPatientDto);
        
        // Perform POST request with minimal data
        performRequest(mockMvc.perform(post("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minimalPatientDto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("generated-uuid"));
    }
    
    /**
     * Testcase 14 `testUpdateProfile_PartialUpdate`
     * - Goal: Verify PUT "/api/patient/profile" allows updating only a subset of fields.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication for an existing patient.
     *   2. Create a PatientDto with only the field to update (addressLine).
     *   3. Mock patientService.update() to return a fully populated PatientDto with updated address.
     *   4. Perform a PUT request to "/api/patient/profile".
     *   5. Assert the response status is 200 OK.
     *   6. Assert the response body shows the updated address and preserved other fields.
     * - Input: Valid JWT token and PatientDto with only addressLine updated.
     * - Expected Output: HTTP Status 200 OK with PatientDto containing updated address and preserved fields.
     * - Note: Tests partial update capability.
     */
    @Test
    @Order(14)
    @DisplayName("TC-PC-014: Patient Profile Update - Partial Update")
    void testUpdateProfile_PartialUpdate() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-1");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-patient-1");
        
        // Create partial update DTO (only address updated)
        PatientDto partialUpdateDto = new PatientDto();
        partialUpdateDto.setAddressLine("Updated Address Only");
        
        UserDto userDto = new UserDto();
        userDto.setId("uuid-patient-1");
        partialUpdateDto.setUser(userDto);
        
        // Mock original data to be returned after update
        PatientDto updatedPatientDto = new PatientDto();
        updatedPatientDto.setId("uuid-patient-1-profile");
        updatedPatientDto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        updatedPatientDto.setAddressLine("Updated Address Only"); // Only this changed
        updatedPatientDto.setDistrict("Wellness");
        updatedPatientDto.setCity("MedCity");
        updatedPatientDto.setInsuranceCode("INS12345");
        updatedPatientDto.setUser(mockUserDto);
        
        when(patientService.update(any(PatientDto.class))).thenReturn(updatedPatientDto);
        
        // Perform PUT request with partial update
        performRequest(mockMvc.perform(put("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdateDto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.addressLine").value("Updated Address Only"))
                .andExpect(jsonPath("$.data.district").value("Wellness")); // Other field unchanged
    }
    
    /**
     * Testcase 15 `testCreateProfile_BoundaryAgeValues`
     * - Goal: Verify creating a profile with boundary age values (0 and 120).
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication.
     *   2. Set user age to boundary value (0).
     *   3. Mock patientService.create() to return a created PatientDto.
     *   4. Perform a POST request and verify success.
     *   5. Repeat with another boundary value (120).
     * - Input: Valid JWT token and PatientDto with age set to boundary values.
     * - Expected Output: HTTP Status 200 OK for both requests.
     * - Note: Tests boundary conditions for numeric fields like age.
     */
    @Test
    @Order(15)
    @DisplayName("TC-PC-015: Patient Profile Creation - Boundary Age Values")
    void testCreateProfile_BoundaryAgeValues() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-user-new");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-user-new");
        
        // Test with age = 0
        mockUserDto.setAge(0);
        
        // Mock service to return created profile
        when(patientService.create(any(PatientDto.class))).thenReturn(mockPatientDto);
        
        // Perform POST request with age = 0
        performRequest(mockMvc.perform(post("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPatientDto))))
                .andExpect(status().isOk());
        
        // Test with high age
        mockUserDto.setAge(120);
        
        // Perform POST request with age = 120
        performRequest(mockMvc.perform(post("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPatientDto))))
                .andExpect(status().isOk());
    }
    
    /**
     * Testcase 16 `testCreateProfile_ValidGenderEnumValues`
     * - Goal: Verify creating a profile with different valid enum values for gender.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication.
     *   2. Set user gender to Male and verify creation succeeds.
     *   3. Set user gender to Female and verify creation succeeds.
     *   4. Set user gender to Other and verify creation succeeds.
     * - Input: Valid JWT token and PatientDto with different gender enum values.
     * - Expected Output: HTTP Status 200 OK for all requests.
     * - Note: Tests handling of enum data types.
     */
    @Test
    @Order(16)
    @DisplayName("TC-PC-016: Patient Profile Creation - Valid Gender Enum Values")
    void testCreateProfile_ValidGenderEnumValues() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-user-new");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-user-new");
        
        // Mock service to return created profile
        when(patientService.create(any(PatientDto.class))).thenReturn(mockPatientDto);
        
        // Test with Male gender
        mockUserDto.setGender(UserGender.Male);
        performRequest(mockMvc.perform(post("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPatientDto))))
                .andExpect(status().isOk());
        
        // Test with Female gender
        mockUserDto.setGender(UserGender.Female);
        performRequest(mockMvc.perform(post("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPatientDto))))
                .andExpect(status().isOk());
        
        // Test with Other gender
        mockUserDto.setGender(UserGender.Other);
        performRequest(mockMvc.perform(post("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPatientDto))))
                .andExpect(status().isOk());
    }
    
    
    /**
     * Testcase 17 `testGetProfile_WithSpecialCharacters`
     * - Goal: Verify GET "/api/patient/profile" correctly retrieves profile with special characters.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication.
     *   2. Create a PatientDto with special characters in user's fullName and addressLine.
     *   3. Mock patientService.findByUserId() to return the PatientDto with special characters.
     *   4. Perform a GET request to "/api/patient/profile".
     *   5. Assert the response status is 200 OK.
     *   6. Assert the response body correctly includes the special characters.
     * - Input: Valid JWT token for user with special character data.
     * - Expected Output: HTTP Status 200 OK with correctly encoded special characters.
     * - Note: Tests handling of non-ASCII characters and proper encoding.
     */
    @Test
    @Order(17)
    @DisplayName("TC-PC-017: Patient Profile Retrieval - With Special Characters")
    void testGetProfile_WithSpecialCharacters() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-special");
        
        // Create patient with special characters
        PatientDto specialPatientDto = new PatientDto();
        specialPatientDto.setId("uuid-patient-special-profile");
        specialPatientDto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        specialPatientDto.setAddressLine("123 Müller Str.");
        
        UserDto specialUserDto = new UserDto();
        specialUserDto.setId("uuid-patient-special");
        specialUserDto.setEmail("special@clinic.com");
        specialUserDto.setFullName("Renée O'Malley");
        
        specialPatientDto.setUser(specialUserDto);
        
        // Mock service to return profile with special chars
        when(patientService.findByUserId("uuid-patient-special")).thenReturn(specialPatientDto);
        
        // Perform GET request
        performRequest(mockMvc.perform(get("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.addressLine").value("123 Müller Str."))
                .andExpect(jsonPath("$.data.user.fullName").value("Renée O'Malley"));
    }
    
    /**
     * Testcase 18 `testCreateProfile_MaxLengthStrings`
     * - Goal: Verify POST "/api/patient/profile" handles maximum string length values.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication.
     *   2. Create a PatientDto with fields at maximum allowed length (e.g., 255 chars for address).
     *   3. Mock patientService.create() to return the created PatientDto.
     *   4. Perform a POST request to "/api/patient/profile".
     *   5. Assert the response status is 200 OK.
     * - Input: Valid JWT token and PatientDto with max-length strings.
     * - Expected Output: HTTP Status 200 OK.
     * - Note: Tests boundary conditions for string length validation.
     */
    @Test
    @Order(18)
    @DisplayName("TC-PC-018: Patient Profile Creation - Max Length Strings")
    void testCreateProfile_MaxLengthStrings() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-user-new");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-user-new");
        
        // Create patient with max length strings (assuming max address length is 255)
        PatientDto maxLengthPatientDto = new PatientDto();
        maxLengthPatientDto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        maxLengthPatientDto.setAddressLine("A".repeat(255));
        
        UserDto maxLengthUserDto = new UserDto();
        maxLengthUserDto.setEmail("max@clinic.com");
        maxLengthUserDto.setFullName("A".repeat(50)); // Assuming max length is 50
        
        maxLengthPatientDto.setUser(maxLengthUserDto);
        
        // Mock service to return created profile
        when(patientService.create(any(PatientDto.class))).thenReturn(maxLengthPatientDto);
        
        // Perform POST request with max length strings
        performRequest(mockMvc.perform(post("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxLengthPatientDto))))
                .andExpect(status().isOk());
    }
    
    /**
     * Testcase 19 `testUpdateProfile_MinLengthStrings`
     * - Goal: Verify PUT "/api/patient/profile" enforces minimum string length validation.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication for an existing patient.
     *   2. Create a PatientDto with user.fullName below minimum length requirement.
     *   3. Mock patientService.update() to throw an exception for validation failure.
     *   4. Perform a PUT request to "/api/patient/profile".
     *   5. Assert the response status is 400 Bad Request.
     * - Input: Valid JWT token and PatientDto with below-minimum string length.
     * - Expected Output: HTTP Status 400 Bad Request.
     * - Note: Tests minimum string length validation.
     */
    @Test
    @Order(19)
    @DisplayName("TC-PC-019: Patient Profile Update - Min Length Strings")
    void testUpdateProfile_MinLengthStrings() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-1");
        when(jwtAuthenticationManager.getClaim("sub")).thenReturn("auth0|uuid-patient-1");
        
        // Create patient with min length strings
        PatientDto minLengthPatientDto = new PatientDto();
        minLengthPatientDto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        minLengthPatientDto.setAddressLine("A"); // Min length 1
        
        UserDto minLengthUserDto = new UserDto();
        minLengthUserDto.setId("uuid-patient-1");
        minLengthUserDto.setEmail("min@clinic.com");
        minLengthUserDto.setFullName("Jo"); // Min length 3 according to validation
        
        minLengthPatientDto.setUser(minLengthUserDto);
        
        // Mock service to throw validation exception due to min length
        when(patientService.update(any(PatientDto.class)))
                .thenThrow(HttpException.badRequest("fullName must be at least 3 characters"));
        
        // Perform PUT request with min length strings
        performRequest(mockMvc.perform(put("/api/patient/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minLengthPatientDto))))
                .andExpect(status().isBadRequest());
    }
}
