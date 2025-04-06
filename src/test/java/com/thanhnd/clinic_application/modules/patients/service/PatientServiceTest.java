package com.thanhnd.clinic_application.modules.patients.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.constants.UserGender;
import com.thanhnd.clinic_application.entity.Patient;
import com.thanhnd.clinic_application.entity.User;
import com.thanhnd.clinic_application.modules.patients.dto.PatientDto;
import com.thanhnd.clinic_application.modules.patients.repository.PatientRepository;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import com.thanhnd.clinic_application.modules.users.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Patient Service Tests")
@Tag("service")
public class PatientServiceTest {

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private JwtAuthenticationManager jwtAuthenticationManager;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private ObjectMapper objectMapper;

    private String TEST_USER_ID = "test-service-user";
    private UserDto userDto;
    private PatientDto patientDto;

    private String currentTestCaseId;
    private static final Pattern TEST_CASE_ID_PATTERN = Pattern.compile("TC-PS-(\\d{3})");
    
    // Variables to track test results
    private boolean testPassed = false;
    private Map<String, Object> testInputs = new HashMap<>();
    private Map<String, Object> testOutputs = new HashMap<>();
    private Exception testException = null;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        // Reset test result tracking variables
        testPassed = false;
        testInputs.clear();
        testOutputs.clear();
        testException = null;
        
        // Initialize object mapper for pretty printing JSON
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.findAndRegisterModules(); // For LocalDate support
        
        // Extract test case ID from the DisplayName
        String displayName = testInfo.getDisplayName();
        Matcher matcher = TEST_CASE_ID_PATTERN.matcher(displayName);
        if (matcher.find()) {
            currentTestCaseId = "TC-PS-" + matcher.group(1);
        }

        // Create a test user in the database with the EXACT SAME ID that JWT will return
        User user = new User();
        user.setEmail("service-test@example.com");
        user.setFullName("Service Test User");
        user.setAge(25);
        user.setPhone("5551234567");
        user.setGender(UserGender.Male);
        User savedUser = userRepository.save(user);
        TEST_USER_ID = savedUser.getId();
        
        // Log saved user for debugging
        System.out.println("Saved user with ID: " + savedUser.getId());
        when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
        
        // Prepare DTOs for testing
        userDto = new UserDto();
        userDto.setId(TEST_USER_ID);  // This must match the saved user ID
        userDto.setEmail("service-test@example.com");
        userDto.setFullName("Service Test User");
        userDto.setAge(25);
        userDto.setPhone("5551234567");
        userDto.setGender(UserGender.Male);

        patientDto = new PatientDto();
        patientDto.setDateOfBirth(LocalDate.of(1998, 4, 15));
        patientDto.setAddressLine("456 Service Test Street");
        patientDto.setDistrict("Service District");
        patientDto.setCity("Service City");
        patientDto.setInsuranceCode("SRV-123456");
        patientDto.setUser(userDto);

        // Track input data
        testInputs.put("userId", TEST_USER_ID);
        testInputs.put("patientDto", patientDto);

        System.out.println("\n======= TEST EXECUTION STARTED: " + (currentTestCaseId != null ? currentTestCaseId : "Unknown Test") + " =======");
        
        // Verify user was actually saved
        Optional<User> foundUser = userRepository.findById(TEST_USER_ID);
        System.out.println("User found in database: " + foundUser.isPresent());
        
        // Print test input
        try {
            System.out.println("\n======= TEST INPUT =======");
            System.out.println("User ID from JWT: " + TEST_USER_ID);
            System.out.println("Patient DTO: " + objectMapper.writeValueAsString(patientDto));
            System.out.println("==========================\n");
        } catch (Exception e) {
            System.out.println("Error printing test input: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        // Print test results
        try {
            System.out.println("\n======= TEST RESULT =======");
            System.out.println("Test Case: " + currentTestCaseId);
            System.out.println("Status: " + (testPassed ? "✅ PASSED" : "❌ FAILED"));
            
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
            System.out.println("==========================\n");
        } catch (Exception e) {
            System.out.println("Error printing test result: " + e.getMessage());
        }
        
        System.out.println("======= TEST COMPLETED: " + currentTestCaseId + " =======\n");
    }

    /**
     * Test Case: TC-PS-001
     * Goal: Verify successful creation of a patient profile
     */
    @Test
    @Order(1)
    @DisplayName("📋 TC-PS-001: Patient Profile Creation")
    void testCreatePatient() {
        try {
            // Step 1: First create JWT authentication with user ID
            System.out.println("Setting up JWT authentication with user ID: " + TEST_USER_ID);
            when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
            
            // Step 2: Create a patient profile (this will use the authenticated user ID)
            System.out.println("Creating patient profile...");
            PatientDto createdPatient = patientService.create(patientDto);
            System.out.println("Patient created with ID: " + createdPatient.getId());
            
            // Track output
            testOutputs.put("createdPatient", createdPatient);
            
            // Flush and clear the persistence context
            entityManager.flush();
            entityManager.clear();

            // Step 3: Verify that patient was created with correct information
            assertNotNull(createdPatient.getId(), "Patient ID should be generated");
            assertEquals("456 Service Test Street", createdPatient.getAddressLine());
            assertEquals("Service District", createdPatient.getDistrict());
            assertEquals("Service City", createdPatient.getCity());
            assertEquals("SRV-123456", createdPatient.getInsuranceCode());
            assertEquals(TEST_USER_ID, createdPatient.getUser().getId());

            // Step 4: Verify patient exists in database
            Optional<Patient> dbPatient = patientRepository.findByUserId(TEST_USER_ID);
            assertTrue(dbPatient.isPresent(), "Patient should exist in database");
            assertEquals("456 Service Test Street", dbPatient.get().getAddressLine());
            
            // Test passed
            testPassed = true;
            
        } catch (AssertionError e) {
            // For assertion errors, just rethrow
            throw e;
        } catch (Exception e) {
            // For any other exceptions, log and track
            System.err.println("Test failed with exception: " + e.getMessage());
            testException = e;
            throw e;
        }
    }

    /**
     * Test Case: TC-PS-002
     * Goal: Verify that a patient can successfully retrieve their profile
     * Input:
     * - Valid JWT token for authenticated user
     * - User has an existing patient profile
     * Expected Output:
     * - Status 200 OK (implied by successful return)
     * - Patient DTO with correct user information
     * Note: Verifies the happy path for profile retrieval
     */
    @Test
    @Order(2)
    @DisplayName("📋 TC-PS-002: Patient Profile Retrieval - Success")
    void testPatientProfileRetrieval() {
        try {
            // Step 1: First create JWT authentication with user ID
            System.out.println("Setting up JWT authentication with user ID: " + TEST_USER_ID);
            when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
            
            // Step 2: Create a patient profile (this will use the authenticated user ID)
            System.out.println("Creating patient profile...");
            PatientDto createdPatient = patientService.create(patientDto);
            System.out.println("Patient created with ID: " + createdPatient.getId());
            testOutputs.put("createdPatient", createdPatient);
            
            // Flush and clear the persistence context 
            entityManager.flush();
            entityManager.clear();
            
            // Step 3: Verify patient was created successfully
            assertNotNull(createdPatient.getId(), "Patient should have been created with an ID");
            
            // Step 4: Retrieve the patient profile
            System.out.println("Retrieving patient profile for user ID: " + TEST_USER_ID);
            PatientDto foundPatient = patientService.findByUserId(TEST_USER_ID);
            System.out.println("Patient profile retrieved successfully");
            testOutputs.put("retrievedPatient", foundPatient);

            // Step 5: Verify retrieved patient information is correct
            assertNotNull(foundPatient, "Patient profile should be found");
            assertEquals(createdPatient.getId(), foundPatient.getId(), "Should return the same patient");
            assertEquals("456 Service Test Street", foundPatient.getAddressLine());
            assertEquals("Service District", foundPatient.getDistrict());
            assertEquals("Service City", foundPatient.getCity());
            assertEquals("SRV-123456", foundPatient.getInsuranceCode());
            
            // Verify user information is correct
            assertNotNull(foundPatient.getUser(), "User information should be included");
            assertEquals(TEST_USER_ID, foundPatient.getUser().getId());
            assertEquals("service-test@example.com", foundPatient.getUser().getEmail());
            assertEquals("Service Test User", foundPatient.getUser().getFullName());
            assertEquals(25, foundPatient.getUser().getAge());
            assertEquals("5551234567", foundPatient.getUser().getPhone());
            assertEquals(UserGender.Male, foundPatient.getUser().getGender());
            
            // Verify against database record
            Optional<Patient> dbPatient = patientRepository.findByUserId(TEST_USER_ID);
            assertTrue(dbPatient.isPresent(), "Patient should exist in database");
            assertEquals(foundPatient.getId(), dbPatient.get().getId(), "IDs should match");
            
            // Test passed
            testPassed = true;
            
        } catch (AssertionError e) {
            // For assertion errors, just rethrow
            throw e;
        } catch (Exception e) {
            // For any other exceptions, log and track
            System.err.println("Test failed with exception: " + e.getMessage());
            testException = e;
            throw e;
        }
    }

    /**
     * Test Case: TC-PS-003
     * Goal: Verify error handling when patient profile not found
     */
    @Test
    @Order(3)
    @DisplayName("📋 TC-PS-003: Patient Not Found Error")
    void testFindNonExistentPatient() {
        try {
            // Given: No patient exists for this user ID
            String nonExistentUserId = "non-existent-user-id";
            when(jwtAuthenticationManager.getUserId()).thenReturn(nonExistentUserId);
            testInputs.put("nonExistentUserId", nonExistentUserId);

            // When/Then: Service should throw exception when patient not found
            HttpException exception = assertThrows(HttpException.class, 
                () -> patientService.findByUserId(nonExistentUserId),
                "Should throw HttpException for non-existent patient");
            
            testOutputs.put("exceptionMessage", exception.getMessage());
            assertEquals(Message.PATIENT_NOT_FOUND.getMessage(), exception.getMessage());
            
            // Test passed if we got the expected exception
            testPassed = true;
        } catch (AssertionError e) {
            // For assertion errors, just rethrow
            throw e;
        } catch (Exception e) {
            // For any other exceptions, log and track
            System.err.println("Test failed with exception: " + e.getMessage());
            testException = e;
            throw e;
        }
    }

    /**
     * Test Case: TC-PS-004
     * Goal: Verify successful update of a patient profile
     */
    @Test
    @Order(4)
    @DisplayName("📋 TC-PS-004: Patient Profile Update")
    void testUpdatePatient() {
        try {
            // Given: A patient exists in the database
            System.out.println("Setting up JWT authentication with user ID: " + TEST_USER_ID);
            when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
            
            // Need to create a patient profile first - this will properly set up the bi-directional relationship
            System.out.println("Creating initial patient...");
            PatientDto createdPatient = patientService.create(patientDto);
            System.out.println("Patient created with ID: " + createdPatient.getId());
            testOutputs.put("initialPatient", createdPatient);
            
            // Flush and clear the persistence context to ensure changes are written to the database
            // and a fresh state will be loaded for subsequent operations
            entityManager.flush();
            entityManager.clear();
            
            // Debug: Check what's in the database directly
            System.out.println("DEBUG: Checking database state after patient creation");
            Optional<Patient> dbPatientAfterCreation = patientRepository.findByUserId(TEST_USER_ID);
            System.out.println("Patient found in database: " + dbPatientAfterCreation.isPresent());
            if (dbPatientAfterCreation.isPresent()) {
                System.out.println("Patient ID: " + dbPatientAfterCreation.get().getId());
                System.out.println("Patient user ID: " + (dbPatientAfterCreation.get().getUser() != null ? dbPatientAfterCreation.get().getUser().getId() : "null"));
            }
            
            // The problem might be with cached entities, so let's get a fresh copy from the database
            Optional<User> userWithPatient = userRepository.findById(TEST_USER_ID);
            assertTrue(userWithPatient.isPresent(), "User should exist");
            
            // Debug: Look at the user's state
            System.out.println("User found in database: " + userWithPatient.isPresent());
            if (userWithPatient.isPresent()) {
                System.out.println("User ID: " + userWithPatient.get().getId());
                System.out.println("User has patient: " + (userWithPatient.get().getPatient() != null));
                if (userWithPatient.get().getPatient() != null) {
                    System.out.println("User's patient ID: " + userWithPatient.get().getPatient().getId());
                }
            }
            
            // When: Update patient information
            System.out.println("Updating patient profile...");
            PatientDto updateDto = new PatientDto();
            updateDto.setId(createdPatient.getId()); // Important: Include the ID of the existing patient
            updateDto.setAddressLine("999 Updated Street");
            updateDto.setDistrict(patientDto.getDistrict());  // Keep original value
            updateDto.setCity("Updated City");
            updateDto.setInsuranceCode("UPD-789");
            updateDto.setDateOfBirth(patientDto.getDateOfBirth());  // Keep original value
            updateDto.setUser(patientDto.getUser());  // Keep original user
            
            testInputs.put("updatedPatientDto", updateDto);
            
            // Make sure JWT still returns the same user ID
            when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
            
            // Perform the update
            PatientDto updatedPatient = patientService.update(updateDto);
            testOutputs.put("updatedPatient", updatedPatient);
            
            // Then: Patient information should be updated
            assertEquals("999 Updated Street", updatedPatient.getAddressLine());
            assertEquals("Updated City", updatedPatient.getCity());
            assertEquals("UPD-789", updatedPatient.getInsuranceCode());
            
            // Verify updates in database
            Optional<Patient> dbPatient = patientRepository.findByUserId(TEST_USER_ID);
            assertTrue(dbPatient.isPresent(), "Patient should exist in database");
            assertEquals("999 Updated Street", dbPatient.get().getAddressLine());
            assertEquals("Updated City", dbPatient.get().getCity());
            
            // Test passed
            testPassed = true;
        } catch (AssertionError e) {
            // For assertion errors, just rethrow
            throw e;
        } catch (Exception e) {
            // For any other exceptions, log and track
            System.err.println("Test failed with exception: " + e.getMessage());
            testException = e;
            throw e;
        }
    }

    /**
     * Test Case: TC-PS-005
     * Goal: Verify error when trying to create duplicate patient profile
     */
    @Test
    @Order(5)
    @DisplayName("📋 TC-PS-005: Duplicate Patient Creation Error")
    void testCreateDuplicatePatient() {
        try {
            // Given: A patient already exists
            System.out.println("Creating initial patient...");
            PatientDto createdPatient = patientService.create(patientDto);
            testOutputs.put("initialPatient", createdPatient);
            
            // Flush and clear the persistence context 
            entityManager.flush();
            entityManager.clear();
            
            // When/Then: Attempting to create another profile should fail
            System.out.println("Attempting to create duplicate patient...");
            HttpException exception = assertThrows(HttpException.class, 
                () -> patientService.create(patientDto),
                "Should throw HttpException for duplicate patient creation");
            
            testOutputs.put("exceptionMessage", exception.getMessage());
            assertEquals(Message.PATIENT_ALREADY_EXISTS.getMessage(), exception.getMessage());
            
            // Test passed if we got the expected exception
            testPassed = true;
        } catch (AssertionError e) {
            // For assertion errors, just rethrow
            throw e;
        } catch (Exception e) {
            // For any other exceptions, log and track
            System.err.println("Test failed with exception: " + e.getMessage());
            testException = e;
            throw e;
        }
    }
} 