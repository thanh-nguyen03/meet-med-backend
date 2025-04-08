package com.thanhnd.clinic_application.modules.notifications;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okta.commons.lang.Assert;
import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.NotificationStatus;
import com.thanhnd.clinic_application.modules.appointments.service.AppointmentReminderService;
import com.thanhnd.clinic_application.modules.notifications.controller.NotificationController;
import com.thanhnd.clinic_application.modules.notifications.dto.NotificationDto;
import com.thanhnd.clinic_application.modules.notifications.service.NotificationService;
import com.thanhnd.clinic_application.modules.shifts.service.RegisteredShiftReminderService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest // This will load the entire application context
@AutoConfigureMockMvc // Automatically configure MockMvc
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;  // Inject MockMvc for API testing

    @MockBean
    private JwtDecoder jwtDecoder; // Mock JwtDecoder to decode the JWT

    @MockBean
    private NotificationService notificationService; // Mock NotificationService

    @MockBean
    private AppointmentReminderService appointmentReminderService;

    @MockBean
    private RegisteredShiftReminderService registeredShiftReminderService;
    @InjectMocks
    private NotificationController notificationController; // Inject the controller being tested

    @Autowired
    private ObjectMapper objectMapper; // ObjectMapper for JSON processing

    // Method to generate a valid JWT token using Spring Security's Jwt
    private Jwt createJwtToken(String userId) {
        Map<String, Object> headers = Map.of("alg", "HS256", "typ", "JWT");

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userId);
        userInfo.put("permissions", List.of("Permissions.Room.READ")); // Add permissions here

        Map<String, Object> claims = new HashMap<>();
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

    // Setup for each test
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock the notificationService to return mock data
        String notificationId = "notif789";
        String fakeUserId = "user123"; // Simulate a logged-in user

        NotificationDto mockDto = new NotificationDto();
        mockDto.setId(notificationId);
        mockDto.setReceiverId(fakeUserId);
        mockDto.setTitle("Reminder");
        mockDto.setContent("Don't forget your appointment!");

        when(notificationService.getNotification(fakeUserId, notificationId)).thenReturn(mockDto);

        // Mock the JWT decoding process
        Jwt mockJwt = createJwtToken("user123");
        when(jwtDecoder.decode("test-token-user123")).thenReturn(mockJwt);

//        // Manually authenticate the user with the role based on decoded JWT
//        UsernamePasswordAuthenticationToken authenticationToken =
//                new UsernamePasswordAuthenticationToken("user123", null, List.of(new SimpleGrantedAuthority("Permissions.Room.READ")));
//        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    /**
     * ✅ TC_NC_001: Successful fetch - Valid request returns NotificationDto
     *
     * Input:
     * - Simulated GET request to /api/notification/{notificationId} with Authorization header
     * - User: user123
     * - Authorization Token: Bearer {generated-token}
     *
     * Expected Output:
     * - HTTP Status 200 (OK)
     * - JSON response with NotificationDto data
     * - Notification ID, Receiver ID, Title, and Content should match expected values
     */

    @Test
    void getNotification_validRequest_returnsNotificationDto() throws Exception {
        String notificationId = "notif789";
        String fakeUserId = "user123"; // Simulate a logged-in user

        // Prepare mock data for NotificationDto
        NotificationDto mockDto = new NotificationDto();
        mockDto.setId(notificationId);
        mockDto.setReceiverId(fakeUserId);
        mockDto.setTitle("Reminder");
        mockDto.setContent("Don't forget your appointment!");

        // Generate the valid JWT token for the user
        String token = createJwtToken(fakeUserId).getTokenValue();  // Extract token value
        System.out.println("Generated Token: " + token);  // Print the token to verify it

        // Build URL for the API endpoint
        String url = "/api/notification/" + notificationId;

        // Command Output: Simulating API Request
        System.out.println("Performing GET request to URL: " + url);
        System.out.println("Authorization: Bearer " + token);

        // Perform the actual API call using MockMvc and simulate an authenticated request
        MvcResult result = mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + token) // Use the generated JWT token in the Authorization header
                )
                .andExpect(MockMvcResultMatchers.status().isOk()) // Expect HTTP status 200 (OK)
                .andExpect(jsonPath("$.data.id").value(notificationId)) // Verify Notification ID
                .andExpect(jsonPath("$.data.receiverId").value(fakeUserId)) // Verify Receiver ID
                .andExpect(jsonPath("$.data.title").value("Reminder")) // Verify Title
                .andExpect(jsonPath("$.data.content").value("Don't forget your appointment!")) // Verify Content
                .andReturn();

        // Print the complete response content for inspection
        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Response Content: " + responseContent);  // Full response output

        // Optionally, you can parse the response content manually if you want to extract the data
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseJson = objectMapper.readTree(responseContent);

        // Extract individual fields from the response JSON and print them
        String id = responseJson.at("/data/id").asText();
        System.out.println("Notification ID: " + id);

        String receiverId = responseJson.at("/data/receiverId").asText();
        System.out.println("Receiver ID: " + receiverId);

        String title = responseJson.at("/data/title").asText();
        System.out.println("Title: " + title);

        String content = responseJson.at("/data/content").asText();
        System.out.println("Content: " + content);

        // Validate response contents with custom assertions and print them
        assertEquals(notificationId, id);
        assertEquals(fakeUserId, receiverId);
        assertEquals("Reminder", title);
        assertEquals("Don't forget your appointment!", content);

        // Additional Command Output: Assertion results
        System.out.println("Assertions passed for Notification ID, Receiver ID, Title, and Content.");
    }

    /**
     * ✅ TC_NC_002: Invalid Notification ID - Notification doesn't exist
     *
     * Input:
     * - Simulated GET request to /api/notification/{non-existent-id} with Authorization header
     * - User: user123
     * - Authorization Token: Bearer {generated-token}
     *
     * Expected Output:
     * - HTTP Status 404 (Not Found)
     * - JSON response with an error message
     */
    @Test
    void getNotification_invalidNotificationId_returnsNotFound() throws Exception {
        String notificationId = "invalidNotifId";
        String fakeUserId = "user123"; // Simulate a logged-in user

        // Generate the valid JWT token for the user
        String token = createJwtToken(fakeUserId).getTokenValue();  // Extract token value

        // Build URL for the API endpoint
        String url = "/api/notification/" + notificationId;

        // Command Output: Simulating API Request
        System.out.println("Performing GET request to URL: " + url);
        System.out.println("Authorization: Bearer " + token);

        // Perform the actual API call using MockMvc and simulate an authenticated request
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + token) // Use the generated JWT token in the Authorization header
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound()) // Expect HTTP status 404 (Not Found)
                .andExpect(jsonPath("$.error").value("Notification not found")) // Verify error message
                .andReturn();
    }

    /**
     * ✅ TC_NC_003: Unauthorized Access - Invalid or Missing JWT Token
     *
     * Input:
     * - Simulated GET request to /api/notification/{notificationId} with no Authorization header or invalid token
     * - User: None (No JWT token)
     *
     * Expected Output:
     * - HTTP Status 401 (Unauthorized)
     */
    @Test
    void getNotification_missingOrInvalidToken_returnsUnauthorized() throws Exception {
        String notificationId = "notif789";

        // Build URL for the API endpoint
        String url = "/api/notification/" + notificationId;

        // Command Output: Simulating API Request
        System.out.println("Performing GET request to URL: " + url);

        // Perform the actual API call using MockMvc without a token
        MvcResult result = mockMvc.perform(get(url))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized()) // Expect HTTP status 401 (Unauthorized)
                .andReturn();

        // Print the status code and the response body for inspection
        int statusCode = result.getResponse().getStatus();
        System.out.println("Response Status Code: " + statusCode);  // Output the status code

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Response Content: " + responseContent);  // Output the response content (should be empty or null)

        // Optionally, check the headers
        String contentType = result.getResponse().getHeader("Content-Type");
        System.out.println("Content-Type Header: " + contentType);  // Print the Content-Type header (should be null or some default)
    }

    /**
     * TC_NC_004: Ensure that getAllNotifications() returns notifications with correct pagination and sorting
     *
     * Input:
     * - HTTP GET /api/notifications?page=1&size=2&orderBy=createdAt&order=desc
     * - User ID: user123 (mocked from JWT)
     *
     * Expected Output:
     * - HTTP 200 OK
     * - JSON response with statusCode = 200
     * - Success = true
     * - Correct list of notifications in `data`
     * - NotificationService.getNotifications() called with correct args
     */
    @Test
    void getAllNotifications_returnSuccess_withPaginationAndSorting() throws Exception {
        String fakeUserId = "user123";
        // Generate the valid JWT token for the user
        String token = createJwtToken(fakeUserId).getTokenValue();  // Extract token value
        System.out.println("Generated Token: " + token);  // Print the token to verify it

        // Arrange - prepare mock notifications
        NotificationDto notification1 = new NotificationDto();
        notification1.setId("1");
        notification1.setTitle("Notification 1");
        notification1.setContent("Content 1");
        notification1.setReceiverId("user123");

        NotificationDto notification2 = new NotificationDto();
        notification2.setId("2");
        notification2.setTitle("Notification 2");
        notification2.setContent("Content 2");
        notification2.setReceiverId("user123");

        List<NotificationDto> notificationList = List.of(notification1, notification2);

        // Create a Page of NotificationDto
        Page<NotificationDto> notificationPage = new PageImpl<>(notificationList, PageRequest.of(0, 10), 2);

        // Mock the service
        when(notificationService.getNotifications(eq("user123"), any(Pageable.class)))
                .thenReturn(PageableResultDto.parse(notificationPage));

        // Act & Assert
        mockMvc.perform(get("/api/notification")
                        .param("receiverId", "user123")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].title").value("Notification 1"))
                .andExpect(jsonPath("$.data.content[1].title").value("Notification 2"))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.success").value(true));
    }
    /**
     * TC_NC_005: Ensure that getAllNotifications() returns 401 when JWT token is missing
     *
     * Input:
     * - HTTP GET /api/notification?page=0&size=10
     * - No Authorization header
     *
     * Expected Output:
     * - HTTP 401 Unauthorized
     * - JSON response with statusCode = 401
     * - Success = false
     * - Error message indicating missing authentication
     */
    @Test
    void getAllNotifications_missingToken_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/notification")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }
    /**
     * TC_NC_006: Ensure that getAllNotifications() returns 400 when pagination parameters are invalid
     *
     * Input:
     * - HTTP GET /api/notification?page=-1&size=0
     * - Authorization header with valid token
     *
     * Expected Output:
     * - HTTP 400 Bad Request
     * - JSON response with statusCode = 400
     * - Success = false
     * - Error message indicating invalid parameters
     */
    @Test
    void getAllNotifications_invalidPagination_shouldReturnBadRequest() throws Exception {
        String token = createJwtToken("user123").getTokenValue();

        mockMvc.perform(get("/api/notification")
                        .param("page", "-1")
                        .param("size", "0")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(400));
    }
    /**
     * TC_NC_007: Ensure that getAllNotifications() returns empty data when no notifications exist
     *
     * Input:
     * - HTTP GET /api/notification?page=0&size=10
     * - User ID: user999 (mocked from JWT, has no notifications)
     *
     * Expected Output:
     * - HTTP 200 OK
     * - JSON response with statusCode = 200
     * - Success = true
     * - Empty content list
     */
    @Test
    void getAllNotifications_noNotifications_shouldReturnEmptyContent() throws Exception {
        String fakeUserId = "user123";
        // Generate the valid JWT token for the user
        String token = createJwtToken(fakeUserId).getTokenValue();  // Extract token value
        System.out.println("Generated Token: " + token);  // Print the token to verify it
//
//        String token = createJwtToken("user999").getTokenValue();

        Page<NotificationDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(notificationService.getNotifications(eq(fakeUserId), any(Pageable.class)))
                .thenReturn(PageableResultDto.parse(emptyPage));

        mockMvc.perform(get("/api/notification")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }
    /**
     * TC_NC_008: Ensure that getAllNotifications() handles invalid sort field or order gracefully
     *
     * Input:
     * - HTTP GET /api/notification?page=0&size=10&orderBy=invalidField&order=invalidOrder
     * - User ID: user123 (mocked from JWT)
     *
     * Expected Output:
     * - HTTP 400 Bad Request or server-defined error handling
     * - JSON response with statusCode = 400 or 500
     * - Success = false
     */
    @Test
    void getAllNotifications_invalidSortingParams_shouldHandleGracefully() throws Exception {
        String token = createJwtToken("user123").getTokenValue();

        mockMvc.perform(get("/api/notification")
                        .param("page", "0")
                        .param("size", "10")
                        .param("orderBy", "invalidField")
                        .param("order", "invalidOrder")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest()) // or is5xxServerError depending on implementation
                .andExpect(jsonPath("$.success").value(false));
    }
    /**
     * TC_NC_009: Ensure that getAllNotifications() supports custom page size and page number
     *
     * Input:
     * - HTTP GET /api/notification?page=2&size=5
     * - User ID: user123 (mocked from JWT)
     *
     * Expected Output:
     * - HTTP 200 OK
     * - JSON response with correct pagination metadata
     * - Success = true
     */
    @Test
    void getAllNotifications_customPagination_shouldReturnExpectedPage() throws Exception {
        String token = createJwtToken("user123").getTokenValue();

        List<NotificationDto> pageContent = List.of(new NotificationDto(), new NotificationDto(), new NotificationDto());
        Page<NotificationDto> customPage = new PageImpl<>(pageContent, PageRequest.of(2, 5), 15);
        when(notificationService.getNotifications(eq("user123"), any(Pageable.class)))
                .thenReturn(PageableResultDto.parse(customPage));

        mockMvc.perform(get("/api/notification")
                        .param("page", "2")
                        .param("size", "5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(15))
                .andExpect(jsonPath("$.data.totalPage").value(3));
    }

    /**
     * TC_NC_010: Ensure that createAppointmentNotification() works with a valid appointment ID and JWT authentication
     *
     * Input:
     * - HTTP POST /api/notification/appointment/apt123
     * - JWT token for user: user123
     *
     * Expected Output:
     * - HTTP 200 OK
     * - JSON response: success = true
     */
    @Test
    void createAppointmentNotification_validIdWithJwt_shouldReturnSuccess() throws Exception {
        String appointmentId = "apt123";
        String token = createJwtToken("user123").getTokenValue();

        doNothing().when(appointmentReminderService).sendMockReminder(eq(appointmentId));

        mockMvc.perform(post("/api/notification/appointment/{appointmentId}", appointmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }



    /**
     * TC_NC_011: Ensure that createAppointmentNotification() handles internal service exceptions properly
     *
     * Input:
     * - HTTP POST /api/notification/appointment/apt123
     *
     * Expected Output:
     * - HTTP 500 Internal Server Error
     */
    @Test
    void createAppointmentNotification_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        String appointmentId = "apt123";
        String token = createJwtToken("user123").getTokenValue();

        doThrow(new RuntimeException("Service error"))
                .when(appointmentReminderService).sendMockReminder(eq(appointmentId));

        mockMvc.perform(post("/api/notification/appointment/{appointmentId}", appointmentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))

                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

        /**
         * TC_NC_012: Ensure that createAppointmentNotification() handles blank appointment ID
         *
         * Input:
         * - HTTP POST /api/notification/appointment/ (with blank ID " ")
         *
         * Expected Output:
         * - HTTP 400 Bad Request
         */
    @Test
    void createAppointmentNotification_blankId_shouldReturnBadRequest() throws Exception {
        String token = createJwtToken("user123").getTokenValue();
        String appointmentId = " ";
        mockMvc.perform(post("/api/notification/appointment/{appointmentId}", appointmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TC_NC_013: Ensure that createAppointmentNotification() returns 401 when JWT is missing
     *
     * Input:
     * - HTTP POST /api/notification/appointment/apt123
     * - No JWT token
     *
     * Expected Output:
     * - HTTP 401 Unauthorized
     */
    @Test
    void createAppointmentNotification_missingJwt_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/notification/appointment/{appointmentId}", "apt123"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    /**
     * TC_NC_014: Ensure that createRegisteredShiftNotification() triggers reminder service correctly
     *
     * Input:
     * - HTTP POST /api/notification/registerd-shift/shift456
     * - registeredShiftId: "shift456"
     * - Valid JWT token with userId
     *
     * Expected Output:
     * - HTTP 200 OK
     * - JSON response with statusCode = 200
     * - success = true
     * - RegisteredShiftReminderService.sendMockReminder() is called exactly once with "shift456"
     */
    @Test
    void createRegisteredShiftNotification_success() throws Exception {
        String fakeUserId = "user123";
        String registeredShiftId = "shift456";
        String token = createJwtToken(fakeUserId).getTokenValue();

        // No need to mock return value because method returns void and just calls service

        // Act & Assert
        mockMvc.perform(post("/api/notification/registerd-shift/{registeredShiftId}", registeredShiftId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true));

        // Verify service call
        verify(registeredShiftReminderService, times(1)).sendMockReminder(eq(registeredShiftId));
    }

    /**
     * TC_NC_015: Ensure that missing registeredShiftId returns 404 Not Found
     *
     * Input:
     * - HTTP POST /api/notification/registerd-shift/
     *
     * Expected Output:
     * - HTTP 404 Not Found
     */
    @Test
    void createRegisteredShiftNotification_missingPathVariable_returnsNotFound() throws Exception {
        String fakeUserId = "user123";
        String token = createJwtToken(fakeUserId).getTokenValue();

        mockMvc.perform(post("/api/notification/registerd-shift/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    /**
     * TC_NC_016: Ensure that empty registeredShiftId returns 400 Bad Request or handled safely
     *
     * Input:
     * - HTTP POST /api/notification/registerd-shift/ (with empty or space)
     *
     * Expected Output:
     * - HTTP 400 Bad Request (or handled by validation)
     */
    @Test
    void createRegisteredShiftNotification_emptyShiftId_returnsBadRequest() throws Exception {
        String fakeUserId = "user123";
        String token = createJwtToken(fakeUserId).getTokenValue();

        String emptyShiftId = " ";
        mockMvc.perform(post("/api/notification/registerd-shift/{registeredShiftId}", emptyShiftId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest()); // or is4xxClientError()
    }

    /**
     * TC_NC_017: Ensure unauthorized access returns 401 when JWT token is missing
     *
     * Input:
     * - HTTP POST /api/notification/registerd-shift/shift456
     * - No Authorization header
     *
     * Expected Output:
     * - HTTP 401 Unauthorized
     */
    @Test
    void createRegisteredShiftNotification_noToken_returnsUnauthorized() throws Exception {

        mockMvc.perform(post("/api/notification/registerd-shift/{registeredShiftId}", "shift456"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TC_NC_018: Ensure controller handles exception from service and returns 500
     *
     * Input:
     * - HTTP POST /api/notification/registerd-shift/shift999
     * - Service throws RuntimeException
     *
     * Expected Output:
     * - HTTP 500 Internal Server Error
     * - Proper error structure (if handled globally)
     */
    @Test
    void createRegisteredShiftNotification_serviceThrowsException_returnsInternalServerError() throws Exception {
        String shiftId = "shift999";
        String token = createJwtToken("user123").getTokenValue();

        doThrow(new RuntimeException("Mock failure")).when(registeredShiftReminderService).sendMockReminder(shiftId);

        mockMvc.perform(post("/api/notification/registerd-shift/{registeredShiftId}", shiftId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isInternalServerError()); // Or .is5xxServerError()
    }

    /**
     * TC_NC_019: Ensure that updateNotificationStatus() updates notification to READ status successfully
     *
     * Input:
     * - HTTP PUT /api/notification/{notificationId}/read
     * - Notification ID: notif123
     * - User ID: user123 (mocked from JWT)
     *
     * Expected Output:
     * - HTTP 200 OK
     * - JSON response with statusCode = 200
     * - Success = true
     */
    @Test
    void updateNotificationStatus_success_shouldReturnOk() throws Exception {
        String fakeUserId = "user123";
        String notificationId = "notif123";
        String token = createJwtToken(fakeUserId).getTokenValue();

        doNothing().when(notificationService)
                .updateStatus(eq(fakeUserId), eq(notificationId), eq(NotificationStatus.READ));

        mockMvc.perform(put("/api/notification/{notificationId}/read", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).updateStatus(fakeUserId, notificationId, NotificationStatus.READ);
    }

    /**
     * TC_NC_020: updateNotificationStatus() returns 401 when no Authorization header is present
     *
     * Input:
     * - HTTP PUT /api/notification/{notificationId}/read
     * - No Authorization header
     *
     * Expected Output:
     * - HTTP 401 UNAUTHORIZED
     */
    @Test
    void updateNotificationStatus_noAuthHeader_shouldReturn401() throws Exception {
        mockMvc.perform(put("/api/notification/notif123/read"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TC_NC_021: updateNotificationStatus() returns 400 when notification ID is invalid
     *
     * Input:
     * - HTTP PUT /api/notification//read (empty ID)
     *
     * Expected Output:
     * - HTTP 400 BAD REQUEST
     */
    @Test
    void updateNotificationStatus_emptyNotificationId_shouldReturn400() throws Exception {
        String userId = "user123";
        String token = createJwtToken(userId).getTokenValue();

        String invalidNotificationId = " "; // simulate invalid input

        doThrow(new BadRequestException("Notification ID is invalid"))
                .when(notificationService).updateStatus(eq(userId), eq(invalidNotificationId.trim()), eq(NotificationStatus.READ));

        mockMvc.perform(put("/api/notification/{notificationId}/read", invalidNotificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Notification ID is invalid"));
    }

    /**
     * TC_NC_022: updateAllNotificationStatus() marks all notifications as READ for authenticated user
     *
     * Input:
     * - HTTP PUT /api/notification/read-all
     * - Valid JWT Token for user123
     *
     * Expected Output:
     * - HTTP 200 OK
     * - JSON response with statusCode = 200
     * - success = true
     */
    @Test
    void updateAllNotificationStatus_validUser_shouldReturnSuccess() throws Exception {
        String userId = "user123";
        String token = createJwtToken(userId).getTokenValue();

        // No return value expected, just verify interaction
        doNothing().when(notificationService).updateAllStatus(eq(userId), eq(NotificationStatus.READ));

        mockMvc.perform(put("/api/notification/read-all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true));

        // Verify service interaction
        verify(notificationService, times(1)).updateAllStatus(userId, NotificationStatus.READ);
    }

    /**
     * TC_NC_023: updateAllNotificationStatus() returns 401 when token is missing
     *
     * Input:
     * - HTTP PUT /api/notification/read-all
     * - No Authorization header
     *
     * Expected Output:
     * - HTTP 401 UNAUTHORIZED
     */
    @Test
    void updateAllNotificationStatus_missingToken_shouldReturn401() throws Exception {
        mockMvc.perform(put("/api/notification/read-all"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TC_NC_024: updateAllNotificationStatus() returns 500 when service throws unexpected exception
     *
     * Input:
     * - HTTP PUT /api/notification/read-all
     * - Valid JWT Token
     * - Service throws RuntimeException
     *
     * Expected Output:
     * - HTTP 500 INTERNAL SERVER ERROR
     */
    @Test
    void updateAllNotificationStatus_serviceThrowsException_shouldReturn500() throws Exception {
        String userId = "user123";
        String token = createJwtToken(userId).getTokenValue();

        doThrow(new RuntimeException("Unexpected error"))
                .when(notificationService).updateAllStatus(eq(userId), eq(NotificationStatus.READ));

        mockMvc.perform(put("/api/notification/read-all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isInternalServerError());
    }

    /**
     * TC_NC_025: getSocketIONameRooms() returns the list of Socket.IO room names for the authenticated user
     *
     * Input:
     * - HTTP GET /api/notification/socket-io-name-rooms
     * - Valid JWT Token for user123
     *
     * Expected Output:
     * - HTTP 200 OK
     * - JSON response with statusCode = 200
     * - success = true
     * - data contains the expected list of room names
     */
    @Test
    void getSocketIONameRooms_shouldReturnListOfRoomNames() throws Exception {
        String userId = "user123";
        String token = createJwtToken(userId).getTokenValue();
        List<String> rooms = List.of("room1", "room2", "room3");

        when(notificationService.getSocketIONameRooms(userId)).thenReturn(rooms);

        mockMvc.perform(get("/api/notification/socket-io-name-rooms")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0]").value("room1"))
                .andExpect(jsonPath("$.data[1]").value("room2"))
                .andExpect(jsonPath("$.data[2]").value("room3"));
    }

    /**
     * TC_NC_026: getSocketIONameRooms() returns empty list when user has no rooms
     *
     * Input:
     * - HTTP GET /api/notification/socket-io-name-rooms
     * - Valid JWT Token for user123
     *
     * Expected Output:
     * - HTTP 200 OK
     * - JSON response with statusCode = 200
     * - success = true
     * - data is an empty list
     */
    @Test
    void getSocketIONameRooms_noRooms_shouldReturnEmptyList() throws Exception {
        String userId = "user123";
        String token = createJwtToken(userId).getTokenValue();

        when(notificationService.getSocketIONameRooms(userId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/notification/socket-io-name-rooms")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
    /**
     * TC_NC_027: getSocketIONameRooms() returns 500 when service throws exception
     *
     * Input:
     * - HTTP GET /api/notification/socket-io-name-rooms
     * - Valid JWT Token for user123
     *
     * Expected Output:
     * - HTTP 500 INTERNAL SERVER ERROR (or your custom error handling response)
     * - JSON response with success = false
     */
    @Test
    void getSocketIONameRooms_serviceThrowsException_shouldReturn500() throws Exception {
        String userId = "user123";
        String token = createJwtToken(userId).getTokenValue();

        when(notificationService.getSocketIONameRooms(userId))
                .thenThrow(new RuntimeException("Simulated service failure"));

        mockMvc.perform(get("/api/notification/socket-io-name-rooms")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    /**
     * TC_NC_028: getSocketIONameRooms() returns 401 when token is missing
     *
     * Input:
     * - HTTP GET /api/notification/socket-io-name-rooms
     * - No Authorization header
     *
     * Expected Output:
     * - HTTP 401 UNAUTHORIZED
     */
    @Test
    void getSocketIONameRooms_missingToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/notification/socket-io-name-rooms"))
                .andExpect(status().isUnauthorized());
    }


}
