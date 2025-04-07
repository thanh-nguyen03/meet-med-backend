package com.thanhnd.clinic_application.modules.patients.service;

import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.entity.Patient;
import com.thanhnd.clinic_application.entity.User;
import com.thanhnd.clinic_application.mapper.PatientMapper;
import com.thanhnd.clinic_application.modules.patients.dto.PatientDto;
import com.thanhnd.clinic_application.modules.patients.repository.PatientRepository;
import com.thanhnd.clinic_application.modules.patients.service.impl.PatientServiceImpl;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import com.thanhnd.clinic_application.modules.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private JwtAuthenticationManager jwtAuthenticationManager;

    @InjectMocks
    private PatientServiceImpl patientService;

    private User mockUser;
    private Patient mockPatient;
    private PatientDto mockPatientDto;
    private UserDto mockUserDto;

    @BeforeEach
    void setUp() {
        // Setup mock User
        mockUser = new User();
        mockUser.setId("uuid-patient-1");
        mockUser.setEmail("patient1@clinic.com");
        mockUser.setFullName("Alice Smith");
        mockUser.setAge(34);
        mockUser.setPhone("555-1111");

        // Setup mock Patient
        mockPatient = new Patient();
        mockPatient.setId("uuid-patient-1-profile");
        mockPatient.setDateOfBirth(LocalDate.of(1990, 5, 15));
        mockPatient.setAddressLine("123 Health St");
        mockPatient.setDistrict("Wellness");
        mockPatient.setCity("MedCity");
        mockPatient.setInsuranceCode("INS12345");
        mockPatient.setUser(mockUser);

        // Setup mock UserDto
        mockUserDto = new UserDto();
        mockUserDto.setId("uuid-patient-1");
        mockUserDto.setEmail("patient1@clinic.com");
        mockUserDto.setFullName("Alice Smith");
        mockUserDto.setAge(34);
        mockUserDto.setPhone("555-1111");

        // Setup mock PatientDto
        mockPatientDto = new PatientDto();
        mockPatientDto.setId("uuid-patient-1-profile");
        mockPatientDto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        mockPatientDto.setAddressLine("123 Health St");
        mockPatientDto.setDistrict("Wellness");
        mockPatientDto.setCity("MedCity");
        mockPatientDto.setInsuranceCode("INS12345");
        mockPatientDto.setUser(mockUserDto);

        // Setup mapper mock behavior
        when(patientMapper.toDto(any(Patient.class))).thenReturn(mockPatientDto);
        when(patientMapper.toEntity(any(PatientDto.class))).thenReturn(mockPatient);
    }

    /**
     * Testcase 1 `findAll_ReturnsListOfPatientDtos`
     * - Goal: Verify that all patients can be retrieved successfully.
     * - Test Case Code/Steps:
     *   1. Mock patientRepository to return a list with one patient.
     *   2. Call patientService.findAll().
     *   3. Assert the response contains the expected patient data.
     * - Input: N/A (method takes no parameters).
     * - Expected Output: List containing one PatientDto matching the mock data.
     * - Note: Tests the basic retrieval functionality.
     */
    @Test
    void findAll_ReturnsListOfPatientDtos() {
        // Arrange
        when(patientRepository.findAll()).thenReturn(List.of(mockPatient));

        // Act
        List<PatientDto> result = patientService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockPatientDto, result.get(0));
    }

    /**
     * Testcase 2 `findByUserId_ExistingUser_ReturnsPatientDto`
     * - Goal: Verify a patient profile can be successfully retrieved by user ID when it exists.
     * - Test Case Code/Steps:
     *   1. Mock patientRepository to return a patient when findByUserId is called with "uuid-patient-1".
     *   2. Call patientService.findByUserId("uuid-patient-1").
     *   3. Assert the response contains the expected patient profile.
     * - Input: userId = "uuid-patient-1"
     * - Expected Output: PatientDto object matching the mock patient data.
     * - Note: Tests happy path for profile retrieval by user ID.
     */
    @Test
    void findByUserId_ExistingUser_ReturnsPatientDto() {
        // Arrange
        when(patientRepository.findByUserId("uuid-patient-1")).thenReturn(Optional.of(mockPatient));

        // Act
        PatientDto result = patientService.findByUserId("uuid-patient-1");

        // Assert
        assertNotNull(result);
        assertEquals(mockPatientDto, result);
    }

    /**
     * Testcase 3 `findByUserId_NonExistingUser_ThrowsException`
     * - Goal: Verify error handling when trying to get a patient profile for a non-existing user.
     * - Test Case Code/Steps:
     *   1. Mock patientRepository to return empty Optional when findByUserId is called with "non-existing-user-id".
     *   2. Call patientService.findByUserId("non-existing-user-id").
     *   3. Assert that HttpException is thrown with BAD_REQUEST status.
     * - Input: userId = "non-existing-user-id"
     * - Expected Output: HttpException with BAD_REQUEST status
     * - Note: Tests error case for profile retrieval when patient doesn't exist.
     */
    @Test
    void findByUserId_NonExistingUser_ThrowsException() {
        // Arrange
        when(patientRepository.findByUserId("non-existing-user-id")).thenReturn(Optional.empty());

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class, () -> {
            patientService.findByUserId("non-existing-user-id");
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    /**
     * Testcase 4 `create_NewPatient_Success`
     * - Goal: Verify a new patient profile can be successfully created for an existing user.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication to return a valid user ID.
     *   2. Mock userRepository to return a User entity for the given ID.
     *   3. Mock patientRepository.save to return a saved patient entity.
     *   4. Call patientService.create with a valid PatientDto.
     *   5. Assert the result is not null and patientRepository.save was called.
     * - Input: PatientDto with user details
     * - Expected Output: Created PatientDto matching mock data
     * - Note: Tests happy path for patient profile creation.
     */
    @Test
    void create_NewPatient_Success() {
        // Arrange
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-1");
        when(userRepository.findById("uuid-patient-1")).thenReturn(Optional.of(mockUser));
        when(patientRepository.save(any(Patient.class))).thenReturn(mockPatient);

        // Act
        PatientDto result = patientService.create(mockPatientDto);

        // Assert
        assertNotNull(result);
        verify(patientRepository).save(any(Patient.class));
    }

    /**
     * Testcase 5 `create_UserNotFound_ThrowsException`
     * - Goal: Verify creating a patient profile fails if the associated user is not found.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication to return a non-existing user ID.
     *   2. Mock userRepository to return empty Optional for the given ID.
     *   3. Call patientService.create with a PatientDto.
     *   4. Assert that HttpException is thrown with BAD_REQUEST status.
     * - Input: PatientDto with user details
     * - Expected Output: HttpException with BAD_REQUEST status
     * - Note: Tests error case for profile creation when user doesn't exist.
     */
    @Test
    void create_UserNotFound_ThrowsException() {
        // Arrange
        when(jwtAuthenticationManager.getUserId()).thenReturn("non-existing-user");
        when(userRepository.findById("non-existing-user")).thenReturn(Optional.empty());

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class, () -> {
            patientService.create(mockPatientDto);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    /**
     * Testcase 6 `create_PatientAlreadyExists_ThrowsException`
     * - Goal: Verify creating a patient profile fails if the user already has one.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication to return a valid user ID.
     *   2. Create a User entity that already has an associated Patient.
     *   3. Mock userRepository to return this User entity.
     *   4. Call patientService.create with a PatientDto.
     *   5. Assert that HttpException is thrown with BAD_REQUEST status.
     * - Input: PatientDto with user details
     * - Expected Output: HttpException with BAD_REQUEST status
     * - Note: Tests business rule that a user can only have one patient profile.
     */
    @Test
    void create_PatientAlreadyExists_ThrowsException() {
        // Arrange
        User userWithPatient = new User();
        userWithPatient.setId("uuid-patient-1");
        userWithPatient.setPatient(mockPatient); // User already has a patient

        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-1");
        when(userRepository.findById("uuid-patient-1")).thenReturn(Optional.of(userWithPatient));

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class, () -> {
            patientService.create(mockPatientDto);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    /**
     * Testcase 7 `update_ExistingPatient_Success`
     * - Goal: Verify a patient can successfully update their existing profile.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication to return a valid user ID.
     *   2. Mock userRepository to return an existing User entity.
     *   3. Mock patientRepository to return an existing Patient entity.
     *   4. Mock repositories to return saved entities on save operations.
     *   5. Update fields in the PatientDto for testing.
     *   6. Call patientService.update with the modified PatientDto.
     *   7. Assert result is not null and repository save methods were called.
     * - Input: Updated PatientDto with modified address and user details
     * - Expected Output: Updated PatientDto matching mock data
     * - Note: Tests happy path for patient profile update.
     */
    @Test
    void update_ExistingPatient_Success() {
        // Arrange
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-1");
        when(userRepository.findById("uuid-patient-1")).thenReturn(Optional.of(mockUser));
        when(patientRepository.findByUserId("uuid-patient-1")).thenReturn(Optional.of(mockPatient));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(mockPatient);

        // Update some fields in DTO
        mockPatientDto.setAddressLine("456 Updated St");
        mockPatientDto.setDistrict("New District");
        mockUserDto.setFullName("Alice Smith Updated");

        // Act
        PatientDto result = patientService.update(mockPatientDto);

        // Assert
        assertNotNull(result);
        verify(patientMapper).merge(any(Patient.class), eq(mockPatientDto));
        verify(patientRepository).save(any(Patient.class));
        verify(userRepository).save(any(User.class));
    }

    /**
     * Testcase 8 `update_UserNotFound_ThrowsException`
     * - Goal: Verify updating a patient profile fails if the associated user is not found.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication to return a non-existing user ID.
     *   2. Mock userRepository to return empty Optional for the given ID.
     *   3. Call patientService.update with a PatientDto.
     *   4. Assert that HttpException is thrown with BAD_REQUEST status.
     * - Input: PatientDto with user details
     * - Expected Output: HttpException with BAD_REQUEST status
     * - Note: Tests error case for profile update when user doesn't exist.
     */
    @Test
    void update_UserNotFound_ThrowsException() {
        // Arrange
        when(jwtAuthenticationManager.getUserId()).thenReturn("non-existing-user");
        when(userRepository.findById("non-existing-user")).thenReturn(Optional.empty());

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class, () -> {
            patientService.update(mockPatientDto);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    /**
     * Testcase 9 `update_PatientNotExists_ThrowsException`
     * - Goal: Verify updating a profile fails if the patient profile does not exist yet.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication to return a valid user ID.
     *   2. Create a User entity that has no associated Patient.
     *   3. Mock userRepository to return this User entity.
     *   4. Call patientService.update with a PatientDto.
     *   5. Assert that HttpException is thrown with BAD_REQUEST status.
     * - Input: PatientDto with user details
     * - Expected Output: HttpException with BAD_REQUEST status
     * - Note: Tests error case where user exists but no patient profile has been created yet.
     */
    @Test
    void update_PatientNotExists_ThrowsException() {
        // Arrange
        User userWithoutPatient = new User();
        userWithoutPatient.setId("uuid-patient-1");
        userWithoutPatient.setPatient(null); // User doesn't have a patient yet

        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-1");
        when(userRepository.findById("uuid-patient-1")).thenReturn(Optional.of(userWithoutPatient));

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class, () -> {
            patientService.update(mockPatientDto);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    /**
     * Testcase 10 `update_PatientNotFoundForUser_ThrowsException`
     * - Goal: Verify updating a profile fails if the patient profile cannot be found in the repository.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication to return a valid user ID.
     *   2. Mock userRepository to return an existing User entity.
     *   3. Mock patientRepository to return empty Optional when findByUserId is called.
     *   4. Call patientService.update with a PatientDto.
     *   5. Assert that HttpException is thrown with BAD_REQUEST status.
     * - Input: PatientDto with user details
     * - Expected Output: HttpException with BAD_REQUEST status
     * - Note: Tests repository error case where user exists but patient profile can't be found.
     */
    @Test
    void update_PatientNotFoundForUser_ThrowsException() {
        // Arrange
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-1");
        when(userRepository.findById("uuid-patient-1")).thenReturn(Optional.of(mockUser));
        when(patientRepository.findByUserId("uuid-patient-1")).thenReturn(Optional.empty());

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class, () -> {
            patientService.update(mockPatientDto);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    /**
     * Testcase 11 `update_UserDataUpdated_Success`
     * - Goal: Verify user details are correctly updated when updating a patient profile.
     * - Test Case Code/Steps:
     *   1. Mock JWT authentication to return a valid user ID.
     *   2. Mock repositories to return existing entities and saved entities.
     *   3. Update user data in the PatientDto (fullName, age, phone).
     *   4. Call patientService.update with the modified PatientDto.
     *   5. Capture the User entity passed to userRepository.save.
     *   6. Assert the captured User has the expected updated values.
     * - Input: PatientDto with updated user details (fullName="New Name", age=35, phone="555-2222")
     * - Expected Output: User entity updated with new values
     * - Note: Tests that user data is properly synchronized when updating patient profile.
     */
    @Test
    void update_UserDataUpdated_Success() {
        // Arrange
        when(jwtAuthenticationManager.getUserId()).thenReturn("uuid-patient-1");
        when(userRepository.findById("uuid-patient-1")).thenReturn(Optional.of(mockUser));
        when(patientRepository.findByUserId("uuid-patient-1")).thenReturn(Optional.of(mockPatient));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(mockPatient);

        // Update user data in DTO
        mockUserDto.setFullName("New Name");
        mockUserDto.setAge(35);
        mockUserDto.setPhone("555-2222");

        // Act
        patientService.update(mockPatientDto);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertEquals("New Name", capturedUser.getFullName());
        assertEquals(Integer.valueOf(35), capturedUser.getAge());
        assertEquals("555-2222", capturedUser.getPhone());
    }
} 