package com.thanhnd.clinic_application.modules.rooms;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.entity.Department;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thanhnd.clinic_application.entity.Room;
import com.thanhnd.clinic_application.mapper.RoomMapper;
import com.thanhnd.clinic_application.modules.departments.repository.DepartmentRepository;
import com.thanhnd.clinic_application.modules.rooms.dto.RoomDto;
import com.thanhnd.clinic_application.modules.rooms.repository.RoomRepository;
import com.thanhnd.clinic_application.modules.rooms.service.impl.RoomServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Tag("service")
class RoomServiceImplTest {
    /**
     * TC_RS_01: Ensure that findAll() method returns correct list of RoomDto
     * Input: List of Room entities with IDs "R001", "R002"
     * Expected Output: List of RoomDto with same IDs
     */
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomServiceImpl roomService;
//    @MockBean
//    private RoomRepository mockRoomRepository;  // Mocked RoomRepository

    private static final Logger logger = LoggerFactory.getLogger(RoomServiceImplTest.class);
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * TC_RS_001: Ensure that findAll() method returns correct list of RoomDto
     * Input: List of Room entities with IDs "R001", "R002"
     * Expected Output: List of RoomDto with same IDs
     */
    @Test
    @Transactional
    @DisplayName("✅ TC_RS_001: Ensure that findAll() method returns correct list of RoomDto")
    void testFindAll_ReturnsRoomDtoList() {
        // Step 1: Arrange - Set up room data and save to the database
        Department department = new Department();
        department.setDescription("This is a test department");
        department.setName("Test");

        // Save Department to real DB
        Department savedDepartment = departmentRepository.save(department);
        // Ensure the department is persisted by flushing the EntityManager
        entityManager.flush();
        System.out.println("Saved department with ID: " + savedDepartment.getId());

        //- Get initial count of rooms in the database before adding new ones
        long initialRoomCount = roomRepository.count();
        System.out.println("Initial room count: " + initialRoomCount);

        // Step 1: Arrange - Create Room entities and save them to the database
        Room room1 = new Room();
        room1.setName("Room 101");
        room1.setDepartment(savedDepartment);
        roomRepository.save(room1);

        Room room2 = new Room();
        room2.setName("Room 102");
        room2.setDepartment(savedDepartment);
        roomRepository.save(room2);

        // Flush the entity manager to ensure the rooms are saved
        entityManager.flush();
        entityManager.clear();

        // Step 2: Act - Call the service method
        List<RoomDto> result = roomService.findAll();

        // Step 3: Output result for debugging
        System.out.println("Test Result:");
        System.out.println("Number of rooms: " + result.size());
        result.forEach(roomDto -> System.out.println("Room ID: " + roomDto.getName()));

        // Step 4: Assert - Validate the expected behavior
        assertEquals(initialRoomCount+2, result.size());  // We expect 2 rooms to be returned
        assertTrue(result.stream().anyMatch(roomDto -> roomDto.getId().equals(room1.getId())));
        assertTrue(result.stream().anyMatch(roomDto -> roomDto.getId().equals(room2.getId())));

        System.out.println("Test passed: findAll returned the correct list of RoomDto.");
    }

    /**
     * TC_RS_002: Verify the findById() method when room exists
     * Test Objective: Ensure that the method returns the correct RoomDto when the room is found.
     * Input: Room ID "R001"
     * Expected Output: RoomDto with ID "R001"
     */
    @Test
    @Transactional
    @DisplayName("✅ TC_RS_002: Verify the findById() method when room exists")
    void testFindById_RoomExists_ReturnsRoomDto() {
        // Step 1: Arrange - Set up room data and save to the database
        Department department = new Department();
        department.setDescription("This is a test department");
        department.setName("Test Department");

        // Save Department to real DB
        Department savedDepartment = departmentRepository.save(department);
        // Ensure the department is persisted by flushing the EntityManager
        entityManager.flush();
        System.out.println("Saved department with ID: " + savedDepartment.getId());

        Room room = new Room();
        room.setName("Room 101");
        room.setDepartment(savedDepartment);
        roomRepository.save(room);

        // Step 2: Act - Call the service method
        RoomDto result = roomService.findById(room.getId());

        // Step 3: Log the result and status
        if (result != null) {
            System.out.println("Room found with ID: " + result.getId());
        } else {
            System.out.println("No room found with ID: " + room.getId());
        }

        // Step 4: Assert - Check the result
        assertNotNull(result);
        assertEquals(room.getId(), result.getId());
        System.out.println("Test passed: findById returned the correct RoomDto.");
    }

    /**
     * TC_RS_003: Verify the findById() method when room does not exist
     * Test Objective: Ensure that the method throws an exception when the room is not found.
     * Input: Room ID "R001" (Room does not exist)
     * Expected Output: HttpException with a "Room not found" message
     */
    @Test
    @Transactional
    @DisplayName("✅ TC_RS_003: Verify the findById() method when room does not exist")
    void testFindById_RoomNotFound_ThrowsException() {
        // Step 1: Arrange - Room ID "R001" does not exist, so no room is saved in the database

        // Step 2: Act & Assert - Call service method and check for exception
        System.out.println("Attempting to find room with ID: R001 (which does not exist).");
        HttpException exception = assertThrows(HttpException.class, () -> roomService.findById("R001"));

        // Step 3: Log the exception details
        System.out.println("Exception thrown: " + exception.getMessage());
        logger.error("Exception thrown: {}", exception.getMessage());

        // Step 4: Assert - Validate the exception message
        assertEquals(Message.ROOM_NOT_FOUND.getMessage(), exception.getMessage());
        System.out.println("Test passed: Correct exception thrown for non-existing room.");
    }

    /**
     * TC_RS_004: Test createRoom() method when successful
     * Test Objective: Verify that a new room is created successfully and returned as RoomDto.
     * Input: RoomDto with room name "Room 101" and department ID "D001"
     * Expected Output: RoomDto with name "Room 101" and department ID "D001" have created
     * Notes: The test ensures that the transaction is rolled back after the test.
     */
    @Test
    @Transactional  // Ensures rollback after the test
    @DisplayName("✅ TC-RS-004: Create Room Success")
    void testCreateRoom_Success_ReturnsRoomDto_AndRollbacks() {
        // Create Department entity for the real DB
        Department department = new Department();
        department.setDescription("This is a test department");
        department.setName("Test Department");

        // Save Department to real DB
        Department savedDepartment = departmentRepository.save(department);
        // Ensure the department is persisted by flushing the EntityManager
        entityManager.flush();
        System.out.println("Saved department with ID: " + savedDepartment.getId());

        // Create DTOs
        RoomDto roomDto = new RoomDto();
        roomDto.setName("Room 101");

        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setId(savedDepartment.getId());
        roomDto.setDepartment(departmentDto);

        // Act: Call real service method to save the Room in the DB
        RoomDto result = roomService.createRoom(roomDto);

        // Flush and clear persistence context to ensure real DB state is checked
        entityManager.flush();
        entityManager.clear();

        // Print the result
        System.out.println("✅ RoomDto created: ID: " + result.getId() + ", Name: " + result.getName());

        // Assert: Validate the Room DTO returned
        assertNotNull(result, "RoomDto should not be null");
        assertNotNull(result.getId(), "Generated ID should not be null");
        assertEquals("Room 101", result.getName(), "Room name should match");
        assertEquals(savedDepartment.getId(), result.getDepartment().getId(), "Department ID should match");

        // Verify the room exists in the real database (not mocked)
        Room savedRoom = roomRepository.findById(result.getId()).orElse(null);
        assertNotNull(savedRoom, "Room should exist in real DB");
        assertEquals("Room 101", savedRoom.getName(), "Room name should match saved value");
        assertEquals(savedDepartment.getId(), savedRoom.getDepartment().getId(), "Department ID should match saved value");

        // After the test, the transaction will automatically be rolled back
    }

    /**
     * TC_RS_005: Test createRoom() when department is not found
     * Test Objective: Ensure that the method throws an exception when the department is not found.
     * Input: Department ID "D001" that does not exist
     * Expected Output: HttpException with a "Department not found" message
     */
    @Test
    @Transactional  // Ensure the changes are rolled back after the test
    @DisplayName("✅ TC_RS_005: Test createRoom() when department is not found")
    void testCreateRoom_DepartmentNotFound_ThrowsException_AndRollbacks() {
        // Arrange
        String departmentId = "D001";
        RoomDto roomDto = new RoomDto();
        roomDto.setName("Room 101");
        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setId(departmentId);
        roomDto.setDepartment(departmentDto);

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class, () -> roomService.createRoom(roomDto));

        // Log the exception details
        System.out.println("Exception thrown: " + exception.getMessage());

        // Assert that the exception message is correct
        assertEquals(Message.DEPARTMENT_NOT_FOUND.getMessage(), exception.getMessage());

        // After the test, the transaction will automatically be rolled back
    }

    /**
     * TC_RS_006: Test createRoom() when room name already exists
     * Test Objective: Ensure that the method throws an exception when a room with the same name already exists in the department.
     * Input: Room name "Room 101" already exists in department "D001"
     * Expected Output: HttpException with a "Room name already exists" message
     */
    @Test
    @Transactional
    @DisplayName("✅ TC_RS_006: Test createRoom() when room name already exists")
    void testCreateRoom_RoomNameAlreadyExists_ThrowsException_AndRollbacks() {
        // Step 1: Create and persist a Department
        Department department = new Department();
        department.setDescription("This is a test department");
        department.setName("Test Department");

        Department savedDepartment = departmentRepository.save(department);
        entityManager.flush();
        System.out.println("Saved department with ID: " + savedDepartment.getId());

        // Step 2: Persist a Room with the same name manually
        Room existingRoom = new Room();
        existingRoom.setName("Room 101");
        existingRoom.setDepartment(savedDepartment);
        roomRepository.save(existingRoom);
        entityManager.flush();
        System.out.println("Saved existing room with name: " + existingRoom.getName());

        // Step 3: Create DTO with the same room name
        RoomDto roomDto = new RoomDto();
        roomDto.setName("Room 101");

        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setId(savedDepartment.getId());
        roomDto.setDepartment(departmentDto);

        // Step 4: Expect exception on duplicate room name
        HttpException exception = assertThrows(HttpException.class, () -> roomService.createRoom(roomDto));
        System.out.println("Exception thrown: " + exception.getMessage());

        assertEquals(
                Message.ROOM_NAME_ALREADY_EXISTS.getMessage(roomDto.getName(), savedDepartment.getName()),
                exception.getMessage()
        );

    }

    /**
     * TC_RS_007: Test updateRoom() method when room is successfully updated
     * Test Objective: Ensure that the room is updated correctly and the RoomDto is returned.
     * Input: RoomDto with updated name "Updated Room 101"
     * Expected Output: RoomDto with updated name "Updated Room 101"
     * Notes: This test checks rollback and database consistency.
     */
    @Test
    @Transactional
    @DisplayName("✅ TC_RS_007: Test updateRoom() method when room is successfully updated")
    void testUpdateRoom_Success() {
        // Step 1: Prepare Department entity and save it to real DB
        Department department = new Department();
        department.setName("Test Department");
        department.setDescription("This is a test department");

        Department savedDepartment = departmentRepository.save(department); // Save to DB
        entityManager.flush();  // Ensure persistence
        System.out.println("Saved department with ID: " + savedDepartment.getId());

        // Step 2: Prepare Room entity and save it to real DB
        Room room = new Room();
        room.setName("Old Room 101");
        room.setDepartment(savedDepartment);

        System.out.println("Room Name: " + room.getName());

        Room savedRoom = roomRepository.save(room);
        entityManager.flush();
        entityManager.clear();

        System.out.println("Persisted room: " + savedRoom.getId());

        // Step 3: Prepare RoomDto to update the room
        RoomDto roomDto = new RoomDto();
        roomDto.setId(savedRoom.getId()); // Ensure ID is passed as String
        roomDto.setName("Updated Room 101");

        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setId(savedDepartment.getId());
        roomDto.setDepartment(departmentDto);  // Set the DepartmentDto in RoomDto

        System.out.println("RoomDto to update: " + roomDto.getName() + " with department ID: " + departmentDto.getId());

        // Step 4: Act - Call the real service method to update the room
        RoomDto result = roomService.updateRoom(roomDto);

        // Step 5: Assert - Validate the updated RoomDto
        assertNotNull(result, "Updated RoomDto should not be null");
        assertEquals("Updated Room 101", result.getName(), "Room name should be updated");
        assertEquals(savedDepartment.getId(), result.getDepartment().getId(), "Department ID should match");

        // Step 6: Verify the room was updated in the real DB
        Room updatedRoom = roomRepository.findById(savedRoom.getId()).orElse(null);
        assertNotNull(updatedRoom, "Room should exist in DB after update");
        assertEquals("Updated Room 101", updatedRoom.getName(), "Room name should match updated value");

    }

    /**
     * TC_RS_008: Test updateRoom() method when room does not exist
     * Test Objective: Ensure that the method throws an exception when the room is not found.
     * Input: Room ID "R001" does not exist
     * Expected Output: HttpException with a "Room not found" message
     */
    @Test
    @Transactional
    @DisplayName("✅ TC_RS_008: Test updateRoom() method when room does not exist")
    void testUpdateRoom_RoomNotFound() {
        String roomId = "R001";
        RoomDto roomDto = new RoomDto();
        roomDto.setId(roomId);
        roomDto.setName("Updated Room 101");

        HttpException exception = assertThrows(HttpException.class, () -> {
            roomService.updateRoom(roomDto);
        });

        logger.error("Exception thrown: {}", exception.getMessage());
        assertEquals(Message.ROOM_NOT_FOUND.getMessage(), exception.getMessage());
    }

    /**
     * TC_RS_009: Test updateRoom() method when department does not exist
     * Test Objective: Ensure that the method throws an exception when the department is not found.
     * Input: Department ID "D001" does not exist
     * Expected Output: HttpException with a "Department not found" message
     */
    @Test
    @Transactional
    @DisplayName("✅ TC_RS_009: Test updateRoom() method when department does not exist")
    void testUpdateRoom_DepartmentNotFound() {
        // 1. Save a valid department to avoid null constraint, but we will use a different ID later
        Department existingDept = new Department();
        existingDept.setName("Test Department");
        existingDept.setDescription("This is a test department");
        departmentRepository.save(existingDept);

        // 2. Save room with a valid department
        Room room = new Room();
        room.setName("Old Room 101");
        room.setDepartment(existingDept); // avoid constraint violation
        room = roomRepository.save(room);

        entityManager.flush();
        entityManager.clear();

        // 3. Create DTO with non-existent department ID
        RoomDto roomDto = new RoomDto();
        roomDto.setId(room.getId());
        roomDto.setName("Updated Room 101");

        DepartmentDto deptDto = new DepartmentDto();
        deptDto.setId("D001"); // this department does not exist in DB
        roomDto.setDepartment(deptDto);

        // 4. Test updateRoom
        HttpException exception = assertThrows(HttpException.class, () -> {
            roomService.updateRoom(roomDto);
        });

        logger.error("Exception thrown: {}", exception.getMessage());
        assertEquals(Message.DEPARTMENT_NOT_FOUND.getMessage(), exception.getMessage());;
    }

    /**
     * TC_RS_010: Test updateRoom() method when room name already exists
     * Test Objective: Ensure that the method throws an exception when the room name already exists in the department.
     * Input: Room name "Updated Room 101" already exists in department "D001"
     * Expected Output: HttpException with a "Room name already exists" message
     */
    @Test
    @Transactional
    @DisplayName("✅ TC_RS_010: Test updateRoom() method when room name already exists in the same department")
    void testUpdateRoom_RoomNameAlreadyExists() {
        // Step 1: Create and save department (ID will be auto-generated)
        Department department = new Department();
        department.setName("Test Department");
        department.setDescription("This is a test department");
        departmentRepository.save(department);

        // Step 2: Save first room (room to be updated)
        Room room = new Room();
        room.setName("Old Room 101");
        room.setDepartment(department);
        roomRepository.save(room);

        // Step 3: Save another room with conflicting name
        Room duplicateRoom = new Room();
        duplicateRoom.setName("Updated Room 101"); // duplicate name
        duplicateRoom.setDepartment(department);
        roomRepository.save(duplicateRoom);

        // Step 4: Prepare update request for the first room
        RoomDto roomDto = new RoomDto();
        roomDto.setId(room.getId()); // Use the auto-generated ID from the saved room
        roomDto.setName("Updated Room 101"); // Trying to update to the conflicting name

        DepartmentDto deptDto = new DepartmentDto();
        deptDto.setId(department.getId()); // Use the auto-generated department ID
        roomDto.setDepartment(deptDto);

        // Step 5: Perform test and assert exception
        HttpException exception = assertThrows(HttpException.class, () -> {
            roomService.updateRoom(roomDto);
        });

        logger.error("Exception thrown: {}", exception.getMessage());
        assertEquals(
                Message.ROOM_NAME_ALREADY_EXISTS.getMessage("Updated Room 101", "Test Department"),
                exception.getMessage()
        );
    }

    /**
     * TC_RS_011: Test deleteRoom() method when room is successfully deleted
     * Test Objective: Ensure that a room is deleted correctly.
     * Input: Room ID "R001"
     * Expected Output: Room is deleted successfully
     */
    @Test
    @Transactional
    @DisplayName("✅ TC_RS_011: Test deleteRoom() method when room is successfully deleted")
    void testDeleteRoom_Success() {
        // Step 1: Create and save a room
        Room room = new Room();
        room.setName("Room to Delete");
        room = roomRepository.save(room);
        System.out.print("Room created with ID: " + room.getId() + "\n");

        // Step 2: Delete the room
        roomService.deleteRoom(room.getId());

        // Step 3: Assert that the room is deleted by checking if it no longer exists in the repository
        Optional<Room> deletedRoom = roomRepository.findById(room.getId());
        assertTrue(deletedRoom.isEmpty(), "Room should be deleted");

        System.out.print("Room with ID: " + room.getId() + " successfully deleted.\n");
    }

    /**
     * TC_RS_012: Test deleteRoom() method when room does not exist
     * Test Objective: Ensure that the method throws an exception when the room is not found.
     * Input: Room ID "R001" does not exist
     * Expected Output: HttpException with a "Room not found" message
     */
    @Test
    @Transactional
    @DisplayName("✅ TC_RS_012: Test deleteRoom() method when room does not exist")
    void testDeleteRoom_RoomNotFound() {
        // Step 1: Use a non-existing room ID for testing
        String roomId = "R001"; // Room does not exist

        // Step 2: Attempt to delete the non-existing room
        HttpException exception = assertThrows(HttpException.class, () -> {
            roomService.deleteRoom(roomId);
        });

        // Step 3: Assert the expected exception is thrown
        logger.error("Exception thrown: {}", exception.getMessage());
        assertEquals(Message.ROOM_NOT_FOUND.getMessage(), exception.getMessage());
    }


}


//    @Test
//    @Transactional  // Ensure the changes are rolled back after the test
//    @DisplayName("✅ TC-RS-001: Create Room Success")
//    void testCreateRoom_Success_ReturnsRoomDto_AndRollbacks() {
//        // Arrange
//        String departmentId = "D001";
//        RoomDto roomDto = new RoomDto();
//        roomDto.setName("Room 101");
//        DepartmentDto departmentDto = new DepartmentDto();
//        departmentDto.setId(departmentId);
//        roomDto.setDepartment(departmentDto);
//
//        Department department = new Department();
//        department.setId(departmentId);
//        department = departmentRepository.save(department);
//
//        Room room = new Room();
//        room.setName("Room 101");
//        room.setDepartment(department);
//
//        // Mock repository behavior
//        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
//        when(roomRepository.findRoomByNameAndDepartmentId(roomDto.getName(), departmentId)).thenReturn(Optional.empty());
//        when(roomMapper.toEntity(roomDto)).thenReturn(room);
//        when(roomMapper.toDto(room)).thenReturn(roomDto);
//        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
//            Room savedRoom = invocation.getArgument(0);
//            savedRoom.setId(UUID.randomUUID().toString()); // Simulate ID generation
//            return savedRoom;
//        });
//
//        // Act
//        RoomDto result = roomService.createRoom(roomDto);
//        entityManager.flush();
//        // Log the result
//        logger.info("RoomDto created: ID: {}, Name: {}", result.getId(), result.getName());
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("Room 101", result.getName());
//        assertEquals(departmentId, result.getDepartment().getId());
//
//        // Verify that save was called once
//        verify(roomRepository, times(1)).save(room);
//        // Check the database state to ensure that the room was saved
//        Room savedRoom = roomRepository.findById(result.getId()).orElse(null);
//        if (savedRoom != null) {
//            logger.info("Saved Room from DB: ID: {}, Name: {}", savedRoom.getId(), savedRoom.getName());
//        } else {
//            logger.warn("No Room found in the DB with ID: {}", result.getId());
//        }
//
//        // After the test, the transaction will automatically be rolled back
//    }