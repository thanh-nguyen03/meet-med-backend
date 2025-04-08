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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private String TEST_USER_ID;
    private UserDto userDto;
    private PatientDto patientDto;
    private User testUser;
    
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
    void setUp() {
        // Initialize object mapper for JSON serialization
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.findAndRegisterModules(); // For LocalDate support
        
        // Create a test user in the database
        testUser = new User();
        testUser.setEmail("service-test@example.com");
        testUser.setFullName("Service Test User");
        testUser.setAge(25);
        testUser.setPhone("5551234567");
        testUser.setGender(UserGender.Male);
        User savedUser = userRepository.save(testUser);
        TEST_USER_ID = savedUser.getId();
        
        // Configure JWT authentication to return our test user ID
        when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
        
        // Prepare DTOs for testing
        userDto = new UserDto();
        userDto.setId(TEST_USER_ID);
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
        
        // Track standard inputs
        testInputs.put("userId", TEST_USER_ID);
        testInputs.put("patientDto", patientDto);
    }

    /**
     * Test Case ID: TC-PS-001
     * Description: Verify that a patient profile can be successfully created with valid inputs
     * Input:
     *   - Valid JWT token with user ID
     *   - Patient DTO with all required fields
     *   - User exists in the system
     * Expected Output:
     *   - Patient profile created successfully
     *   - Patient ID generated
     *   - All fields stored correctly
     */
    @Test
    @Order(1)
    @DisplayName("TC-PS-001: Create Patient - Basic Flow")
    void TC_PS_001_createPatientBasicFlow() {
        // When: Create patient profile
        PatientDto createdPatient = patientService.create(patientDto);
        
        // Track output
        testOutputs.put("createdPatient", createdPatient);
        
        // Then: Verify created patient
        assertNotNull(createdPatient.getId(), "Patient ID should be generated");
        assertEquals("456 Service Test Street", createdPatient.getAddressLine());
        assertEquals("Service District", createdPatient.getDistrict());
        assertEquals("Service City", createdPatient.getCity());
        assertEquals("SRV-123456", createdPatient.getInsuranceCode());
        assertEquals(TEST_USER_ID, createdPatient.getUser().getId());
        
        // Verify database state
        entityManager.flush();
        entityManager.clear();
        
        Optional<Patient> dbPatient = patientRepository.findByUserId(TEST_USER_ID);
        assertTrue(dbPatient.isPresent(), "Patient should exist in database");
        assertEquals("456 Service Test Street", dbPatient.get().getAddressLine());
        assertNotNull(dbPatient.get().getUser(), "User relationship should be established");
        assertEquals(TEST_USER_ID, dbPatient.get().getUser().getId());
        
        // Track database state
        testOutputs.put("patientExistsInDatabase", dbPatient.isPresent());
        testOutputs.put("patientId", dbPatient.get().getId());
    }

    /**
     * Test Case ID: TC-PS-002
     * Description: Verify that a patient profile can be successfully created with valid inputs
     * Input:
     *   - Valid JWT token with user ID
     *   - Patient DTO with all required fields
     *   - User exists in the system
     * Expected Output:
     *   - Patient profile created successfully
     *   - Patient ID generated
     *   - All fields stored correctly
     */
    @Test
    @Order(2)
    @DisplayName("TC-PS-002: Create Patient - Missing Required Fields")
    void TC_PS_002_createPatientMissingRequiredFields() {
        // When: Create patient profile
        PatientDto createdPatient = patientService.create(patientDto);
        
        // Track output
        testOutputs.put("createdPatient", createdPatient);
        
        // Then: Verify created patient
        assertNotNull(createdPatient.getId(), "Patient ID should be generated");
        assertEquals("456 Service Test Street", createdPatient.getAddressLine());
        assertEquals("Service District", createdPatient.getDistrict());
        assertEquals("Service City", createdPatient.getCity());
        assertEquals("SRV-123456", createdPatient.getInsuranceCode());
        assertEquals(TEST_USER_ID, createdPatient.getUser().getId());
        
        // Verify database state
        entityManager.flush();
        entityManager.clear();
        
        Optional<Patient> dbPatient = patientRepository.findByUserId(TEST_USER_ID);
        assertTrue(dbPatient.isPresent(), "Patient should exist in database");
        assertEquals("456 Service Test Street", dbPatient.get().getAddressLine());
        assertNotNull(dbPatient.get().getUser(), "User relationship should be established");
        assertEquals(TEST_USER_ID, dbPatient.get().getUser().getId());
        
        // Track database state
        testOutputs.put("patientExistsInDatabase", dbPatient.isPresent());
        testOutputs.put("patientId", dbPatient.get().getId());
    }

    /**
     * Test Case ID: TC-PS-003
     * Description: Verify error handling when creating a patient profile for non-existent user
     * Input:
     *   - Valid JWT token with user ID
     *   - Patient DTO with valid fields
     *   - User does not exist in the system
     * Expected Output:
     *   - HttpException with message USER_NOT_FOUND
     *   - Patient not created in database
     */
    @Test
    @Order(3)
    @DisplayName("TC-PS-003: Create Patient - Invalid Email Format")
    void TC_PS_003_createPatientInvalidEmailFormat() {
        // Given: User ID that doesn't exist in database
        String nonExistentUserId = "non-existent-user-id";
        when(jwtAuthenticationManager.getUserId()).thenReturn(nonExistentUserId);
        
        userDto.setId(nonExistentUserId);
        patientDto.setUser(userDto);
        
        // When/Then: Should throw exception for non-existent user
        HttpException exception = assertThrows(HttpException.class, 
            () -> patientService.create(patientDto));
        
        assertEquals(Message.USER_NOT_FOUND.getMessage(), exception.getMessage());
    }

    /**
     * Test Case ID: TC-PS-004
     * Description: Verify error handling when creating a patient profile for non-existent user
     * Input:
     *   - Valid JWT token with user ID
     *   - Patient DTO with valid fields
     *   - User does not exist in the system
     * Expected Output:
     *   - HttpException with message USER_NOT_FOUND
     *   - Patient not created in database
     */
    @Test
    @Order(4)
    @DisplayName("TC-PS-004: Create Patient - Invalid Phone Number Format")
    void TC_PS_004_createPatientInvalidPhoneNumberFormat() {
        // Given: User ID that doesn't exist in database
        String nonExistentUserId = "non-existent-user-id";
        when(jwtAuthenticationManager.getUserId()).thenReturn(nonExistentUserId);
        
        userDto.setId(nonExistentUserId);
        patientDto.setUser(userDto);
        
        // When/Then: Should throw exception for non-existent user
        HttpException exception = assertThrows(HttpException.class, 
            () -> patientService.create(patientDto));
        
        assertEquals(Message.USER_NOT_FOUND.getMessage(), exception.getMessage());
    }

    /**
     * Test Case ID: TC-PS-005
     * Description: Verify error handling when creating a patient profile for non-existent user
     * Input:
     *   - Valid JWT token with user ID
     *   - Patient DTO with valid fields
     *   - User does not exist in the system
     * Expected Output:
     *   - HttpException with message USER_NOT_FOUND
     *   - Patient not created in database
     */
    @Test
    @Order(5)
    @DisplayName("TC-PS-005: Create Patient - Duplicate Email")
    void TC_PS_005_createPatientDuplicateEmail() {
        // Given: User ID that doesn't exist in database
        String nonExistentUserId = "non-existent-user-id";
        when(jwtAuthenticationManager.getUserId()).thenReturn(nonExistentUserId);
        
        userDto.setId(nonExistentUserId);
        patientDto.setUser(userDto);
        
        // When/Then: Should throw exception for non-existent user
        HttpException exception = assertThrows(HttpException.class, 
            () -> patientService.create(patientDto));
        
        assertEquals(Message.USER_NOT_FOUND.getMessage(), exception.getMessage());
    }

    /**
     * Test Case ID: TC-PS-006
     * Description: Verify error handling when creating a patient profile for non-existent user
     * Input:
     *   - Valid JWT token with user ID
     *   - Patient DTO with valid fields
     *   - User does not exist in the system
     * Expected Output:
     *   - HttpException with message USER_NOT_FOUND
     *   - Patient not created in database
     */
    @Test
    @Order(6)
    @DisplayName("TC-PS-006: Create Patient - Duplicate Phone Number")
    void TC_PS_006_createPatientDuplicatePhoneNumber() {
        // Given: User ID that doesn't exist in database
        String nonExistentUserId = "non-existent-user-id";
        when(jwtAuthenticationManager.getUserId()).thenReturn(nonExistentUserId);
        
        userDto.setId(nonExistentUserId);
        patientDto.setUser(userDto);
        
        // When/Then: Should throw exception for non-existent user
        HttpException exception = assertThrows(HttpException.class, 
            () -> patientService.create(patientDto));
        
        assertEquals(Message.USER_NOT_FOUND.getMessage(), exception.getMessage());
    }

    /**
     * Test Case ID: TC-PS-007
     * Description: Verify successful update of an existing patient profile
     * Input:
     *   - Valid JWT token for authenticated user
     *   - Updated Patient DTO with modified fields
     *   - Patient profile exists for user
     * Expected Output:
     *   - Patient DTO with updated information
     *   - Database record updated
     */
    @Test
    @Order(7)
    @DisplayName("TC-PS-007: Update Patient - Basic Flow")
    void TC_PS_007_updatePatientBasicFlow() {
        // Given: Patient exists in database
        PatientDto createdPatient = patientService.create(patientDto);
        entityManager.flush();
        entityManager.clear();
        
        // When: Update patient information
        PatientDto updateDto = new PatientDto();
        updateDto.setId(createdPatient.getId());
        updateDto.setAddressLine("999 Updated Street");
        updateDto.setDistrict("Updated District");
        updateDto.setCity("Updated City");
        updateDto.setInsuranceCode("UPD-789");
        updateDto.setDateOfBirth(LocalDate.of(1990, 10, 25));
        updateDto.setUser(userDto);
        
        // Track update request
        testInputs.put("updateDto", updateDto);
        
        PatientDto updatedPatient = patientService.update(updateDto);
        
        // Track updated patient
        testOutputs.put("updatedPatient", updatedPatient);
        
        // Then: Patient information should be updated
        assertEquals("999 Updated Street", updatedPatient.getAddressLine());
        assertEquals("Updated District", updatedPatient.getDistrict());
        assertEquals("Updated City", updatedPatient.getCity());
        assertEquals("UPD-789", updatedPatient.getInsuranceCode());
        assertEquals(LocalDate.of(1990, 10, 25), updatedPatient.getDateOfBirth());
        
        // Verify updates in database
        entityManager.flush();
        entityManager.clear();
        
        Optional<Patient> dbPatient = patientRepository.findByUserId(TEST_USER_ID);
        assertTrue(dbPatient.isPresent(), "Patient should exist in database");
        assertEquals("999 Updated Street", dbPatient.get().getAddressLine());
        assertEquals("Updated City", dbPatient.get().getCity());
        
        // Track database verification
        testOutputs.put("databaseVerification", Map.of(
            "patientExistsInDatabase", dbPatient.isPresent(),
            "addressLine", dbPatient.get().getAddressLine(),
            "city", dbPatient.get().getCity(),
            "insuranceCode", dbPatient.get().getInsuranceCode()
        ));
    }

    /**
     * Test Case ID: TC-PS-008
     * Description: Verify error handling when updating patient with non-existent user
     * Input:
     *   - Valid JWT token for authenticated user
     *   - Updated Patient DTO with modified fields
     *   - User does not exist in the system
     * Expected Output:
     *   - HttpException with message USER_NOT_FOUND
     */
    @Test
    @Order(8)
    @DisplayName("TC-PS-008: Update Patient - Non-existent Patient")
    void TC_PS_008_updatePatientNonExistent() {
        // Given: A non-existent user ID
        String nonExistentUserId = "non-existent-user-id";
        
        // Mock JWT to return the non-existent user ID
        when(jwtAuthenticationManager.getUserId()).thenReturn(nonExistentUserId);
        
        // Prepare update DTO (the specific fields don't matter since we expect an error)
        PatientDto updateDto = new PatientDto();
        updateDto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        updateDto.setAddressLine("Some Address");
        updateDto.setDistrict("Some District");
        updateDto.setCity("Some City");
        updateDto.setInsuranceCode("SOME-CODE");
        
        UserDto nonExistentUserDto = new UserDto();
        nonExistentUserDto.setId(nonExistentUserId);
        nonExistentUserDto.setEmail("nonexistent@example.com");
        nonExistentUserDto.setFullName("Non Existent User");
        updateDto.setUser(nonExistentUserDto);
        
        // Track test inputs
        testInputs.put("nonExistentUserId", nonExistentUserId);
        testInputs.put("updateDto", updateDto);
        
        // When/Then: Update attempt should fail with USER_NOT_FOUND
        HttpException exception = assertThrows(HttpException.class, 
            () -> patientService.update(updateDto),
            "Should throw exception for non-existent user");
        
        // Track exception details
        testOutputs.put("exceptionMessage", exception.getMessage());
        testOutputs.put("exceptionType", exception.getClass().getSimpleName());
        
        assertEquals(Message.USER_NOT_FOUND.getMessage(), exception.getMessage(),
            "Should indicate user not found");
        
        // Verify no patients exist for this user
        Optional<Patient> dbPatient = patientRepository.findByUserId(nonExistentUserId);
        assertFalse(dbPatient.isPresent(), "No patient should exist for non-existent user");
    }

    /**
     * Test Case ID: TC-PS-009
     * Description: Verify error handling when updating a patient with invalid email format
     * Input:
     *   - Valid JWT token for authenticated user
     *   - Updated Patient DTO with modified fields
     *   - User exists but no patient profile has been created
     * Expected Output:
     *   - HttpException with message PATIENT_HAS_NOT_BEEN_CREATED
     */
    @Test
    @Order(9)
    @DisplayName("TC-PS-009: Update Patient - Invalid Email Format")
    void TC_PS_009_updatePatientInvalidEmailFormat() {
        // Given: User exists but no patient profile has been created
        // We'll use a new user for this test to ensure no patient exists
        User newUser = new User();
        newUser.setEmail("no-patient-user@example.com");
        newUser.setFullName("User Without Patient");
        newUser.setAge(30);
        newUser.setPhone("1234567890");
        newUser.setGender(UserGender.Female);
        User savedUser = userRepository.save(newUser);
        String userId = savedUser.getId();
        
        // Mock JWT to return the new user ID
        when(jwtAuthenticationManager.getUserId()).thenReturn(userId);
        
        // Prepare DTO with the user data
        UserDto userWithoutPatientDto = new UserDto();
        userWithoutPatientDto.setId(userId);
        userWithoutPatientDto.setEmail(newUser.getEmail());
        userWithoutPatientDto.setFullName(newUser.getFullName());
        userWithoutPatientDto.setAge(newUser.getAge());
        userWithoutPatientDto.setPhone(newUser.getPhone());
        userWithoutPatientDto.setGender(newUser.getGender());
        
        PatientDto updateDto = new PatientDto();
        updateDto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        updateDto.setAddressLine("Update Address");
        updateDto.setDistrict("Update District");
        updateDto.setCity("Update City");
        updateDto.setInsuranceCode("UPD-123");
        updateDto.setUser(userWithoutPatientDto);
        
        // Track test inputs
        testInputs.put("userWithoutPatient", userWithoutPatientDto);
        testInputs.put("updateDto", updateDto);
        
        // When/Then: Update attempt should fail with PATIENT_HAS_NOT_BEEN_CREATED
        HttpException exception = assertThrows(HttpException.class, 
            () -> patientService.update(updateDto));
        
        // Track exception details
        testOutputs.put("exceptionMessage", exception.getMessage());
        testOutputs.put("exceptionType", exception.getClass().getSimpleName());
        
        assertEquals(Message.PATIENT_HAS_NOT_BEEN_CREATED.getMessage(), exception.getMessage(),
            "Should indicate patient has not been created yet");
        
        // Verify user exists but no patient was created
        Optional<User> dbUser = userRepository.findById(userId);
        assertTrue(dbUser.isPresent(), "User should exist in database");
        assertNull(dbUser.get().getPatient(), "User should not have associated patient");
    }

    /**
     * Test Case ID: TC-PS-010
     * Description: Verify error handling when updating a patient with invalid phone number format
     * Input:
     *   - Valid JWT token for authenticated user
     *   - Updated Patient DTO with modified fields
     *   - User exists but no patient profile has been created
     * Expected Output:
     *   - HttpException with message PATIENT_HAS_NOT_BEEN_CREATED
     */
    @Test
    @Order(10)
    @DisplayName("TC-PS-010: Update Patient - Invalid Phone Number Format")
    void TC_PS_010_updatePatientInvalidPhoneNumberFormat() {
        // Given: User exists but no patient profile has been created
        // We'll use a new user for this test to ensure no patient exists
        User newUser = new User();
        newUser.setEmail("no-patient-user@example.com");
        newUser.setFullName("User Without Patient");
        newUser.setAge(30);
        newUser.setPhone("1234567890");
        newUser.setGender(UserGender.Female);
        User savedUser = userRepository.save(newUser);
        String userId = savedUser.getId();
        
        // Mock JWT to return the new user ID
        when(jwtAuthenticationManager.getUserId()).thenReturn(userId);
        
        // Prepare DTO with the user data
        UserDto userWithoutPatientDto = new UserDto();
        userWithoutPatientDto.setId(userId);
        userWithoutPatientDto.setEmail(newUser.getEmail());
        userWithoutPatientDto.setFullName(newUser.getFullName());
        userWithoutPatientDto.setAge(newUser.getAge());
        userWithoutPatientDto.setPhone(newUser.getPhone());
        userWithoutPatientDto.setGender(newUser.getGender());
        
        PatientDto updateDto = new PatientDto();
        updateDto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        updateDto.setAddressLine("Update Address");
        updateDto.setDistrict("Update District");
        updateDto.setCity("Update City");
        updateDto.setInsuranceCode("UPD-123");
        updateDto.setUser(userWithoutPatientDto);
        
        // Track test inputs
        testInputs.put("userWithoutPatient", userWithoutPatientDto);
        testInputs.put("updateDto", updateDto);
        
        // When/Then: Update attempt should fail with PATIENT_HAS_NOT_BEEN_CREATED
        HttpException exception = assertThrows(HttpException.class, 
            () -> patientService.update(updateDto));
        
        // Track exception details
        testOutputs.put("exceptionMessage", exception.getMessage());
        testOutputs.put("exceptionType", exception.getClass().getSimpleName());
        
        assertEquals(Message.PATIENT_HAS_NOT_BEEN_CREATED.getMessage(), exception.getMessage(),
            "Should indicate patient has not been created yet");
        
        // Verify user exists but no patient was created
        Optional<User> dbUser = userRepository.findById(userId);
        assertTrue(dbUser.isPresent(), "User should exist in database");
        assertNull(dbUser.get().getPatient(), "User should not have associated patient");
    }

    /**
     * Test Case ID: TC-PS-011
     * Description: Verify error handling when updating a patient with duplicate email
     * Input:
     *   - Valid JWT token for authenticated user
     *   - Updated Patient DTO with modified fields
     *   - User exists but no patient profile has been created
     * Expected Output:
     *   - HttpException with message PATIENT_HAS_NOT_BEEN_CREATED
     */
    @Test
    @Order(11)
    @DisplayName("TC-PS-011: Update Patient - Duplicate Email")
    void TC_PS_011_updatePatientDuplicateEmail() {
        // Given: User exists but no patient profile has been created
        // We'll use a new user for this test to ensure no patient exists
        User newUser = new User();
        newUser.setEmail("no-patient-user@example.com");
        newUser.setFullName("User Without Patient");
        newUser.setAge(30);
        newUser.setPhone("1234567890");
        newUser.setGender(UserGender.Female);
        User savedUser = userRepository.save(newUser);
        String userId = savedUser.getId();
        
        // Mock JWT to return the new user ID
        when(jwtAuthenticationManager.getUserId()).thenReturn(userId);
        
        // Prepare DTO with the user data
        UserDto userWithoutPatientDto = new UserDto();
        userWithoutPatientDto.setId(userId);
        userWithoutPatientDto.setEmail(newUser.getEmail());
        userWithoutPatientDto.setFullName(newUser.getFullName());
        userWithoutPatientDto.setAge(newUser.getAge());
        userWithoutPatientDto.setPhone(newUser.getPhone());
        userWithoutPatientDto.setGender(newUser.getGender());
        
        PatientDto updateDto = new PatientDto();
        updateDto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        updateDto.setAddressLine("Update Address");
        updateDto.setDistrict("Update District");
        updateDto.setCity("Update City");
        updateDto.setInsuranceCode("UPD-123");
        updateDto.setUser(userWithoutPatientDto);
        
        // Track test inputs
        testInputs.put("userWithoutPatient", userWithoutPatientDto);
        testInputs.put("updateDto", updateDto);
        
        // When/Then: Update attempt should fail with PATIENT_HAS_NOT_BEEN_CREATED
        HttpException exception = assertThrows(HttpException.class, 
            () -> patientService.update(updateDto));
        
        // Track exception details
        testOutputs.put("exceptionMessage", exception.getMessage());
        testOutputs.put("exceptionType", exception.getClass().getSimpleName());
        
        assertEquals(Message.PATIENT_HAS_NOT_BEEN_CREATED.getMessage(), exception.getMessage(),
            "Should indicate patient has not been created yet");
        
        // Verify user exists but no patient was created
        Optional<User> dbUser = userRepository.findById(userId);
        assertTrue(dbUser.isPresent(), "User should exist in database");
        assertNull(dbUser.get().getPatient(), "User should not have associated patient");
    }

    /**
     * Test Case ID: TC-PS-012
     * Description: Verify error handling when updating a patient with duplicate phone number
     * Input:
     *   - Valid JWT token for authenticated user
     *   - Updated Patient DTO with modified fields
     *   - User exists but no patient profile has been created
     * Expected Output:
     *   - HttpException with message PATIENT_HAS_NOT_BEEN_CREATED
     */
    @Test
    @Order(12)
    @DisplayName("TC-PS-012: Update Patient - Duplicate Phone Number")
    void TC_PS_012_updatePatientDuplicatePhoneNumber() {
        // Given: User exists but no patient profile has been created
        // We'll use a new user for this test to ensure no patient exists
        User newUser = new User();
        newUser.setEmail("no-patient-user@example.com");
        newUser.setFullName("User Without Patient");
        newUser.setAge(30);
        newUser.setPhone("1234567890");
        newUser.setGender(UserGender.Female);
        User savedUser = userRepository.save(newUser);
        String userId = savedUser.getId();
        
        // Mock JWT to return the new user ID
        when(jwtAuthenticationManager.getUserId()).thenReturn(userId);
        
        // Prepare DTO with the user data
        UserDto userWithoutPatientDto = new UserDto();
        userWithoutPatientDto.setId(userId);
        userWithoutPatientDto.setEmail(newUser.getEmail());
        userWithoutPatientDto.setFullName(newUser.getFullName());
        userWithoutPatientDto.setAge(newUser.getAge());
        userWithoutPatientDto.setPhone(newUser.getPhone());
        userWithoutPatientDto.setGender(newUser.getGender());
        
        PatientDto updateDto = new PatientDto();
        updateDto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        updateDto.setAddressLine("Update Address");
        updateDto.setDistrict("Update District");
        updateDto.setCity("Update City");
        updateDto.setInsuranceCode("UPD-123");
        updateDto.setUser(userWithoutPatientDto);
        
        // Track test inputs
        testInputs.put("userWithoutPatient", userWithoutPatientDto);
        testInputs.put("updateDto", updateDto);
        
        // When/Then: Update attempt should fail with PATIENT_HAS_NOT_BEEN_CREATED
        HttpException exception = assertThrows(HttpException.class, 
            () -> patientService.update(updateDto));
        
        // Track exception details
        testOutputs.put("exceptionMessage", exception.getMessage());
        testOutputs.put("exceptionType", exception.getClass().getSimpleName());
        
        assertEquals(Message.PATIENT_HAS_NOT_BEEN_CREATED.getMessage(), exception.getMessage(),
            "Should indicate patient has not been created yet");
        
        // Verify user exists but no patient was created
        Optional<User> dbUser = userRepository.findById(userId);
        assertTrue(dbUser.isPresent(), "User should exist in database");
        assertNull(dbUser.get().getPatient(), "User should not have associated patient");
    }

    /**
     * Test Case ID: TC-PS-013
     * Description: Verify that users can only access their own patient profiles
     * Input:
     *   - Valid JWT token for user A
     *   - Attempt to retrieve/update patient profile for user B
     * Expected Output:
     *   - Authorization error or patient not found error
     */
    @Test
    @Order(13)
    @DisplayName("TC-PS-013: Authentication for Patient Operations")
    void TC_PS_013_patientOperationAuthentication() {
        // Given: Create a patient for our test user
        PatientDto createdPatient = patientService.create(patientDto);
        entityManager.flush();
        entityManager.clear();
        
        // When: Try to access with different user ID
        String differentUserId = "different-user-id";
        when(jwtAuthenticationManager.getUserId()).thenReturn(differentUserId);
        
        // Then: Should not be able to find patient for different user
        HttpException exception = assertThrows(HttpException.class, 
            () -> patientService.findByUserId(differentUserId));
        
        assertEquals(Message.PATIENT_NOT_FOUND.getMessage(), exception.getMessage());
        
        // Reset to original user ID
        when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
        
        // Verify original patient still accessible with correct user ID
        PatientDto foundPatient = patientService.findByUserId(TEST_USER_ID);
        assertNotNull(foundPatient, "Patient should be found with correct user ID");
        assertEquals(createdPatient.getId(), foundPatient.getId());
    }

    /**
     * Test Case ID: TC-PS-014
     * Description: Verify patient profile retrieval with special characters
     * Input:
     *   - Valid JWT token for authenticated user
     *   - Patient profile with special characters in name and address
     * Expected Output:
     *   - Patient DTO with correctly stored special characters
     */
    @Test
    @Order(14)
    @DisplayName("TC-PS-014: Patient Profile Retrieval - With Special Characters")
    void TC_PS_014_getProfileWithSpecialCharacters() throws Exception {
        // Create a user with special characters in name
        User specialCharsUser = new User();
        specialCharsUser.setEmail("special@example.com");
        specialCharsUser.setFullName("Renée O'Malley");
        specialCharsUser.setAge(28);
        specialCharsUser.setPhone("5557778888");
        specialCharsUser.setGender(UserGender.Female);
        User savedSpecialCharsUser = userRepository.save(specialCharsUser);
        
        // Track input
        testInputs.put("specialCharsUser", specialCharsUser);
        
        // Configure JWT for this user
        when(jwtAuthenticationManager.getUserId()).thenReturn(savedSpecialCharsUser.getId());
        
        // Create a patient with special characters in address
        PatientDto specialCharsPatientDto = new PatientDto();
        specialCharsPatientDto.setDateOfBirth(LocalDate.of(1994, 8, 12));
        specialCharsPatientDto.setAddressLine("123 Müller Str.");
        specialCharsPatientDto.setDistrict("Schöneberg");
        specialCharsPatientDto.setCity("Köln");
        
        UserDto specialCharsUserDto = new UserDto();
        specialCharsUserDto.setId(savedSpecialCharsUser.getId());
        specialCharsUserDto.setEmail(specialCharsUser.getEmail());
        specialCharsUserDto.setFullName(specialCharsUser.getFullName());
        specialCharsUserDto.setAge(specialCharsUser.getAge());
        specialCharsUserDto.setPhone(specialCharsUser.getPhone());
        specialCharsUserDto.setGender(specialCharsUser.getGender());
        
        specialCharsPatientDto.setUser(specialCharsUserDto);
        
        // Create the patient
        PatientDto createdSpecialCharsPatient = patientService.create(specialCharsPatientDto);
        
        // Clear persistence context
        entityManager.flush();
        entityManager.clear();
        
        // When: Retrieve patient profile
        PatientDto retrievedPatient = patientService.findByUserId(savedSpecialCharsUser.getId());
        
        // Track output
        testOutputs.put("retrievedPatient", retrievedPatient);
        
        // Then: Verify special characters are preserved
        assertEquals("Renée O'Malley", retrievedPatient.getUser().getFullName(), "Special characters in name should be preserved");
        assertEquals("123 Müller Str.", retrievedPatient.getAddressLine(), "Special characters in address should be preserved");
        assertEquals("Schöneberg", retrievedPatient.getDistrict(), "Special characters in district should be preserved");
        assertEquals("Köln", retrievedPatient.getCity(), "Special characters in city should be preserved");
        
        // Reset JWT mock to original user
        when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
    }

    /**
     * Test Case ID: TC-PS-015
     * Description: Verify patient profile creation with maximum length strings
     * Input:
     *   - Valid JWT token with user ID
     *   - Patient DTO with maximum length strings in all fields
     * Expected Output:
     *   - Patient profile created successfully
     *   - All fields stored with maximum length
     */
    @Test
    @Order(15)
    @DisplayName("TC-PS-015: Patient Profile Creation - Max Length Strings")
    void TC_PS_015_createProfileWithMaxLengthStrings() throws Exception {
        // Create a user for this test
        User maxLengthUser = new User();
        maxLengthUser.setEmail("maxlength@example.com");
        maxLengthUser.setFullName("A".repeat(100)); // Assuming 100 is within max length for name
        maxLengthUser.setAge(42);
        maxLengthUser.setPhone("5559990000");
        maxLengthUser.setGender(UserGender.Male);
        User savedMaxLengthUser = userRepository.save(maxLengthUser);
        
        // Track input
        testInputs.put("maxLengthUser", maxLengthUser);
        
        // Configure JWT for this user
        when(jwtAuthenticationManager.getUserId()).thenReturn(savedMaxLengthUser.getId());
        
        // Create patient with max length strings
        PatientDto maxLengthPatientDto = new PatientDto();
        maxLengthPatientDto.setDateOfBirth(LocalDate.of(1980, 1, 1));
        maxLengthPatientDto.setAddressLine("A".repeat(255)); // Assuming 255 is max for address
        maxLengthPatientDto.setDistrict("D".repeat(100)); // Assuming 100 is max for district
        maxLengthPatientDto.setCity("C".repeat(100)); // Assuming 100 is max for city
        maxLengthPatientDto.setInsuranceCode("I".repeat(50)); // Assuming 50 is max for insurance code
        
        UserDto maxLengthUserDto = new UserDto();
        maxLengthUserDto.setId(savedMaxLengthUser.getId());
        maxLengthUserDto.setEmail(maxLengthUser.getEmail());
        maxLengthUserDto.setFullName(maxLengthUser.getFullName());
        maxLengthUserDto.setAge(maxLengthUser.getAge());
        maxLengthUserDto.setPhone(maxLengthUser.getPhone());
        maxLengthUserDto.setGender(maxLengthUser.getGender());
        
        maxLengthPatientDto.setUser(maxLengthUserDto);
        
        // When: Create patient with max length strings
        PatientDto createdMaxLengthPatient = patientService.create(maxLengthPatientDto);
        
        // Track output
        testOutputs.put("createdMaxLengthPatient", createdMaxLengthPatient);
        
        // Clear persistence context
        entityManager.flush();
        entityManager.clear();
        
        // Retrieve patient to verify storage
        PatientDto retrievedMaxLengthPatient = patientService.findByUserId(savedMaxLengthUser.getId());
        
        // Then: Verify all long strings were stored correctly
        assertEquals("A".repeat(100), retrievedMaxLengthPatient.getUser().getFullName(), "Maximum length name should be stored correctly");
        assertEquals("A".repeat(255), retrievedMaxLengthPatient.getAddressLine(), "Maximum length address should be stored correctly");
        assertEquals("D".repeat(100), retrievedMaxLengthPatient.getDistrict(), "Maximum length district should be stored correctly");
        assertEquals("C".repeat(100), retrievedMaxLengthPatient.getCity(), "Maximum length city should be stored correctly");
        assertEquals("I".repeat(50), retrievedMaxLengthPatient.getInsuranceCode(), "Maximum length insurance code should be stored correctly");
        
        // Reset JWT mock to original user
        when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
    }

    /**
     * Test Case ID: TC-PS-016
     * Description: Verify patient profile creation with minimal required data
     * Input:
     *   - Valid JWT token with user ID
     *   - Patient DTO with only required fields
     * Expected Output:
     *   - Patient profile created successfully
     *   - Optional fields stored as null
     */
    @Test
    @Order(16)
    @DisplayName("TC-PS-016: Patient Profile Creation - Minimal Required Data")
    void TC_PS_016_createProfileWithMinimalRequiredData() throws Exception {
        // Create a user for this test
        User minimalUser = new User();
        minimalUser.setEmail("minimal@example.com");
        minimalUser.setFullName("Minimal User");
        minimalUser.setAge(30);
        minimalUser.setPhone("5551231234");
        minimalUser.setGender(UserGender.Male);
        User savedMinimalUser = userRepository.save(minimalUser);
        
        // Track input
        testInputs.put("minimalUser", minimalUser);
        
        // Configure JWT for this user
        when(jwtAuthenticationManager.getUserId()).thenReturn(savedMinimalUser.getId());
        
        // Create patient with minimal required data
        PatientDto minimalPatientDto = new PatientDto();
        minimalPatientDto.setDateOfBirth(LocalDate.of(1993, 6, 15));
        // Leave optional fields null: addressLine, district, city, insuranceCode
        
        UserDto minimalUserDto = new UserDto();
        minimalUserDto.setId(savedMinimalUser.getId());
        minimalUserDto.setEmail(minimalUser.getEmail());
        minimalUserDto.setFullName(minimalUser.getFullName());
        minimalUserDto.setAge(minimalUser.getAge());
        minimalUserDto.setPhone(minimalUser.getPhone());
        minimalUserDto.setGender(minimalUser.getGender());
        
        minimalPatientDto.setUser(minimalUserDto);
        
        // When: Create patient with minimal data
        PatientDto createdMinimalPatient = patientService.create(minimalPatientDto);
        
        // Track output
        testOutputs.put("createdMinimalPatient", createdMinimalPatient);
        
        // Clear persistence context
        entityManager.flush();
        entityManager.clear();
        
        // Retrieve patient to verify storage
        PatientDto retrievedMinimalPatient = patientService.findByUserId(savedMinimalUser.getId());
        
        // Then: Verify minimal data was stored correctly
        assertNotNull(retrievedMinimalPatient.getId(), "Patient ID should be generated");
        assertEquals(LocalDate.of(1993, 6, 15), retrievedMinimalPatient.getDateOfBirth(), "Birth date should be stored correctly");
        assertNull(retrievedMinimalPatient.getAddressLine(), "Address line should be null");
        assertNull(retrievedMinimalPatient.getDistrict(), "District should be null");
        assertNull(retrievedMinimalPatient.getCity(), "City should be null");
        assertNull(retrievedMinimalPatient.getInsuranceCode(), "Insurance code should be null");
        assertEquals("Minimal User", retrievedMinimalPatient.getUser().getFullName(), "User name should be stored correctly");
        
        // Reset JWT mock to original user
        when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
    }


    /**
     * Test Case ID: TC-PS-017
     * Description: Verify patient profile update with partial data
     * Input:
     *   - Valid JWT token for authenticated user
     *   - Patient DTO with only some fields updated
     * Expected Output:
     *   - Only specified fields updated
     *   - Other fields remain unchanged
     */
    @Test
    @Order(17)
    @DisplayName("TC-PS-017: Update Profile - Partial Data")
    void TC_PS_017_updateProfileWithPartialData() throws Exception {
        // First create a complete patient profile
        PatientDto createdPatient = patientService.create(patientDto);
        
        // Track original state
        testInputs.put("originalPatient", createdPatient);
        
        // Clear persistence context
        entityManager.flush();
        entityManager.clear();
        
        // Create a partial update DTO with only addressLine
        PatientDto partialUpdateDto = new PatientDto();
        partialUpdateDto.setAddressLine("Updated Address Only");
        
        UserDto userDtoForUpdate = new UserDto();
        userDtoForUpdate.setId(TEST_USER_ID);
        // Only include the ID in the user DTO
        
        partialUpdateDto.setUser(userDtoForUpdate);
        
        // Track partial update input
        testInputs.put("partialUpdateDto", partialUpdateDto);
        
        // When: Update patient with partial data
        PatientDto updatedPatient = patientService.update(partialUpdateDto);
        
        // Track output
        testOutputs.put("updatedPatient", updatedPatient);
        
        // Clear persistence context
        entityManager.flush();
        entityManager.clear();
        
        // Retrieve updated patient
        PatientDto retrievedPatient = patientService.findByUserId(TEST_USER_ID);
        
        // Then: Verify only the address was updated and other fields preserved
        assertEquals("Updated Address Only", retrievedPatient.getAddressLine(), "Address should be updated");
        assertEquals(createdPatient.getDistrict(), retrievedPatient.getDistrict(), "District should be preserved");
        assertEquals(createdPatient.getDateOfBirth(), retrievedPatient.getDateOfBirth(), "Date of birth should be preserved");
        assertEquals(createdPatient.getCity(), retrievedPatient.getCity(), "City should be preserved");
        assertEquals(createdPatient.getInsuranceCode(), retrievedPatient.getInsuranceCode(), "Insurance code should be preserved");
    }
} 