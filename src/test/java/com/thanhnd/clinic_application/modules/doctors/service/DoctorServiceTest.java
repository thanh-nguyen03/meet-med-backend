package com.thanhnd.clinic_application.modules.doctors.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.constants.Role;
import com.thanhnd.clinic_application.constants.UserGender;
import com.thanhnd.clinic_application.entity.Department;
import com.thanhnd.clinic_application.entity.Doctor;
import com.thanhnd.clinic_application.entity.DoctorShiftPriceByDegree;
import com.thanhnd.clinic_application.entity.DoctorShiftPriceByExperience;
import com.thanhnd.clinic_application.entity.User;
import com.thanhnd.clinic_application.modules.departments.repository.DepartmentRepository;
import com.thanhnd.clinic_application.modules.doctors.dto.CreateDoctorDto;
import com.thanhnd.clinic_application.modules.doctors.dto.DoctorDto;
import com.thanhnd.clinic_application.modules.doctors.dto.UpdateDoctorDto;
import com.thanhnd.clinic_application.modules.doctors.repository.DoctorRepository;
import com.thanhnd.clinic_application.modules.doctors.repository.DoctorShiftPriceByDegreeRepository;
import com.thanhnd.clinic_application.modules.doctors.repository.DoctorShiftPriceByExperienceRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

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
@DisplayName("Doctor Service Tests")
@Tag("service")
public class DoctorServiceTest {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DoctorShiftPriceByDegreeRepository doctorShiftPriceByDegreeRepository;

    @Autowired
    private DoctorShiftPriceByExperienceRepository doctorShiftPriceByExperienceRepository;

    @MockBean
    private JwtAuthenticationManager jwtAuthenticationManager;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private ObjectMapper objectMapper;

    private String TEST_USER_ID;
    private String TEST_DOCTOR_ID;
    private String TEST_DEPARTMENT_ID;
    private UserDto userDto;
    private DoctorDto doctorDto;
    private CreateDoctorDto createDoctorDto;
    private User testUser;
    private Department testDepartment;
    private Doctor testDoctor;
    
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
        
        // Create a test department in the database
        testDepartment = new Department();
        testDepartment.setName("Test Department");
        testDepartment.setDescription("Test Department Description");
        testDepartment.setImageUrl("http://example.com/image.jpg");
        testDepartment = departmentRepository.save(testDepartment);
        TEST_DEPARTMENT_ID = testDepartment.getId();
        
        // Create a test user in the database
        testUser = new User();
        testUser.setEmail("doctor-test@example.com");
        testUser.setFullName("Doctor Test User");
        testUser.setAge(35);
        testUser.setPhone("5551234567");
        testUser.setGender(UserGender.Male);
        testUser.setRole(Role.Doctor);
        User savedUser = userRepository.save(testUser);
        TEST_USER_ID = savedUser.getId();
        
        // Create a test doctor in the database
        testDoctor = new Doctor();
        testDoctor.setYearsOfExperience(10);
        testDoctor.setDegree("MD");
        testDoctor.setNumberOfPatients(100);
        testDoctor.setNumberOfCertificates(5);
        testDoctor.setDescription("Test doctor description");
        testDoctor.setUser(savedUser);
        testDoctor.setDepartment(testDepartment);
        testDoctor = doctorRepository.save(testDoctor);
        TEST_DOCTOR_ID = testDoctor.getId();
        
        // Configure JWT authentication to return our test user ID
        when(jwtAuthenticationManager.getUserId()).thenReturn(TEST_USER_ID);
        
        // Prepare DTOs for testing
        userDto = new UserDto();
        userDto.setId(TEST_USER_ID);
        userDto.setEmail("doctor-test@example.com");
        userDto.setFullName("Doctor Test User");
        userDto.setAge(35);
        userDto.setPhone("5551234567");
        userDto.setGender(UserGender.Male);

        doctorDto = new DoctorDto();
        doctorDto.setId(TEST_DOCTOR_ID);
        doctorDto.setYearsOfExperience(10);
        doctorDto.setDegree("MD");
        doctorDto.setNumberOfPatients(100);
        doctorDto.setNumberOfCertificates(5);
        doctorDto.setDescription("Test doctor description");
        doctorDto.setUser(userDto);
        
        createDoctorDto = new CreateDoctorDto();
        createDoctorDto.setEmail("new-doctor@example.com");
        createDoctorDto.setPassword("password123");
        createDoctorDto.setFullName("New Doctor");
        createDoctorDto.setGender(UserGender.Male);
        createDoctorDto.setYearsOfExperience(5);
        createDoctorDto.setDegree("MD");
        createDoctorDto.setDescription("New doctor description");
        createDoctorDto.setDepartmentId(TEST_DEPARTMENT_ID);
        
        // Track standard inputs
        testInputs.put("userId", TEST_USER_ID);
        testInputs.put("doctorId", TEST_DOCTOR_ID);
        testInputs.put("departmentId", TEST_DEPARTMENT_ID);
    }

    /**
     * Test Case 1: Doctor Retrieval by ID - Success
     * Goal: Verify successful retrieval of a doctor by ID
     * Input: Valid doctor ID
     * Expected Output: DoctorDto with matching ID
     * Notes: Should return complete doctor information including user and department details
     */
    @Test
    @Order(1)
    @DisplayName("TC-DS-001: Doctor Retrieval by ID - Success")
    void testFindByIdSuccess() {
        // When: Retrieve doctor by ID
        DoctorDto foundDoctor = doctorService.findById(TEST_DOCTOR_ID);
        
        // Track output
        testOutputs.put("foundDoctor", foundDoctor);
        
        // Then: Verify found doctor
        assertNotNull(foundDoctor, "Doctor should be found");
        assertEquals(TEST_DOCTOR_ID, foundDoctor.getId());
        assertEquals(10, foundDoctor.getYearsOfExperience());
        assertEquals("MD", foundDoctor.getDegree());
        assertEquals(100, foundDoctor.getNumberOfPatients());
        assertEquals(5, foundDoctor.getNumberOfCertificates());
        assertEquals("Test doctor description", foundDoctor.getDescription());
        
        // Verify user information
        assertNotNull(foundDoctor.getUser(), "User details should be included");
        assertEquals(TEST_USER_ID, foundDoctor.getUser().getId());
        assertEquals("doctor-test@example.com", foundDoctor.getUser().getEmail());
        assertEquals("Doctor Test User", foundDoctor.getUser().getFullName());
    }

    /**
     * Test Case 2: Doctor Retrieval by ID - Not Found
     * Goal: Verify proper error handling when doctor ID doesn't exist
     * Input: Non-existent doctor ID
     * Expected Output: HttpException with NOT_FOUND status and "Doctor not found" message
     * Notes: Should use Message.DOCTOR_NOT_FOUND constant
     */
    @Test
    @Order(2)
    @DisplayName("TC-DS-002: Doctor Retrieval by ID - Not Found")
    void testFindByIdNotFound() {
        // Given: Non-existent doctor ID
        String nonExistentDoctorId = "non-existent-doctor-id";
        
        // Track input
        testInputs.put("nonExistentDoctorId", nonExistentDoctorId);
        
        // When/Then: Should throw exception for non-existent doctor
        HttpException exception = assertThrows(HttpException.class, 
            () -> doctorService.findById(nonExistentDoctorId));
        
        // Track exception
        testOutputs.put("exceptionMessage", exception.getMessage());
        testOutputs.put("exceptionType", exception.getClass().getSimpleName());
        
        assertEquals(Message.DOCTOR_NOT_FOUND.getMessage(), exception.getMessage());
    }

    /**
     * Test Case 3: Doctor Retrieval by User ID - Success
     * Goal: Verify successful retrieval of a doctor by user ID
     * Input: Valid user ID
     * Expected Output: DoctorDto with matching user ID
     * Notes: Should return complete doctor information including user and department details
     */
    @Test
    @Order(3)
    @DisplayName("TC-DS-003: Doctor Retrieval by User ID - Success")
    void testFindByUserIdSuccess() {
        // When: Retrieve doctor by user ID
        DoctorDto foundDoctor = doctorService.findByUserId(TEST_USER_ID);
        
        // Track output
        testOutputs.put("foundDoctor", foundDoctor);
        
        // Then: Verify found doctor
        assertNotNull(foundDoctor, "Doctor should be found");
        assertEquals(TEST_DOCTOR_ID, foundDoctor.getId());
        assertEquals(10, foundDoctor.getYearsOfExperience());
        assertEquals("MD", foundDoctor.getDegree());
        assertEquals(100, foundDoctor.getNumberOfPatients());
        assertEquals(5, foundDoctor.getNumberOfCertificates());
        assertEquals("Test doctor description", foundDoctor.getDescription());
        
        // Verify user information
        assertNotNull(foundDoctor.getUser(), "User details should be included");
        assertEquals(TEST_USER_ID, foundDoctor.getUser().getId());
        assertEquals("doctor-test@example.com", foundDoctor.getUser().getEmail());
        assertEquals("Doctor Test User", foundDoctor.getUser().getFullName());
    }

    /**
     * Test Case 4: Doctor Retrieval by User ID - Not Found
     * Goal: Verify proper error handling when user ID doesn't exist
     * Input: Non-existent user ID
     * Expected Output: HttpException with NOT_FOUND status and "Doctor not found" message
     * Notes: Should use Message.DOCTOR_NOT_FOUND constant
     */
    @Test
    @Order(4)
    @DisplayName("TC-DS-004: Doctor Retrieval by User ID - Not Found")
    void testFindByUserIdNotFound() {
        // Given: Non-existent user ID
        String nonExistentUserId = "non-existent-user-id";
        
        // Track input
        testInputs.put("nonExistentUserId", nonExistentUserId);
        
        // When/Then: Should throw exception for non-existent user
        HttpException exception = assertThrows(HttpException.class, 
            () -> doctorService.findByUserId(nonExistentUserId));
        
        // Track exception
        testOutputs.put("exceptionMessage", exception.getMessage());
        testOutputs.put("exceptionType", exception.getClass().getSimpleName());
        
        assertEquals(Message.DOCTOR_NOT_FOUND.getMessage(), exception.getMessage());
    }

    /**
     * Test Case 5: Doctor Creation - Success
     * Goal: Verify successful creation of a new doctor
     * Input: Valid CreateDoctorDto with all required fields
     * Expected Output: DoctorDto with created doctor information
     * Notes: Should create associated User entity with Role.Doctor and set proper department association
     */
    @Test
    @Order(5)
    @DisplayName("TC-DS-005: Doctor Creation - Success")
    void testCreateDoctorSuccess() {
        // When: Create doctor
        DoctorDto createdDoctor = doctorService.create(createDoctorDto);
        
        // Track output
        testOutputs.put("createdDoctor", createdDoctor);
        
        // Then: Verify created doctor
        assertNotNull(createdDoctor.getId(), "Doctor ID should be generated");
        assertEquals(5, createdDoctor.getYearsOfExperience());
        assertEquals("MD", createdDoctor.getDegree());
        assertEquals("New doctor description", createdDoctor.getDescription());
        
        // Verify user information
        assertNotNull(createdDoctor.getUser(), "User details should be included");
        assertEquals("new-doctor@example.com", createdDoctor.getUser().getEmail());
        assertEquals("New Doctor", createdDoctor.getUser().getFullName());
        assertEquals(UserGender.Male, createdDoctor.getUser().getGender());
        
        // Verify database state
        entityManager.flush();
        entityManager.clear();
        
        Optional<Doctor> dbDoctor = doctorRepository.findByUserId(createdDoctor.getUser().getId());
        assertTrue(dbDoctor.isPresent(), "Doctor should exist in database");
        assertEquals(5, dbDoctor.get().getYearsOfExperience());
        assertEquals("MD", dbDoctor.get().getDegree());
        assertEquals("New doctor description", dbDoctor.get().getDescription());
        assertEquals(TEST_DEPARTMENT_ID, dbDoctor.get().getDepartment().getId());
        
        // Verify user role
        Optional<User> dbUser = userRepository.findById(createdDoctor.getUser().getId());
        assertTrue(dbUser.isPresent(), "User should exist in database");
        assertEquals(Role.Doctor, dbUser.get().getRole());
    }

    /**
     * Test Case 6: Doctor Creation - Email Already Exists
     * Goal: Verify proper error handling when email already exists
     * Input: CreateDoctorDto with existing email
     * Expected Output: HttpException with BAD_REQUEST status and "User with email {email} already exists" message
     * Notes: Should use Message.USER_EMAIL_ALREADY_EXISTS constant
     */
    @Test
    @Order(6)
    @DisplayName("TC-DS-006: Doctor Creation - Email Already Exists")
    void testCreateDoctorEmailAlreadyExists() {
        // Given: CreateDoctorDto with existing email
        createDoctorDto.setEmail("doctor-test@example.com"); // Use existing email
        
        // Track input
        testInputs.put("createDoctorDto", createDoctorDto);
        
        // When/Then: Should throw exception for existing email
        HttpException exception = assertThrows(HttpException.class, 
            () -> doctorService.create(createDoctorDto));
        
        // Track exception
        testOutputs.put("exceptionMessage", exception.getMessage());
        testOutputs.put("exceptionType", exception.getClass().getSimpleName());
        
        assertEquals(Message.USER_EMAIL_ALREADY_EXISTS.getMessage("doctor-test@example.com"), exception.getMessage());
    }

    /**
     * Test Case 7: Doctor Creation - Department Not Found
     * Goal: Verify proper error handling when department doesn't exist
     * Input: CreateDoctorDto with non-existent department ID
     * Expected Output: HttpException with NOT_FOUND status and "Department not found" message
     * Notes: Should use Message.DEPARTMENT_NOT_FOUND constant
     */
    @Test
    @Order(7)
    @DisplayName("TC-DS-007: Doctor Creation - Department Not Found")
    void testCreateDoctorDepartmentNotFound() {
        // Given: CreateDoctorDto with non-existent department ID
        createDoctorDto.setDepartmentId("non-existent-department-id");
        
        // Track input
        testInputs.put("createDoctorDto", createDoctorDto);
        
        // When/Then: Should throw exception for non-existent department
        HttpException exception = assertThrows(HttpException.class, 
            () -> doctorService.create(createDoctorDto));
        
        // Track exception
        testOutputs.put("exceptionMessage", exception.getMessage());
        testOutputs.put("exceptionType", exception.getClass().getSimpleName());
        
        assertEquals(Message.DEPARTMENT_NOT_FOUND.getMessage(), exception.getMessage());
    }

    /**
     * Test Case 8: Doctor Update - Success
     * Goal: Verify successful update of doctor information
     * Input: Valid doctor ID and UpdateDoctorDto
     * Expected Output: Updated DoctorDto
     * Notes: Should update both doctor and associated user information
     */
    @Test
    @Order(8)
    @DisplayName("TC-DS-008: Doctor Update - Success")
    void testUpdateDoctorSuccess() {
        // Given: UpdateDoctorDto with modified fields
        UpdateDoctorDto updateDoctorDto = new UpdateDoctorDto();
        updateDoctorDto.setYearsOfExperience(15);
        updateDoctorDto.setDegree("PhD");
        updateDoctorDto.setNumberOfPatients(150);
        updateDoctorDto.setNumberOfCertificates(8);
        updateDoctorDto.setDescription("Updated doctor description");
        
        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setId(TEST_USER_ID);
        updatedUserDto.setFullName("Updated Doctor Name");
        updatedUserDto.setGender(UserGender.Female);
        updatedUserDto.setPhone("5559876543");
        updatedUserDto.setAge(40);
        updatedUserDto.setImageUrl("http://example.com/updated-image.jpg");
        
        updateDoctorDto.setUser(updatedUserDto);
        
        // Track input
        testInputs.put("updateDoctorDto", updateDoctorDto);
        
        // When: Update doctor
        DoctorDto updatedDoctor = doctorService.update(TEST_DOCTOR_ID, updateDoctorDto);
        
        // Track output
        testOutputs.put("updatedDoctor", updatedDoctor);
        
        // Then: Verify updated doctor
        assertEquals(15, updatedDoctor.getYearsOfExperience());
        assertEquals("PhD", updatedDoctor.getDegree());
        assertEquals(150, updatedDoctor.getNumberOfPatients());
        assertEquals(8, updatedDoctor.getNumberOfCertificates());
        assertEquals("Updated doctor description", updatedDoctor.getDescription());
        
        // Verify user information
        assertNotNull(updatedDoctor.getUser(), "User details should be included");
        assertEquals("Updated Doctor Name", updatedDoctor.getUser().getFullName());
        assertEquals(UserGender.Female, updatedDoctor.getUser().getGender());
        assertEquals("5559876543", updatedDoctor.getUser().getPhone());
        assertEquals(40, updatedDoctor.getUser().getAge());
        assertEquals("http://example.com/updated-image.jpg", updatedDoctor.getUser().getImageUrl());
        
        // Verify database state
        entityManager.flush();
        entityManager.clear();
        
        Optional<Doctor> dbDoctor = doctorRepository.findById(TEST_DOCTOR_ID);
        assertTrue(dbDoctor.isPresent(), "Doctor should exist in database");
        assertEquals(15, dbDoctor.get().getYearsOfExperience());
        assertEquals("PhD", dbDoctor.get().getDegree());
        assertEquals(150, dbDoctor.get().getNumberOfPatients());
        assertEquals(8, dbDoctor.get().getNumberOfCertificates());
        assertEquals("Updated doctor description", dbDoctor.get().getDescription());
        
        Optional<User> dbUser = userRepository.findById(TEST_USER_ID);
        assertTrue(dbUser.isPresent(), "User should exist in database");
        assertEquals("Updated Doctor Name", dbUser.get().getFullName());
        assertEquals(UserGender.Female, dbUser.get().getGender());
        assertEquals("5559876543", dbUser.get().getPhone());
        assertEquals(40, dbUser.get().getAge());
        assertEquals("http://example.com/updated-image.jpg", dbUser.get().getImageUrl());
    }

    /**
     * Test Case 9: Doctor Update - Doctor Not Found
     * Goal: Verify proper error handling when doctor doesn't exist
     * Input: Non-existent doctor ID
     * Expected Output: HttpException with NOT_FOUND status and "Doctor not found" message
     * Notes: Should use Message.DOCTOR_NOT_FOUND constant
     */
    @Test
    @Order(9)
    @DisplayName("TC-DS-009: Doctor Update - Doctor Not Found")
    void testUpdateDoctorNotFound() {
        // Given: Non-existent doctor ID
        String nonExistentDoctorId = "non-existent-doctor-id";
        
        // Given: UpdateDoctorDto with valid fields
        UpdateDoctorDto updateDoctorDto = new UpdateDoctorDto();
        updateDoctorDto.setYearsOfExperience(15);
        updateDoctorDto.setDegree("PhD");
        updateDoctorDto.setNumberOfPatients(150);
        updateDoctorDto.setNumberOfCertificates(8);
        updateDoctorDto.setDescription("Updated doctor description");
        
        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setId(TEST_USER_ID);
        updatedUserDto.setFullName("Updated Doctor Name");
        
        updateDoctorDto.setUser(updatedUserDto);
        
        // Track input
        testInputs.put("nonExistentDoctorId", nonExistentDoctorId);
        testInputs.put("updateDoctorDto", updateDoctorDto);
        
        // When/Then: Should throw exception for non-existent doctor
        HttpException exception = assertThrows(HttpException.class, 
            () -> doctorService.update(nonExistentDoctorId, updateDoctorDto));
        
        // Track exception
        testOutputs.put("exceptionMessage", exception.getMessage());
        testOutputs.put("exceptionType", exception.getClass().getSimpleName());
        
        assertEquals(Message.DOCTOR_NOT_FOUND.getMessage(), exception.getMessage());
    }

    /**
     * Test Case 10: Doctor Deletion - Success
     * Goal: Verify successful deletion of a doctor
     * Input: Valid doctor ID
     * Expected Output: No exception, doctor should be deleted
     * Notes: Should also delete associated user entity due to cascade
     */
    @Test
    @Order(10)
    @DisplayName("TC-DS-010: Doctor Deletion - Success")
    void testDeleteDoctorSuccess() {
        // When: Delete doctor
        doctorService.delete(TEST_DOCTOR_ID);
        
        // Then: Doctor should be deleted
        entityManager.flush();
        entityManager.clear();
        
        Optional<Doctor> dbDoctor = doctorRepository.findById(TEST_DOCTOR_ID);
        assertFalse(dbDoctor.isPresent(), "Doctor should be deleted from database");
        
        Optional<User> dbUser = userRepository.findById(TEST_USER_ID);
        assertFalse(dbUser.isPresent(), "User should be deleted from database due to cascade");
    }

    /**
     * Test Case 11: Doctor Deletion - Doctor Not Found
     * Goal: Verify proper error handling when doctor doesn't exist
     * Input: Non-existent doctor ID
     * Expected Output: HttpException with NOT_FOUND status and "Doctor not found" message
     * Notes: Should use Message.DOCTOR_NOT_FOUND constant
     */
    @Test
    @Order(11)
    @DisplayName("TC-DS-011: Doctor Deletion - Doctor Not Found")
    void testDeleteDoctorNotFound() {
        // Given: Non-existent doctor ID
        String nonExistentDoctorId = "non-existent-doctor-id";
        
        // Track input
        testInputs.put("nonExistentDoctorId", nonExistentDoctorId);
        
        // When/Then: Should throw exception for non-existent doctor
        HttpException exception = assertThrows(HttpException.class, 
            () -> doctorService.delete(nonExistentDoctorId));
        
        // Track exception
        testOutputs.put("exceptionMessage", exception.getMessage());
        testOutputs.put("exceptionType", exception.getClass().getSimpleName());
        
        assertEquals(Message.DOCTOR_NOT_FOUND.getMessage(), exception.getMessage());
    }

    /**
     * Test Case 12: Calculate Doctor Shift Price - Success
     * Goal: Verify correct calculation of doctor's shift price
     * Input: Doctor with degree and years of experience
     * Expected Output: Calculated price based on degree and experience
     * Notes: Should consider both degree-based and experience-based pricing
     */
    @Test
    @Order(12)
    @DisplayName("TC-DS-012: Calculate Doctor Shift Price - Success")
    void testCalculateDoctorShiftPriceSuccess() {
        // Given: Doctor with degree and years of experience
        Doctor doctor = doctorRepository.findById(TEST_DOCTOR_ID).orElseThrow();
        
        // Given: DoctorShiftPriceByDegree and DoctorShiftPriceByExperience records
        DoctorShiftPriceByDegree degreePrice = new DoctorShiftPriceByDegree();
        degreePrice.setDegree("MD");
        degreePrice.setBasePrice(100.0);
        degreePrice = doctorShiftPriceByDegreeRepository.save(degreePrice);
        
        DoctorShiftPriceByExperience experiencePrice = new DoctorShiftPriceByExperience();
        experiencePrice.setExperience(10);
        experiencePrice.setMultiplier(1.5);
        experiencePrice = doctorShiftPriceByExperienceRepository.save(experiencePrice);
        
        // Track input
        testInputs.put("doctor", doctor);
        testInputs.put("degreePrice", degreePrice);
        testInputs.put("experiencePrice", experiencePrice);
        
        // When: Calculate doctor shift price
        Double calculatedPrice = doctorService.calculateDoctorShiftPrice(doctor);
        
        // Track output
        testOutputs.put("calculatedPrice", calculatedPrice);
        
        // Then: Price should be calculated correctly
        assertEquals(150.0, calculatedPrice, 0.01); // 100.0 * 1.5
    }

    /**
     * Test Case 13: Calculate Doctor Shift Price - Missing Degree
     * Goal: Verify calculation of doctor's shift price when degree is missing
     * Input: Doctor with missing degree but valid years of experience
     * Expected Output: Base price from default degree
     * Notes: Should use default degree price when doctor's degree is missing
     */
    @Test
    @Order(13)
    @DisplayName("TC-DS-013: Calculate Doctor Shift Price - Missing Degree")
    void testCalculateDoctorShiftPriceMissingDegree() {
        // Given: Doctor with missing degree
        Doctor doctor = doctorRepository.findById(TEST_DOCTOR_ID).orElseThrow();
        doctor.setDegree(null);
        doctor = doctorRepository.save(doctor);
        
        // Given: Default degree price
        DoctorShiftPriceByDegree defaultDegreePrice = new DoctorShiftPriceByDegree();
        defaultDegreePrice.setDegree(DoctorShiftPriceByDegree.BASE_DEGREE);
        defaultDegreePrice.setBasePrice(50.0);
        defaultDegreePrice = doctorShiftPriceByDegreeRepository.save(defaultDegreePrice);
        
        // Track input
        testInputs.put("doctor", doctor);
        testInputs.put("defaultDegreePrice", defaultDegreePrice);
        
        // When: Calculate doctor shift price
        Double calculatedPrice = doctorService.calculateDoctorShiftPrice(doctor);
        
        // Track output
        testOutputs.put("calculatedPrice", calculatedPrice);
        
        // Then: Price should be base price from default degree
        assertEquals(50.0, calculatedPrice, 0.01);
    }

    /**
     * Test Case 14: Calculate Doctor Shift Price - Missing Experience
     * Goal: Verify calculation of doctor's shift price when years of experience is missing
     * Input: Doctor with valid degree but missing years of experience
     * Expected Output: Base price from doctor's degree
     * Notes: Should use base price when doctor's years of experience is missing
     */
    @Test
    @Order(14)
    @DisplayName("TC-DS-014: Calculate Doctor Shift Price - Missing Experience")
    void testCalculateDoctorShiftPriceMissingExperience() {
        // Given: Doctor with missing years of experience
        Doctor doctor = doctorRepository.findById(TEST_DOCTOR_ID).orElseThrow();
        doctor.setYearsOfExperience(null);
        doctor = doctorRepository.save(doctor);
        
        // Given: Degree price
        DoctorShiftPriceByDegree degreePrice = new DoctorShiftPriceByDegree();
        degreePrice.setDegree("MD");
        degreePrice.setBasePrice(100.0);
        degreePrice = doctorShiftPriceByDegreeRepository.save(degreePrice);
        
        // Track input
        testInputs.put("doctor", doctor);
        testInputs.put("degreePrice", degreePrice);
        
        // When: Calculate doctor shift price
        Double calculatedPrice = doctorService.calculateDoctorShiftPrice(doctor);
        
        // Track output
        testOutputs.put("calculatedPrice", calculatedPrice);
        
        // Then: Price should be base price from doctor's degree
        assertEquals(100.0, calculatedPrice, 0.01);
    }

    /**
     * Test Case 15: Calculate Doctor Shift Price - Unknown Degree
     * Goal: Verify calculation of doctor's shift price when degree is unknown
     * Input: Doctor with degree not in the price table
     * Expected Output: Base price from default degree
     * Notes: Should use default degree price when doctor's degree is not found in the price table
     */
    @Test
    @Order(15)
    @DisplayName("TC-DS-015: Calculate Doctor Shift Price - Unknown Degree")
    void testCalculateDoctorShiftPriceUnknownDegree() {
        // Given: Doctor with unknown degree
        Doctor doctor = doctorRepository.findById(TEST_DOCTOR_ID).orElseThrow();
        doctor.setDegree("UnknownDegree");
        doctor = doctorRepository.save(doctor);
        
        // Given: Default degree price
        DoctorShiftPriceByDegree defaultDegreePrice = new DoctorShiftPriceByDegree();
        defaultDegreePrice.setDegree(DoctorShiftPriceByDegree.BASE_DEGREE);
        defaultDegreePrice.setBasePrice(50.0);
        defaultDegreePrice = doctorShiftPriceByDegreeRepository.save(defaultDegreePrice);
        
        // Track input
        testInputs.put("doctor", doctor);
        testInputs.put("defaultDegreePrice", defaultDegreePrice);
        
        // When: Calculate doctor shift price
        Double calculatedPrice = doctorService.calculateDoctorShiftPrice(doctor);
        
        // Track output
        testOutputs.put("calculatedPrice", calculatedPrice);
        
        // Then: Price should be base price from default degree
        assertEquals(50.0, calculatedPrice, 0.01);
    }

    /**
     * Test Case 16: Calculate Doctor Shift Price - No Matching Experience Level
     * Goal: Verify calculation of doctor's shift price when no matching experience level is found
     * Input: Doctor with degree and years of experience higher than any defined level
     * Expected Output: Price calculated with highest defined experience multiplier
     * Notes: Should use the highest defined experience multiplier when doctor's years exceed all defined levels
     */
    @Test
    @Order(16)
    @DisplayName("TC-DS-016: Calculate Doctor Shift Price - No Matching Experience Level")
    void testCalculateDoctorShiftPriceNoMatchingExperienceLevel() {
        // Given: Doctor with very high years of experience
        Doctor doctor = doctorRepository.findById(TEST_DOCTOR_ID).orElseThrow();
        doctor.setYearsOfExperience(30); // Very high experience
        doctor = doctorRepository.save(doctor);
        
        // Given: Degree price
        DoctorShiftPriceByDegree degreePrice = new DoctorShiftPriceByDegree();
        degreePrice.setDegree("MD");
        degreePrice.setBasePrice(100.0);
        degreePrice = doctorShiftPriceByDegreeRepository.save(degreePrice);
        
        // Given: Experience prices with lower levels
        DoctorShiftPriceByExperience experiencePrice1 = new DoctorShiftPriceByExperience();
        experiencePrice1.setExperience(5);
        experiencePrice1.setMultiplier(1.2);
        experiencePrice1 = doctorShiftPriceByExperienceRepository.save(experiencePrice1);
        
        DoctorShiftPriceByExperience experiencePrice2 = new DoctorShiftPriceByExperience();
        experiencePrice2.setExperience(10);
        experiencePrice2.setMultiplier(1.5);
        experiencePrice2 = doctorShiftPriceByExperienceRepository.save(experiencePrice2);
        
        // Track input
        testInputs.put("doctor", doctor);
        testInputs.put("degreePrice", degreePrice);
        testInputs.put("experiencePrice1", experiencePrice1);
        testInputs.put("experiencePrice2", experiencePrice2);
        
        // When: Calculate doctor shift price
        Double calculatedPrice = doctorService.calculateDoctorShiftPrice(doctor);
        
        // Track output
        testOutputs.put("calculatedPrice", calculatedPrice);
        
        // Then: Price should be calculated with highest defined experience multiplier
        assertEquals(150.0, calculatedPrice, 0.01); // 100.0 * 1.5 (highest multiplier)
    }
} 