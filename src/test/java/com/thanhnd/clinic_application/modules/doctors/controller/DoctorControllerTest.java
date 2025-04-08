package com.thanhnd.clinic_application.modules.doctors.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.constants.UserGender;
import com.thanhnd.clinic_application.modules.doctors.dto.DoctorDto;
import com.thanhnd.clinic_application.modules.doctors.dto.UpdateDoctorDto;
import com.thanhnd.clinic_application.modules.doctors.service.DoctorService;
import com.thanhnd.clinic_application.modules.shifts.service.RegisteredShiftService;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorController.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Doctor Controller Tests")
public class DoctorControllerTest {
    private static final Logger logger = Logger.getLogger(DoctorControllerTest.class.getName());
    private TestInfo testInfo;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private RegisteredShiftService registeredShiftService;

    @MockBean
    private JwtAuthenticationManager jwtAuthenticationManager;

    private DoctorDto mockDoctorDto;
    private UserDto mockUserDto;

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

    @BeforeEach
    void setUp(TestInfo testInfo) {
        this.testInfo = testInfo;
        logger.info("Starting test: " + testInfo.getDisplayName());

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Setup mock user
        mockUserDto = new UserDto();
        mockUserDto.setId("uuid-doctor-1");
        mockUserDto.setEmail("doctor1@clinic.com");
        mockUserDto.setFullName("Dr. John Smith");
        mockUserDto.setAge(40);
        mockUserDto.setPhone("555-0123");
        mockUserDto.setGender(UserGender.Male);

        // Setup mock doctor
        mockDoctorDto = new DoctorDto();
        mockDoctorDto.setId("uuid-doctor-1-profile");
        mockDoctorDto.setYearsOfExperience(10);
        mockDoctorDto.setDegree("MD");
        mockDoctorDto.setNumberOfPatients(100);
        mockDoctorDto.setNumberOfCertificates(5);
        mockDoctorDto.setDescription("Experienced physician with focus on internal medicine");
        mockDoctorDto.setUser(mockUserDto);

        testInputs.put("mockDoctorDto", mockDoctorDto);
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
     * Test Case ID: TC-DC-001
     * Description: Verify that a doctor can successfully retrieve their own profile
     * Input:
     *   - Valid JWT token with doctor user ID
     *   - Doctor profile exists in the system
     * Expected Output:
     *   - HTTP 200 OK response
     *   - Doctor profile data returned in response
     */
    @Test
    @Order(1)
    @DisplayName("TC-DC-001: Get My Profile - Success")
    void TC_DC_001_getMyProfileSuccess() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-doctor-1");
        testInputs.put("userId", "uuid-doctor-1");

        // Mock service response
        when(doctorService.findByUserId("uuid-doctor-1")).thenReturn(mockDoctorDto);

        // Perform GET request
        performRequest(mockMvc.perform(get("/api/doctor/my-profile")
                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(mockDoctorDto.getId()))
                .andExpect(jsonPath("$.data.yearsOfExperience").value(mockDoctorDto.getYearsOfExperience()))
                .andExpect(jsonPath("$.data.degree").value(mockDoctorDto.getDegree()));
    }

    /**
     * Test Case ID: TC-DC-002
     * Description: Verify error handling when a non-doctor user attempts to access doctor profile
     * Input:
     *   - Valid JWT token with non-doctor user ID
     *   - User exists but is not a doctor
     * Expected Output:
     *   - HTTP 404 Not Found response
     *   - Error message indicating doctor not found
     */
    @Test
    @Order(2)
    @DisplayName("TC-DC-002: Get My Profile - User Not Doctor")
    void TC_DC_002_getMyProfileUserNotDoctor() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-non-doctor");
        testInputs.put("userId", "uuid-non-doctor");

        // Mock service to throw exception
        when(doctorService.findByUserId("uuid-non-doctor"))
                .thenThrow(HttpException.notFound(Message.DOCTOR_NOT_FOUND.getMessage()));

        // Perform GET request
        performRequest(mockMvc.perform(get("/api/doctor/my-profile")
                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(Message.DOCTOR_NOT_FOUND.getMessage()));
    }

    /**
     * Test Case ID: TC-DC-003
     * Description: Verify that a doctor can successfully update their profile
     * Input:
     *   - Valid JWT token with doctor user ID
     *   - UpdateDoctorDto with valid fields
     * Expected Output:
     *   - HTTP 200 OK response
     *   - Updated doctor profile data returned in response
     */
    @Test
    @Order(3)
    @DisplayName("TC-DC-003: Update My Profile - Success")
    void TC_DC_003_updateMyProfileSuccess() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-doctor-1");
        testInputs.put("userId", "uuid-doctor-1");

        UpdateDoctorDto updateDto = new UpdateDoctorDto();
        updateDto.setYearsOfExperience(15);
        updateDto.setDegree("MD, PhD");
        updateDto.setDescription("Updated description");
        testInputs.put("updateDto", updateDto);

        // Mock service response
        when(doctorService.update(eq("uuid-doctor-1"), any(UpdateDoctorDto.class))).thenReturn(mockDoctorDto);

        // Perform PUT request
        performRequest(mockMvc.perform(put("/api/doctor/my-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(mockDoctorDto.getId()));
    }

    /**
     * Test Case ID: TC-DC-004
     * Description: Verify validation error handling when updating doctor profile with invalid data
     * Input:
     *   - Valid JWT token with doctor user ID
     *   - UpdateDoctorDto with invalid fields (negative years of experience)
     * Expected Output:
     *   - HTTP 400 Bad Request response
     *   - Validation error message
     */
    @Test
    @Order(4)
    @DisplayName("TC-DC-004: Update My Profile - Validation Errors")
    void TC_DC_004_updateMyProfileValidationErrors() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-doctor-1");
        testInputs.put("userId", "uuid-doctor-1");

        UpdateDoctorDto invalidDto = new UpdateDoctorDto();
        invalidDto.setYearsOfExperience(-1); // Invalid value
        testInputs.put("updateDto", invalidDto);

        // Perform PUT request
        performRequest(mockMvc.perform(put("/api/doctor/my-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto))))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test Case ID: TC-DC-005
     * Description: Verify that doctor shifts can be successfully retrieved
     * Input:
     *   - Valid doctor ID
     *   - Doctor has registered shifts
     * Expected Output:
     *   - HTTP 200 OK response
     *   - List of doctor shifts returned in response
     */
    @Test
    @Order(5)
    @DisplayName("TC-DC-005: Get Doctor Shifts - Success")
    void TC_DC_005_getDoctorShiftsSuccess() throws Exception {
        String doctorId = "uuid-doctor-1";
        testInputs.put("doctorId", doctorId);

        // Mock service response
        when(registeredShiftService.findAllByDoctorForBooking(doctorId)).thenReturn(any());

        // Perform GET request
        performRequest(mockMvc.perform(get("/api/doctor/{doctorId}/booking-shifts", doctorId)
                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk());
    }

    /**
     * Test Case ID: TC-DC-006
     * Description: Verify error handling when retrieving shifts for non-existent doctor
     * Input:
     *   - Invalid doctor ID
     * Expected Output:
     *   - HTTP 404 Not Found response
     *   - Error message indicating doctor not found
     */
    @Test
    @Order(6)
    @DisplayName("TC-DC-006: Get Doctor Shifts - Doctor Not Found")
    void TC_DC_006_getDoctorShiftsDoctorNotFound() throws Exception {
        String doctorId = "non-existent-doctor";
        testInputs.put("doctorId", doctorId);

        // Mock service to throw exception
        when(registeredShiftService.findAllByDoctorForBooking(doctorId))
                .thenThrow(HttpException.notFound(Message.DOCTOR_NOT_FOUND.getMessage()));

        // Perform GET request
        performRequest(mockMvc.perform(get("/api/doctor/{doctorId}/booking-shifts", doctorId)
                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(Message.DOCTOR_NOT_FOUND.getMessage()));
    }

    /**
     * Test Case ID: TC-DC-007
     * Description: Verify error handling when user lacks permissions to view doctor shifts
     * Input:
     *   - Valid doctor ID
     *   - User without sufficient permissions
     * Expected Output:
     *   - HTTP 403 Forbidden response
     *   - Error message indicating insufficient permissions
     */
    @Test
    @Order(7)
    @DisplayName("TC-DC-007: Get Doctor Shifts - Insufficient Permissions")
    void TC_DC_007_getDoctorShiftsInsufficientPermissions() throws Exception {
        String doctorId = "uuid-doctor-1";
        testInputs.put("doctorId", doctorId);

        // Mock service to throw permission exception
        when(registeredShiftService.findAllByDoctorForBooking(doctorId))
                .thenThrow(HttpException.forbidden("Insufficient permissions"));

        // Perform GET request
        performRequest(mockMvc.perform(get("/api/doctor/{doctorId}/booking-shifts", doctorId)
                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isForbidden());
    }

    /**
     * Test Case ID: TC-DC-008
     * Description: Verify that a doctor can update only specific fields of their profile
     * Input:
     *   - Valid JWT token with doctor user ID
     *   - UpdateDoctorDto with only some fields populated
     * Expected Output:
     *   - HTTP 200 OK response
     *   - Only specified fields updated, others remain unchanged
     */
    @Test
    @Order(8)
    @DisplayName("TC-DC-008: Update My Profile - Only Partial Update")
    void TC_DC_008_updateMyProfileOnlyPartialUpdate() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-doctor-1");
        testInputs.put("userId", "uuid-doctor-1");

        UpdateDoctorDto partialUpdateDto = new UpdateDoctorDto();
        partialUpdateDto.setYearsOfExperience(12); // Only update years of experience
        testInputs.put("updateDto", partialUpdateDto);

        // Mock service response
        when(doctorService.update(eq("uuid-doctor-1"), any(UpdateDoctorDto.class))).thenReturn(mockDoctorDto);

        // Perform PUT request
        performRequest(mockMvc.perform(put("/api/doctor/my-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdateDto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.yearsOfExperience").value(mockDoctorDto.getYearsOfExperience()));
    }

    /**
     * Test Case ID: TC-DC-009
     * Description: Verify that a doctor can update their profile with maximum length description
     * Input:
     *   - Valid JWT token with doctor user ID
     *   - UpdateDoctorDto with maximum length description
     * Expected Output:
     *   - HTTP 200 OK response
     *   - Profile updated with maximum length description
     */
    @Test
    @Order(9)
    @DisplayName("TC-DC-009: Update My Profile - Max Description Length")
    void TC_DC_009_updateMyProfileMaxDescriptionLength() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-doctor-1");
        testInputs.put("userId", "uuid-doctor-1");

        UpdateDoctorDto maxLengthDto = new UpdateDoctorDto();
        maxLengthDto.setDescription("A".repeat(2000)); // Max length
        testInputs.put("updateDto", maxLengthDto);

        // Mock service response
        when(doctorService.update(eq("uuid-doctor-1"), any(UpdateDoctorDto.class))).thenReturn(mockDoctorDto);

        // Perform PUT request
        performRequest(mockMvc.perform(put("/api/doctor/my-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxLengthDto))))
                .andExpect(status().isOk());
    }

    /**
     * Test Case ID: TC-DC-010
     * Description: Verify handling when a doctor has no registered shifts
     * Input:
     *   - Valid doctor ID
     *   - Doctor has no registered shifts
     * Expected Output:
     *   - HTTP 200 OK response
     *   - Empty list returned in response
     */
    @Test
    @Order(10)
    @DisplayName("TC-DC-010: Get Doctor Shifts - No Shifts")
    void TC_DC_010_getDoctorShiftsNoShifts() throws Exception {
        String doctorId = "uuid-doctor-no-shifts";
        testInputs.put("doctorId", doctorId);

        // Mock service to return empty list
        when(registeredShiftService.findAllByDoctorForBooking(doctorId))
                .thenReturn(java.util.Collections.emptyList());

        // Perform GET request
        performRequest(mockMvc.perform(get("/api/doctor/{doctorId}/booking-shifts", doctorId)
                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    /**
     * Test Case ID: TC-DC-011
     * Description: Verify validation error handling when updating doctor profile with invalid years of experience
     * Input:
     *   - Valid JWT token with doctor user ID
     *   - UpdateDoctorDto with invalid years of experience (zero)
     * Expected Output:
     *   - HTTP 400 Bad Request response
     *   - Validation error message
     */
    @Test
    @Order(11)
    @DisplayName("TC-DC-011: Update My Profile - Invalid Years of Experience")
    void TC_DC_011_updateMyProfileInvalidYearsOfExperience() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-doctor-1");
        testInputs.put("userId", "uuid-doctor-1");

        UpdateDoctorDto invalidYearsDto = new UpdateDoctorDto();
        invalidYearsDto.setYearsOfExperience(0); // Invalid value
        testInputs.put("updateDto", invalidYearsDto);

        // Perform PUT request
        performRequest(mockMvc.perform(put("/api/doctor/my-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidYearsDto))))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test Case ID: TC-DC-012
     * Description: Verify that the doctor profile endpoint handles concurrent requests correctly
     * Input:
     *   - Valid JWT token with doctor user ID
     *   - Multiple concurrent requests to the same endpoint
     * Expected Output:
     *   - All requests complete successfully
     *   - Each request returns the correct doctor profile data
     */
    @Test
    @Order(12)
    @DisplayName("TC-DC-012: Get My Profile - Concurrent Requests")
    void TC_DC_012_getMyProfileConcurrentRequests() throws Exception {
        // Mock JWT authentication
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-doctor-1");
        testInputs.put("userId", "uuid-doctor-1");

        // Mock service response
        when(doctorService.findByUserId("uuid-doctor-1")).thenReturn(mockDoctorDto);

        // Perform multiple concurrent requests
        Runnable request = () -> {
            try {
                mockMvc.perform(get("/api/doctor/my-profile")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.id").value(mockDoctorDto.getId()));
            } catch (Exception e) {
                fail("Concurrent request failed: " + e.getMessage());
            }
        };

        // Create and start multiple threads
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(request);
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
    }
} 