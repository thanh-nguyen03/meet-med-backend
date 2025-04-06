package com.thanhnd.clinic_application.modules.rooms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import com.thanhnd.clinic_application.modules.rooms.controller.AdminRoomController;
import com.thanhnd.clinic_application.modules.rooms.dto.RoomDto;
import com.thanhnd.clinic_application.modules.rooms.service.RoomService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MvcResult;

@ExtendWith(SpringExtension.class)
@SpringBootTest // This will load the entire application context
@AutoConfigureMockMvc // Automatically configure MockMvc
public class AdminRoomControllerTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc to simulate HTTP requests

    @MockBean
    private RoomService roomService; // Mock RoomService to isolate controller logic

    @Autowired
    private ObjectMapper objectMapper; // ObjectMapper for JSON processing

    @MockBean
    private JwtDecoder jwtDecoder; // Mock JwtDecoder to decode the JWT

    // Method to generate a valid JWT token using Spring Security's Jwt
    private Jwt createJwtToken(String userId) {
        Map<String, Object> headers = Map.of("alg", "HS256", "typ", "JWT");

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userId);
        userInfo.put("permissions", List.of("Permissions.Room.READ")); // 🔥 add permissions

        Map<String, Object> claims = new HashMap<>();
        claims.put("user_info", userInfo);  // 🔥 your logic likely reads permissions from here
        claims.put("sub", "auth0|" + userId);

        return new Jwt(
                "test-token-" + userId,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                headers,
                claims
        );
    }


    // Setup for each test
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock the RoomService to return a list of rooms
        RoomDto roomDto1 = new RoomDto();
        roomDto1.setName("Room 1");

        RoomDto roomDto2 = new RoomDto();
        roomDto2.setName("Room 2");

        List<RoomDto> roomDTOs = List.of(roomDto1, roomDto2);

        when(roomService.findAll()).thenReturn(roomDTOs);

        // Mock the JWT decoding process
        Jwt mockJwt = createJwtToken("user123");
        when(jwtDecoder.decode("test-token-user123")).thenReturn(mockJwt);

        // Manually authenticate the user with an admin role for each test
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken("user123", null, List.of(new SimpleGrantedAuthority("Permissions.Room.READ")));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    /**
     * TC_RC_001: Ensure that findAll() method returns correct list of RoomDto
     *
     * Input: Simulated request to GET /api/admin/room with proper authorization
     * - User: user123
     * - Authority: Permissions.Room.READ
     *
     * Expected Output:
     * - HTTP Status 200 (OK)
     * - JSON response
     * - The roomService.findAll() method is called exactly once
     */
    @Test
//    @PreAuthorize("hasAuthority(‘Permissions.Room.READ’)")
    @WithMockUser(username = "user123", authorities = {"Permissions.Room.READ"}) // Simulate user with correct permission
    void findAll_returnSuccess() throws Exception {
        // Set the user in the security context with necessary role/permission
//        UsernamePasswordAuthenticationToken authenticationToken =
//                new UsernamePasswordAuthenticationToken("user123", null, List.of(new SimpleGrantedAuthority("Permissions.Room.READ")));
//        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // Perform the GET request to fetch all rooms
        MvcResult result = mockMvc.perform(get("/api/admin/room")
                        .header("Authorization", "Bearer test-token-user123")
                        .accept("application/json"))
                .andExpect(status().isOk()) // Check HTTP 200 OK
                .andExpect(jsonPath("$.statusCode").value(200)) // Check statusCode in response
                .andExpect(jsonPath("$.success").value(true)) // Check success flag
                .andExpect(jsonPath("$.message").value("OK")) // Check message
                .andExpect(jsonPath("$.data[0].name").value("Room 1")) // Check first room name
                .andExpect(jsonPath("$.data[1].name").value("Room 2")) // Check second room name
                .andReturn();

        // print the response body
        String rawResponse = result.getResponse().getContentAsString();
        Object jsonResponse = objectMapper.readValue(rawResponse, Object.class);
        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonResponse);
        System.out.println("🔍 Response Body:\n" + prettyJson);

        // Verify that the findAll method of RoomService was called once
        verify(roomService, times(1)).findAll();
    }

    /**
     * TC_RC_002: Ensure that findAll() handles empty list correctly
     *
     * Input: Service returns empty list
     * Expected Output:
     * - HTTP Status 200 (OK)
     * - Response contains empty data array
     */
    @Test
    @WithMockUser(username = "user123", authorities = {"Permissions.Room.READ"})
    void findAll_returnEmptyList() throws Exception {
        when(roomService.findAll()).thenReturn(Collections.emptyList());

        MvcResult result = mockMvc.perform(get("/api/admin/room")
                        .header("Authorization", "Bearer test-token-user123")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andReturn();

        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readValue(result.getResponse().getContentAsString(), Object.class));
        System.out.println("✅ TC_RC_002 - Empty List Response:\n" + prettyJson);

        verify(roomService, times(1)).findAll();
    }

    /**
     * TC_RC_003: Ensure that unauthorized user cannot access the endpoint
     *
     * Input: No authority/permission
     * Expected Output:
     * - HTTP Status 403 (Forbidden)
     */
    @Test
    @WithMockUser(username = "user123", authorities = {"Permissions.Other.READ"})
    void findAll_unauthorizedUser_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/room")
                        .accept("application/json"))
                .andExpect(status().isForbidden());

        verify(roomService, never()).findAll();
    }

    /**
     * TC_RC_004: Ensure proper handling when service throws exception
     *
     * Input: roomService.findAll() throws RuntimeException
     * Expected Output:
     * - The controller should throw a ServletException wrapping the RuntimeException
     * - The roomService.findAll() method is called exactly once
     */
    @Test
    @WithMockUser(username = "user123", authorities = {"Permissions.Room.READ"})
    void findAll_serviceThrowsException_returnServerError() throws Exception {
        when(roomService.findAll()).thenThrow(new RuntimeException("Database connection error"));

        try {
            mockMvc.perform(get("/api/admin/room")
                            .header("Authorization", "Bearer test-token-user123")
                            .accept("application/json"))
                    .andReturn();
            fail("Expected ServletException was not thrown");
        } catch (ServletException ex) {
            System.out.println("❌ TC_RC_004 - Exception Thrown:");
            System.out.println("Exception Class: " + ex.getClass().getName());
            System.out.println("Message: " + ex.getMessage());
            System.out.println("Cause: " + ex.getCause());

            assertTrue(ex.getCause() instanceof RuntimeException);
            assertEquals("Database connection error", ex.getCause().getMessage());
        }

        verify(roomService, times(1)).findAll();
    }
    /**
     * TC_RC_005: Ensure that findById() method returns correct RoomDto
     *
     * Input: Simulated request to GET /api/admin/room/1 with proper authorization
     * - User: user123
     * - Authority: Permissions.Room.READ
     *
     * Expected Output:
     * - HTTP Status 200 (OK)
     * - JSON response
     * - The roomService.findById() method is called exactly once
     */
    @Test
    @WithMockUser(username = "user123", authorities = {"Permissions.Room.READ"})
    void findById_returnSuccess() throws Exception {
        // Set up a mock RoomDto with ID "1"
        RoomDto roomDto = new RoomDto();
        roomDto.setId("1");
        roomDto.setName("Room 1");

        // Mock the roomService call to return the room with ID "1"
        when(roomService.findById("1")).thenReturn(roomDto);

        MvcResult result = mockMvc.perform(get("/api/admin/room/1")
                        .header("Authorization", "Bearer test-token-user123")
                        .accept("application/json"))
                .andExpect(status().isOk()) // Check HTTP 200 OK
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.name").value("Room 1"))
                .andReturn();

        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readValue(result.getResponse().getContentAsString(), Object.class));
        System.out.println("🔍 Response Body:\n" + prettyJson);

        // Verify that the findById method of RoomService was called once
        verify(roomService, times(1)).findById("1");
    }
    /**
     * TC_RC_006: Ensure that unauthorized user cannot access findById() method
     *
     * Input: Simulated request to GET /api/admin/room/1 without required authorization
     * - User: user123
     * - Authority: Permissions.Other.READ
     *
     * Expected Output:
     * - HTTP Status 403 (Forbidden)
     * - The roomService.findById() method is never called
     */
    @Test
    @WithMockUser(username = "user123", authorities = {"Permissions.Other.READ"})
    void findById_unauthorizedUser_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/room/1")
                        .header("Authorization", "Bearer test-token-user123")
                        .accept("application/json"))
                .andExpect(status().isForbidden());

        verify(roomService, never()).findById("1");
    }
    /**
     * TC_RC_007: Ensure that create() method creates a new RoomDto
     *
     * Input: Simulated request to POST /api/admin/room with proper authorization and RoomDto
     * - User: user123
     * - Authority: Permissions.Room.WRITE
     * - RoomDto: {"name": "Room 1"}
     *
     * Expected Output:
     * - HTTP Status 200 (OK)
     * - JSON response
     * - The roomService.createRoom() method is called exactly once
     */
    @Test
    @WithMockUser(username = "user123", authorities = {"Permissions.Room.WRITE"})
    void create_returnSuccess() throws Exception {
        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setId("D001");
        departmentDto.setName("Computer Science");
        departmentDto.setDescription("Test");

        RoomDto roomDto = new RoomDto();
        roomDto.setName("Room 1");
        roomDto.setDepartment(departmentDto);

        when(roomService.createRoom(roomDto)).thenReturn(roomDto);

        MvcResult result = mockMvc.perform(post("/api/admin/room")
                        .header("Authorization", "Bearer test-token-user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Object json = objectMapper.readValue(responseBody, Object.class);
        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

        System.out.println("✅ TC_RC_002 - Response:\n" + prettyJson);
    }
    /**
     * TC_RC_008: Ensure that unauthorized user cannot access create() method
     *
     * Input: Simulated request to POST /api/admin/room without required authorization
     * - User: user123
     * - Authority: Permissions.Room.READ
     * - RoomDto: {"name": "Room 1"}
     *
     * Expected Output:
     * - HTTP Status 403 (Forbidden)
     * - The roomService.createRoom() method is never called
     */
    @Test
    @WithMockUser(username = "user123", authorities = {"Permissions.Room.READ"})
    void create_unauthorizedUser_forbidden() throws Exception {
        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setId("D001");
        departmentDto.setName("Computer Science");
        departmentDto.setDescription("Test");

        RoomDto roomDto = new RoomDto();
        roomDto.setName("Room 1");
        roomDto.setDepartment(departmentDto);

        mockMvc.perform(post("/api/admin/room")
                        .header("Authorization", "Bearer test-token-user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomDto)))
                .andExpect(status().isForbidden());

        verify(roomService, never()).createRoom(any(RoomDto.class));
    }


    /**
     * TC_RC_009: Ensure that update() method updates an existing RoomDto
     *
     * Input: Simulated request to PUT /api/admin/room/1 with proper authorization and updated RoomDto
     * - User: user123
     * - Authority: Permissions.Room.WRITE
     * - RoomDto: {"id": "1", "name": "Updated Room 1"}
     *
     * Expected Output:
     * - HTTP Status 200 (OK)
     * - JSON response
     * - The roomService.updateRoom() method is called exactly once
     */
    @Test
    @WithMockUser(username = "user123", authorities = {"Permissions.Room.WRITE"})
    void update_returnSuccess() throws Exception {
        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setId("D001");
        departmentDto.setName("Computer Science");
        departmentDto.setDescription("Test");

        RoomDto roomDto = new RoomDto();
        roomDto.setId("1");
        roomDto.setName("Updated Room 1");
        roomDto.setDepartment(departmentDto);

        when(roomService.updateRoom(roomDto)).thenReturn(roomDto);

        MvcResult result = mockMvc.perform(put("/api/admin/room/1")
                        .header("Authorization", "Bearer test-token-user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Object json = objectMapper.readValue(responseBody, Object.class);
        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

        System.out.println("✅ TC_RC_009 - Response:\n" + prettyJson);

    }


    /**
     * TC_RC_010: Ensure that unauthorized user cannot access update() method
     *
     * Input: Simulated request to PUT /api/admin/room/1 without required authorization
     * - User: user123
     * - Authority: Permissions.Room.READ
     * - RoomDto: {"id": "1", "name": "Updated Room 1"}
     *
     * Expected Output:
     * - HTTP Status 403 (Forbidden)
     * - The roomService.updateRoom() method is never called
     */
    @Test
    @WithMockUser(username = "user123", authorities = {"Permissions.Room.READ"})
    void update_unauthorizedUser_forbidden() throws Exception {
        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setId("D001");
        departmentDto.setName("Computer Science");
        departmentDto.setDescription("Test");

        RoomDto roomDto = new RoomDto();
        roomDto.setId("1");
        roomDto.setName("Updated Room 1");
        roomDto.setDepartment(departmentDto);

        mockMvc.perform(put("/api/admin/room/1")
                        .header("Authorization", "Bearer test-token-user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomDto)))
                .andExpect(status().isForbidden());


        verify(roomService, never()).updateRoom(any(RoomDto.class));
    }

    /**
     * TC_RC_011: Ensure that delete() method deletes a Room
     *
     * Input: Simulated request to DELETE /api/admin/room with proper authorization
     * - User: user123
     * - Authority: Permissions.Room.WRITE
     * - RoomId: 1
     *
     * Expected Output:
     * - HTTP Status 200 (OK)
     * - JSON response
     * - The roomService.deleteRoom() method is called exactly once
     */
    @Test
    @WithMockUser(username = "user123", authorities = {"Permissions.Room.WRITE"})
    void delete_returnSuccess() throws Exception {
        String roomId = "1";  // Room ID to be deleted

        doNothing().when(roomService).deleteRoom(roomId);

        MvcResult result = mockMvc.perform(delete("/api/admin/room")
                        .header("Authorization", "Bearer test-token-user123")
                        .param("roomId", roomId))  // Passing roomId as query parameter
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"))
                .andReturn();  // Capture the result

        // Convert the response to pretty JSON and print it
        String responseBody = result.getResponse().getContentAsString();
        Object json = objectMapper.readValue(responseBody, Object.class);
        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

        System.out.println("✅ TC_RC_011 - Response:\n" + prettyJson);  // Print the formatted response

        verify(roomService, times(1)).deleteRoom(roomId);  // Ensure deleteRoom is called exactly once
    }



    /**
     * TC_RC_012: Ensure that unauthorized user cannot access delete() method
     *
     * Input: Simulated request to DELETE /api/admin/room without required authorization
     * - User: user123
     * - Authority: Permissions.Room.READ
     *
     * Expected Output:
     * - HTTP Status 403 (Forbidden)
     * - The roomService.deleteRoom() method is never called
     */
    @Test
    @WithMockUser(username = "user123", authorities = {"Permissions.Room.READ"})
    void delete_unauthorizedUser_forbidden() throws Exception {
        String roomId = "1";  // Room ID to be deleted

        MvcResult result = mockMvc.perform(delete("/api/admin/room")
                        .header("Authorization", "Bearer test-token-user123")
                        .param("roomId", roomId))  // Passing roomId as query parameter
                .andExpect(status().isForbidden())  // Ensure status is Forbidden (403)
                .andReturn();  // Capture the result

        // Convert the response to pretty JSON and print it
        String responseBody = result.getResponse().getContentAsString();
        Object json = objectMapper.readValue(responseBody, Object.class);
        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

        System.out.println("❌ TC_RC_012 - Response:\n" + prettyJson);  // Print the formatted response

        verify(roomService, never()).deleteRoom(any(String.class));  // Ensure deleteRoom is never called
    }


}
