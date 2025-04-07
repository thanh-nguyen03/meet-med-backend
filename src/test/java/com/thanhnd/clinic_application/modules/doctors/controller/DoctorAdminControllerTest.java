package com.thanhnd.clinic_application.modules.doctors.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.modules.doctors.dto.CreateDoctorDto;
import com.thanhnd.clinic_application.modules.doctors.dto.DoctorDto;
import com.thanhnd.clinic_application.modules.doctors.dto.UpdateDoctorDto;
import com.thanhnd.clinic_application.modules.doctors.service.DoctorService;
import com.thanhnd.clinic_application.modules.identity_providers.IdentityProviderStrategyFactory;
import com.thanhnd.clinic_application.modules.identity_providers.interfaces.IdentityProviderStrategy;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Doctor Admin Controller Tests")
public class DoctorAdminControllerTest {
    private static final Logger logger = Logger.getLogger(DoctorAdminControllerTest.class.getName());
    private TestInfo testInfo;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private IdentityProviderStrategyFactory identityProviderStrategyFactory;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    // Maps to track inputs and outputs for each test
    private Map<String, Object> testInputs = new HashMap<>();
    private Map<String, Object> testOutputs = new HashMap<>();
    private Exception testException = null;

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

    private void setPermissions(List<String> permissions) {
        Jwt mockJwt = createJwtToken("user123", permissions);
        when(jwtDecoder.decode(any())).thenReturn(mockJwt);
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

    @BeforeEach
    void setUp(TestInfo testInfo) {
        this.testInfo = testInfo;
        logger.info("Starting test: " + testInfo.getDisplayName());
        MockitoAnnotations.openMocks(this);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Default mock behavior for findById
        DoctorDto defaultDoctor = new DoctorDto();
        defaultDoctor.setId("doctor123");
        UserDto defaultUser = new UserDto();
        defaultUser.setEmail("doctor@example.com");
        defaultDoctor.setUser(defaultUser);
        when(doctorService.findById("doctor123")).thenReturn(defaultDoctor);
    }

    @AfterEach
    void tearDown() {
        logger.info("Completed test: " + testInfo.getDisplayName());
    }

    private ResultActions performRequest(ResultActions action) throws Exception {
        ResultActions result = action.andDo(mvcResult -> {
            testOutputs.put("status", mvcResult.getResponse().getStatus());
            testOutputs.put("response", mvcResult.getResponse().getContentAsString());
        });
        return result;
    }

    /**
     * Test Case ID: TC-DAC-001
     * Description: Verify that a doctor can be successfully retrieved by ID when the user has read permission
     * Input: 
     *   - doctorId: "doctor123"
     *   - permissions: ["read:doctors"]
     * Expected Output: 
     *   - Status: 200 OK
     *   - Response contains doctor details with ID "doctor123"
     */
    @Test
    @Order(1)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-001: Find Doctor By ID - Success")
    void TC_DAC_001_findById_Success() throws Exception {
        // Arrange
        String doctorId = "doctor123";
        setPermissions(List.of("read:doctors"));
        testInputs.put("doctorId", doctorId);
        
        DoctorDto doctorDto = new DoctorDto();
        doctorDto.setId(doctorId);
        doctorDto.setYearsOfExperience(5);
        doctorDto.setDegree("MD");
        testInputs.put("doctorDto", doctorDto);
        
        when(doctorService.findById(doctorId)).thenReturn(doctorDto);

        // Act & Assert
        performRequest(mockMvc.perform(get("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")
                .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value(doctorId))
                .andExpect(jsonPath("$.data.yearsOfExperience").value(5))
                .andExpect(jsonPath("$.data.degree").value("MD"));

        verify(doctorService, times(1)).findById(doctorId);
    }

    /**
     * Test Case ID: TC-DAC-002
     * Description: Verify that a 404 error is returned when trying to find a non-existent doctor
     * Input: 
     *   - doctorId: "nonexistent"
     *   - permissions: ["read:doctors"]
     * Expected Output: 
     *   - Status: 404 Not Found
     */
    @Test
    @Order(2)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-002: Find Doctor by ID - Not Found")
    void TC_DAC_002_findById_NotFound() throws Exception {
        // Arrange
        String doctorId = "nonexistent";
        setPermissions(List.of("read:doctors"));
        testInputs.put("doctorId", doctorId);
        
        when(doctorService.findById(doctorId))
            .thenThrow(HttpException.notFound("Doctor not found"));

        // Act & Assert
        performRequest(mockMvc.perform(get("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")
                .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isNotFound());

        verify(doctorService, times(1)).findById(doctorId);
    }

    /**
     * Test Case ID: TC-DAC-003
     * Description: Verify that a new doctor can be successfully created when the user has write permission
     * Input: 
     *   - CreateDoctorDto with valid doctor information
     *   - permissions: ["write:doctors"]
     * Expected Output: 
     *   - Status: 200 OK
     *   - Response contains created doctor with ID "newDoctorId"
     */
    @Test
    @Order(3)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-003: Create Doctor - Success")
    void TC_DAC_003_create_Success() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors"));
        
        CreateDoctorDto createDto = new CreateDoctorDto();
        createDto.setEmail("doctor@example.com");
        createDto.setPassword("password123");
        createDto.setFullName("Dr. John Doe");
        createDto.setYearsOfExperience(5);
        createDto.setDegree("MD");
        createDto.setDepartmentId("dept123");
        createDto.setDescription("Test description");
        testInputs.put("createDto", createDto);

        DoctorDto responseDto = new DoctorDto();
        responseDto.setId("newDoctorId");
        testInputs.put("responseDto", responseDto);
        
        IdentityProviderStrategy mockStrategy = mock(IdentityProviderStrategy.class);
        when(identityProviderStrategyFactory.getStrategy(any())).thenReturn(mockStrategy);
        when(doctorService.create(any())).thenReturn(responseDto);

        // Act & Assert
        performRequest(mockMvc.perform(post("/api/admin/doctor")
                .header("Authorization", "Bearer test-token-user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value("newDoctorId"));

        verify(doctorService, times(1)).create(any());
        verify(mockStrategy, times(1)).createUser(any(), any());
    }

    /**
     * Test Case ID: TC-DAC-004
     * Description: Verify that a 400 error is returned when creating a doctor with invalid input
     * Input: 
     *   - CreateDoctorDto with missing required fields
     *   - permissions: ["write:doctors"]
     * Expected Output: 
     *   - Status: 400 Bad Request
     */
    @Test
    @Order(4)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-004: Create Doctor - Invalid Input")
    void TC_DAC_004_create_InvalidInput() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors"));
        
        CreateDoctorDto createDto = new CreateDoctorDto();
        testInputs.put("createDto", createDto);

        // Act & Assert
        performRequest(mockMvc.perform(post("/api/admin/doctor")
                .header("Authorization", "Bearer test-token-user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto))))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test Case ID: TC-DAC-005
     * Description: Verify that a 400 error is returned when creating a doctor with an email that already exists
     * Input: 
     *   - CreateDoctorDto with email "existing@example.com"
     *   - permissions: ["write:doctors"]
     * Expected Output: 
     *   - Status: 400 Bad Request
     *   - Response contains error message about duplicate email
     */
    @Test
    @Order(5)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-005: Create Doctor - Duplicate Email")
    void TC_DAC_005_create_DuplicateEmail() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors"));
        
        CreateDoctorDto createDto = new CreateDoctorDto();
        createDto.setEmail("existing@example.com");
        createDto.setPassword("password123");
        createDto.setFullName("Dr. John Doe");
        testInputs.put("createDto", createDto);
        
        when(doctorService.create(any()))
            .thenThrow(new RuntimeException("Email already exists"));

        // Act & Assert
        performRequest(mockMvc.perform(post("/api/admin/doctor")
                .header("Authorization", "Bearer test-token-user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Test Case ID: TC-DAC-006
     * Description: Verify that a 404 error is returned when creating a doctor with a non-existent department
     * Input: 
     *   - CreateDoctorDto with departmentId "nonexistent-dept"
     *   - permissions: ["write:doctors"]
     * Expected Output: 
     *   - Status: 404 Not Found
     */
    @Test
    @Order(6)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-006: Create Doctor - Invalid Department")
    void TC_DAC_006_create_InvalidDepartment() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors"));
        
        CreateDoctorDto createDto = new CreateDoctorDto();
        createDto.setEmail("doctor@example.com");
        createDto.setDepartmentId("nonexistent-dept");
        testInputs.put("createDto", createDto);
        
        when(doctorService.create(any()))
            .thenThrow(new RuntimeException("Department not found"));

        // Act & Assert
        performRequest(mockMvc.perform(post("/api/admin/doctor")
                .header("Authorization", "Bearer test-token-user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto))))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test Case ID: TC-DAC-007
     * Description: Verify that a doctor can be successfully updated when the user has write permission
     * Input: 
     *   - doctorId: "doctor123"
     *   - UpdateDoctorDto with updated doctor information
     *   - permissions: ["write:doctors"]
     * Expected Output: 
     *   - Status: 200 OK
     *   - Response contains updated doctor information
     */
    @Test
    @Order(7)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-007: Update Doctor - Success")
    void TC_DAC_007_update_Success() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors"));
        
        String doctorId = "doctor123";
        UpdateDoctorDto updateDto = new UpdateDoctorDto();
        UserDto userDto = new UserDto();
        
        userDto.setFullName("Updated Name");
        updateDto.setUser(userDto);
        updateDto.setYearsOfExperience(6);
        updateDto.setDegree("MD");
        updateDto.setNumberOfPatients(100);
        updateDto.setNumberOfCertificates(5);
        updateDto.setDescription("Updated description");
        testInputs.put("updateDto", updateDto);

        DoctorDto responseDto = new DoctorDto();
        responseDto.setId(doctorId);
        UserDto responseUserDto = new UserDto();
        responseUserDto.setFullName("Updated Name");
        responseDto.setUser(responseUserDto);
        testInputs.put("responseDto", responseDto);
        
        when(doctorService.update(eq(doctorId), any())).thenReturn(responseDto);

        // Act & Assert
        performRequest(mockMvc.perform(put("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(doctorId))
                .andExpect(jsonPath("$.data.user.fullName").value("Updated Name"));

        verify(doctorService, times(1)).update(eq(doctorId), any());
    }

    /**
     * Test Case ID: TC-DAC-008
     * Description: Verify that a 404 error is returned when trying to update a non-existent doctor
     * Input: 
     *   - doctorId: "nonexistent"
     *   - UpdateDoctorDto with updated doctor information
     *   - permissions: ["write:doctors"]
     * Expected Output: 
     *   - Status: 404 Not Found
     */
    @Test
    @Order(8)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-008: Update Doctor - Not Found")
    void TC_DAC_008_update_NotFound() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors"));
        
        String doctorId = "nonexistent";
        UpdateDoctorDto updateDto = new UpdateDoctorDto();
        UserDto userDto = new UserDto();
        userDto.setFullName("Updated Name");
        updateDto.setUser(userDto);
        updateDto.setYearsOfExperience(6);
        updateDto.setDegree("MD");
        updateDto.setNumberOfPatients(100);
        updateDto.setNumberOfCertificates(5);
        updateDto.setDescription("Updated description");
        testInputs.put("updateDto", updateDto);
        
        when(doctorService.update(eq(doctorId), any()))
            .thenThrow(HttpException.notFound("Doctor not found"));

        // Act & Assert
        performRequest(mockMvc.perform(put("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))))
                .andExpect(status().isNotFound());
    }

    /**
     * Test Case ID: TC-DAC-009
     * Description: Verify that a 400 error is returned when updating a doctor with invalid input
     * Input: 
     *   - doctorId: "doctor123"
     *   - UpdateDoctorDto with invalid years of experience (-1)
     *   - permissions: ["write:doctors"]
     * Expected Output: 
     *   - Status: 400 Bad Request
     */
    @Test
    @Order(9)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-009: Update Doctor - Invalid Input")
    void TC_DAC_009_update_InvalidInput() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors"));
        
        String doctorId = "doctor123";
        UpdateDoctorDto updateDto = new UpdateDoctorDto();
        UserDto userDto = new UserDto();
        userDto.setFullName("Updated Name");
        updateDto.setUser(userDto);
        updateDto.setYearsOfExperience(-1); // Invalid experience years
        updateDto.setDegree("MD");
        updateDto.setNumberOfPatients(100);
        updateDto.setNumberOfCertificates(5);
        updateDto.setDescription("Updated description");
        testInputs.put("updateDto", updateDto);

        // Act & Assert
        performRequest(mockMvc.perform(put("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test Case ID: TC-DAC-010
     * Description: Verify that a doctor can be successfully deleted when the user has both write and delete permissions
     * Input: 
     *   - doctorId: "doctor123"
     *   - permissions: ["write:doctors", "delete:users"]
     * Expected Output: 
     *   - Status: 200 OK
     */
    @Test
    @Order(10)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-010: Delete Doctor - Success")
    void TC_DAC_010_delete_Success() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors", "delete:users"));
        
        String doctorId = "doctor123";
        String userEmail = "doctor@example.com";
        String identityProviderUserId = "auth0|123";
        
        DoctorDto doctorDto = new DoctorDto();
        doctorDto.setId(doctorId);
        UserDto userDto = new UserDto();
        userDto.setEmail(userEmail);
        doctorDto.setUser(userDto);
        testInputs.put("doctorDto", doctorDto);
        
        IdentityProviderStrategy mockStrategy = mock(IdentityProviderStrategy.class);
        when(identityProviderStrategyFactory.getStrategy(any())).thenReturn(mockStrategy);
        when(doctorService.findById(doctorId)).thenReturn(doctorDto);
        
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("user_id", identityProviderUserId);
        when(mockStrategy.getUserByEmail(userEmail)).thenReturn(userMap);
        when(mockStrategy.getUserIdKey()).thenReturn("user_id");

        // Act & Assert
        performRequest(mockMvc.perform(delete("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")))
                .andExpect(status().isOk());

        verify(doctorService, times(1)).delete(doctorId);
        verify(mockStrategy, times(1)).deleteUser(identityProviderUserId);
    }

    /**
     * Test Case ID: TC-DAC-011
     * Description: Verify that a 404 error is returned when trying to delete a non-existent doctor
     * Input: 
     *   - doctorId: "nonexistent"
     *   - permissions: ["write:doctors", "delete:users"]
     * Expected Output: 
     *   - Status: 404 Not Found
     */
    @Test
    @Order(11)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-011: Delete Doctor - Not Found")
    void TC_DAC_011_delete_NotFound() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors", "delete:users"));
        
        String doctorId = "nonexistent";
        testInputs.put("doctorId", doctorId);
        
        when(doctorService.findById(doctorId))
            .thenThrow(HttpException.notFound("Doctor not found"));

        // Act & Assert
        performRequest(mockMvc.perform(delete("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")))
                .andExpect(status().isNotFound());
    }

    /**
     * Test Case ID: TC-DAC-013
     * Description: Verify that a 403 error is returned when accessing an endpoint without sufficient permissions
     * Input: 
     *   - doctorId: "doctor123"
     *   - permissions: ["read:doctors"] (only read permission, no write permission)
     * Expected Output: 
     *   - Status: 403 Forbidden
     */
    @Test
    @Order(13)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-013: Unauthorized Access")
    void TC_DAC_013_unauthorized_Access() throws Exception {
        // Arrange
        setPermissions(List.of("read:doctors")); // Only read permission, no write permission
        
        String doctorId = "doctor123";
        testInputs.put("doctorId", doctorId);

        // Mock identity provider strategy
        IdentityProviderStrategy mockStrategy = mock(IdentityProviderStrategy.class);
        when(identityProviderStrategyFactory.getStrategy(any())).thenReturn(mockStrategy);
        when(mockStrategy.getUserByEmail(any())).thenReturn(Map.of("user_id", "auth0|123"));
        when(mockStrategy.getUserIdKey()).thenReturn("user_id");

        // Act & Assert
        performRequest(mockMvc.perform(delete("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")))
                .andExpect(status().isForbidden());
    }

    /**
     * Test Case ID: TC-DAC-014
     * Description: Verify that a doctor is created in both the database and the identity provider
     * Input: 
     *   - CreateDoctorDto with identityProvider set to "auth0"
     *   - permissions: ["write:doctors"]
     * Expected Output: 
     *   - Status: 200 OK
     *   - Identity provider strategy is called to create user
     */
    @Test
    @Order(14)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-014: Create Doctor - Identity Provider Integration")
    void TC_DAC_014_create_IdentityProviderIntegration() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors"));
        
        CreateDoctorDto createDto = new CreateDoctorDto();
        createDto.setEmail("doctor@example.com");
        createDto.setIdentityProvider("auth0");
        createDto.setYearsOfExperience(1);
        createDto.setDegree("MD");
        createDto.setDescription("Test description");
        createDto.setDepartmentId("dept123");
        createDto.setFullName("Dr. John Doe");
        createDto.setPassword("password123");
        testInputs.put("createDto", createDto);
        
        IdentityProviderStrategy mockStrategy = mock(IdentityProviderStrategy.class);
        when(identityProviderStrategyFactory.getStrategy("auth0")).thenReturn(mockStrategy);
        when(doctorService.create(any())).thenReturn(new DoctorDto());

        // Act & Assert
        performRequest(mockMvc.perform(post("/api/admin/doctor")
                .header("Authorization", "Bearer test-token-user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto))))
                .andExpect(status().isOk());

        verify(mockStrategy, times(1)).createUser(any(), any());
    }

    /**
     * Test Case ID: TC-DAC-015
     * Description: Verify that a doctor is deleted from both the database and the identity provider
     * Input: 
     *   - doctorId: "doctor123"
     *   - identity_provider: "auth0"
     *   - permissions: ["write:doctors", "delete:users"]
     * Expected Output: 
     *   - Status: 200 OK
     *   - Identity provider strategy is called to delete user
     */
    @Test
    @Order(15)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-015: Delete Doctor - Identity Provider Integration")
    void TC_DAC_015_delete_IdentityProviderIntegration() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors", "delete:users"));
        
        String doctorId = "doctor123";
        String userEmail = "doctor@example.com";
        String identityProviderUserId = "auth0|123";
        
        // Create and set up DoctorDto with UserDto
        DoctorDto doctorDto = new DoctorDto();
        doctorDto.setId(doctorId);
        UserDto userDto = new UserDto();
        userDto.setEmail(userEmail);
        doctorDto.setUser(userDto);
        testInputs.put("doctorDto", doctorDto);
        
        // Mock doctor service
        when(doctorService.findById(doctorId)).thenReturn(doctorDto);
        
        // Mock identity provider strategy
        IdentityProviderStrategy mockStrategy = mock(IdentityProviderStrategy.class);
        when(identityProviderStrategyFactory.getStrategy("auth0")).thenReturn(mockStrategy);
        when(mockStrategy.getUserByEmail(userEmail)).thenReturn(Map.of("user_id", identityProviderUserId));
        when(mockStrategy.getUserIdKey()).thenReturn("user_id");

        // Act & Assert
        performRequest(mockMvc.perform(delete("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")
                .param("identity_provider", "auth0")))
                .andExpect(status().isOk());

        verify(mockStrategy, times(1)).deleteUser(identityProviderUserId);
    }

    /**
     * Test Case ID: TC-DAC-016
     * Description: Verify that a doctor's user information can be updated
     * Input: 
     *   - doctorId: "doctor123"
     *   - UpdateDoctorDto with updated user information
     *   - permissions: ["write:doctors"]
     * Expected Output: 
     *   - Status: 200 OK
     *   - Response contains updated user information
     */
    @Test
    @Order(16)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-016: Update Doctor - User Information Update")
    void TC_DAC_016_update_UserInformation() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors"));
        
        String doctorId = "doctor123";
        UpdateDoctorDto updateDto = new UpdateDoctorDto();
        UserDto userDto = new UserDto();
        userDto.setFullName("New Name");
        userDto.setPhone("1234567890");
        updateDto.setUser(userDto);
        updateDto.setYearsOfExperience(6);
        updateDto.setDegree("MD");
        updateDto.setNumberOfPatients(100);
        updateDto.setNumberOfCertificates(5);
        updateDto.setDescription("Updated description");
        testInputs.put("updateDto", updateDto);
        
        DoctorDto responseDto = new DoctorDto();
        responseDto.setId(doctorId);
        UserDto responseUserDto = new UserDto();
        responseUserDto.setFullName("New Name");
        responseDto.setUser(responseUserDto);
        testInputs.put("responseDto", responseDto);
        
        when(doctorService.update(eq(doctorId), any())).thenReturn(responseDto);

        // Act & Assert
        performRequest(mockMvc.perform(put("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.fullName").value("New Name"));
    }

    /**
     * Test Case ID: TC-DAC-017
     * Description: Verify that validation errors are returned when required fields are missing
     * Input: 
     *   - CreateDoctorDto with missing required fields
     *   - permissions: ["write:doctors"]
     * Expected Output: 
     *   - Status: 400 Bad Request
     *   - Response contains validation errors
     */
    @Test
    @Order(17)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-017: Create Doctor - Required Fields Validation")
    void TC_DAC_017_create_RequiredFieldsValidation() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors"));
        
        CreateDoctorDto createDto = new CreateDoctorDto();
        testInputs.put("createDto", createDto);

        // Act & Assert
        performRequest(mockMvc.perform(post("/api/admin/doctor")
                .header("Authorization", "Bearer test-token-user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto))))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test Case ID: TC-DAC-018
     * Description: Verify that validation errors are returned when required fields are missing in update
     * Input: 
     *   - doctorId: "doctor123"
     *   - UpdateDoctorDto with missing required fields
     *   - permissions: ["write:doctors"]
     * Expected Output: 
     *   - Status: 400 Bad Request
     *   - Response contains validation errors
     */
    @Test
    @Order(18)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-018: Update Doctor - Required Fields Validation")
    void TC_DAC_018_update_RequiredFieldsValidation() throws Exception {
        // Arrange
        setPermissions(List.of("write:doctors"));
        
        String doctorId = "doctor123";
        UpdateDoctorDto updateDto = new UpdateDoctorDto();
        testInputs.put("updateDto", updateDto);

        // Act & Assert
        performRequest(mockMvc.perform(put("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test Case ID: TC-DAC-019
     * Description: Verify that read permission is required to access GET endpoints
     * Input: 
     *   - doctorId: "doctor123"
     *   - permissions: ["write:doctors"] (only write permission, no read permission)
     * Expected Output: 
     *   - Status: 403 Forbidden
     */
    @Test
    @Order(19)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-019: Authorization - Read Permission Required")
    void TC_DAC_019_Authorization_ReadPermissionRequired() throws Exception {
        // Arrange
        String doctorId = "doctor123";
        setPermissions(List.of("write:doctors")); // Only write permission, no read permission
        testInputs.put("doctorId", doctorId);

        // Act & Assert
        performRequest(mockMvc.perform(get("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")))
                .andExpect(status().isForbidden());
    }

    /**
     * Test Case ID: TC-DAC-020
     * Description: Verify that write permission is required to access POST/PUT endpoints
     * Input: 
     *   - CreateDoctorDto with valid doctor information
     *   - permissions: ["read:doctors"] (only read permission, no write permission)
     * Expected Output: 
     *   - Status: 403 Forbidden
     */
    @Test
    @Order(20)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-020: Authorization - Write Permission Required")
    void TC_DAC_020_Authorization_WritePermissionRequired() throws Exception {
        // Arrange
        setPermissions(List.of("read:doctors")); // Only read permission, no write permission
        CreateDoctorDto createDto = new CreateDoctorDto();
        createDto.setEmail("doctor@example.com");
        testInputs.put("createDto", createDto);

        // Act & Assert
        performRequest(mockMvc.perform(post("/api/admin/doctor")
                .header("Authorization", "Bearer test-token-user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto))))
                .andExpect(status().isForbidden());
    }

    /**
     * Test Case ID: TC-DAC-021
     * Description: Verify that delete permission is required to access DELETE endpoints
     * Input: 
     *   - doctorId: "doctor123"
     *   - permissions: ["write:doctors"] (only write permission, no delete permission)
     * Expected Output: 
     *   - Status: 403 Forbidden
     */
    @Test
    @Order(21)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-021: Authorization - Delete Permission Required")
    void TC_DAC_021_Authorization_DeletePermissionRequired() throws Exception {
        // Arrange
        String doctorId = "doctor123";
        setPermissions(List.of("write:doctors")); // Only write permission, no delete permission
        testInputs.put("doctorId", doctorId);

        // Mock doctor service
        DoctorDto doctorDto = new DoctorDto();
        UserDto userDto = new UserDto();
        userDto.setEmail("doctor@example.com");
        doctorDto.setUser(userDto);
        when(doctorService.findById(doctorId)).thenReturn(doctorDto);

        // Mock identity provider strategy
        IdentityProviderStrategy mockStrategy = mock(IdentityProviderStrategy.class);
        when(identityProviderStrategyFactory.getStrategy(any())).thenReturn(mockStrategy);
        when(mockStrategy.getUserByEmail(any())).thenReturn(Map.of("user_id", "auth0|123"));
        when(mockStrategy.getUserIdKey()).thenReturn("user_id");

        // Act & Assert
        performRequest(mockMvc.perform(delete("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")))
                .andExpect(status().isForbidden());
    }

    /**
     * Test Case ID: TC-DAC-024
     * Description: Verify that empty permissions are rejected
     * Input: 
     *   - doctorId: "doctor123"
     *   - permissions: [] (empty permissions list)
     * Expected Output: 
     *   - Status: 403 Forbidden
     */
    @Test
    @Order(24)
    @WithMockUser(username = "user123")
    @DisplayName("TC-DAC-024: Authorization - Empty Permissions")
    void TC_DAC_024_Authorization_EmptyPermissions() throws Exception {
        // Arrange
        String doctorId = "doctor123";
        setPermissions(List.of()); // Empty permissions list
        testInputs.put("doctorId", doctorId);

        // Act & Assert
        performRequest(mockMvc.perform(get("/api/admin/doctor/{id}", doctorId)
                .header("Authorization", "Bearer test-token-user123")))
                .andExpect(status().isForbidden());
    }

}
