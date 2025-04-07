package com.thanhnd.clinic_application.modules.notifications;

import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.constants.NotificationStatus;
import com.thanhnd.clinic_application.constants.NotificationType;
import com.thanhnd.clinic_application.entity.Notification;
import com.thanhnd.clinic_application.mapper.NotificationMapper;
import com.thanhnd.clinic_application.mapper.RoomMapper;
import com.thanhnd.clinic_application.modules.notifications.dto.NotificationDto;
import com.thanhnd.clinic_application.modules.notifications.repository.NotificationRepository;
import com.thanhnd.clinic_application.modules.notifications.service.NotificationService;
import com.thanhnd.clinic_application.modules.rooms.repository.RoomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Tag("service")
public class NotificationServiceImplTest {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationMapper notificationMapper;

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImplTest.class);
    @PersistenceContext
    private EntityManager entityManager;
    /**
     * TC_NS_001: Test sendNotifications() when notifications are successfully created
     * Test Objective: Verify that a list of notifications is created and saved correctly.
     * Input: A list of receiver IDs and a notification DTO with the required fields.
     * Expected Output: A list of saved notifications corresponding to each receiver ID.
     */
    @Test
    @Transactional
    void sendNotifications_success() {
        // Prepare input data: a list of receiver IDs and a notification DTO
        List<String> receiverIds = List.of("user1", "user2", "user3");
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle("Appointment Reminder");
        notificationDto.setContent("This is a reminder for your appointment.");
        notificationDto.setReceiverId("user1");
        notificationDto.setObjectData("Some appointment data");
        notificationDto.setStatus(NotificationStatus.OPEN);
        notificationDto.setType(NotificationType.APPOINTMENT_REMINDER);

        // Print out the receiver IDs to be used in the test
        System.out.println("Receiver IDs: " + receiverIds);

        // Call the service method
        List<Notification> notifications = notificationService.sendNotifications(receiverIds, notificationDto);

        // Check that notifications were saved to the database
        assertNotNull(notifications);
        assertEquals(3, notifications.size());

        // Print the notifications returned by the service method for inspection
        System.out.println("Notifications created:");

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            System.out.println("Notification " + (i + 1) + ": " +
                    "Title=" + notification.getTitle() + ", " +
                    "Content=" + notification.getContent() + ", " +
                    "ReceiverId=" + notification.getReceiverId() + ", " +
                    "ObjectData=" + notification.getObjectData() + ", " +
                    "Status=" + notification.getStatus() + ", " +
                    "Type=" + notification.getType());

            // Validate the notifications for each receiver
            String expectedReceiverId = receiverIds.get(i);  // Correctly get the receiver ID from the list
            assertEquals("Appointment Reminder", notification.getTitle());
            assertEquals("This is a reminder for your appointment.", notification.getContent());
            assertEquals(expectedReceiverId, notification.getReceiverId());  // Check against the actual receiver ID
            assertEquals("Some appointment data", notification.getObjectData());
            assertEquals(NotificationStatus.OPEN, notification.getStatus());
            assertEquals(NotificationType.APPOINTMENT_REMINDER, notification.getType());
        }


    }
    /**
     * TC_NS_002: Test sendNotifications() when receiver list is empty
     * Test Objective: Verify that the service handles empty receiver lists gracefully.
     * Input: An empty list of receiver IDs and a notification DTO.
     * Expected Output: No notifications are created, and the method returns an empty list.
     */
    @Test
    @Transactional
    void sendNotifications_emptyReceiverList() {
        // Prepare input data: an empty list of receiver IDs and a notification DTO
        List<String> receiverIds = List.of();  // Empty list of receiver IDs
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle("Appointment Reminder");
        notificationDto.setContent("This is a reminder for your appointment.");
        notificationDto.setReceiverId("user1");  // This will be ignored since the list is empty
        notificationDto.setObjectData("Some appointment data");
        notificationDto.setStatus(NotificationStatus.OPEN);
        notificationDto.setType(NotificationType.APPOINTMENT_REMINDER);

        // Print out the input data for debugging
        System.out.println("Receiver IDs: " + receiverIds);
        System.out.println("Notification DTO: " + notificationDto);

        // Call the service method with an empty receiver list
        List<Notification> notifications = notificationService.sendNotifications(receiverIds, notificationDto);

        // Print out the notifications returned by the service method for debugging
        System.out.println("Notifications returned: " + notifications);

        // Assert that no notifications were created
        assertNotNull(notifications, "The notifications list should not be null.");
        assertTrue(notifications.isEmpty(), "No notifications should be created for an empty receiver list");

        // Print out a message confirming the assertion
        System.out.println("Test passed: No notifications were created when the receiver list is empty.");
    }


    /**
     * TC_NS_003: Test getNotifications() when notifications exist for a receiver
     * Test Objective: Verify that the service returns notifications for a given receiver.
     * Input: A receiver ID and pageable object.
     * Expected Output: A list of notifications corresponding to the receiver.
     */
    @Test
    @Transactional
    void getNotifications_success() {
        // Prepare input data: a list of receiver IDs and a notification DTO
        List<String> receiverIds = List.of("user1", "user2", "user3");
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle("Appointment Reminder");
        notificationDto.setContent("This is a reminder for your appointment.");
        notificationDto.setReceiverId("user1");
        notificationDto.setObjectData("Some appointment data");
        notificationDto.setStatus(NotificationStatus.OPEN);
        notificationDto.setType(NotificationType.APPOINTMENT_REMINDER);

        // Call the service method to send notifications
        List<Notification> notifications = notificationService.sendNotifications(receiverIds, notificationDto);

        // Check that notifications were saved to the database
        assertNotNull(notifications);
        assertEquals(3, notifications.size(), "Expected 3 notifications to be created.");

        // Print the notifications returned by the service method for inspection
        System.out.println("Notifications created:");

        // Verify that each notification is saved in the database and can be retrieved
        for (int i = 0; i < notifications.size(); i++) {
            String receiverId = receiverIds.get(i);

            // Test pagination using Pageable to fetch notifications by receiver ID
            Pageable pageable = PageRequest.of(0, 2);  // Page 0, with a page size of 2
            Page<Notification> pagedNotifications = notificationRepository.findAllByReceiverId(receiverId, pageable);

            // Assert that the pagination works as expected
            assertNotNull(pagedNotifications);
            assertTrue(pagedNotifications.getContent().size() <= 2, "Page size should be 2 or less");
            System.out.println("Paged Notifications for " + receiverId + ": " + pagedNotifications.getContent());

            // Validate that the saved notification matches the expected values
            for (Notification savedNotification : pagedNotifications.getContent()) {
                assertEquals("Appointment Reminder", savedNotification.getTitle(), "Notification title should match");
                assertEquals("This is a reminder for your appointment.", savedNotification.getContent(), "Notification content should match");
                assertEquals(receiverId, savedNotification.getReceiverId(), "Receiver ID should match");
                assertEquals("Some appointment data", savedNotification.getObjectData(), "Object data should match");
                assertEquals(NotificationStatus.OPEN, savedNotification.getStatus(), "Notification status should be OPEN");
                assertEquals(NotificationType.APPOINTMENT_REMINDER, savedNotification.getType(), "Notification type should match");
            }
        }
    }

    /**
     * TC_NS_004: Test getNotifications() when no notifications exist for a receiver.
     * Test Objective: Ensure that an empty list is returned when there are no notifications.
     * Input: A receiver ID with no notifications and a pageable object.
     * Expected Output: An empty list of notifications.
     */
    @Test
    @Transactional
    void getNotifications_noNotifications() {
        // Prepare a receiver ID that has no notifications
        String receiverId = "nonexistentUser";
        Pageable pageable = PageRequest.of(0, 10);

        // Call the service method
        PageableResultDto<NotificationDto> result = notificationService.getNotifications(receiverId, pageable);

        // Print output for inspection
        System.out.println("=== Test: getNotifications_noNotifications ===");
        System.out.println("Receiver ID: " + receiverId);
        System.out.println("Total elements: " + result.getTotalElements());
        System.out.println("Total pages: " + result.getTotalPage());
        System.out.println("Returned content size: " + result.getContent().size());

        if (result.getContent().isEmpty()) {
            System.out.println("No notifications found for receiver.");
        } else {
            for (NotificationDto dto : result.getContent()) {
                System.out.println("Notification DTO: " + dto.getTitle() + " - " + dto.getContent());
            }
        }

        // Assert that the result is not null and contains no notifications
        assertNotNull(result, "The result should not be null");
        assertTrue(result.getContent().isEmpty(), "Expected no notifications to be returned");
        assertEquals(0, result.getTotalElements(), "Total elements should be 0");
    }

    /**
     * TC_NS_005: Test getNotifications() when receiverId is null.
     * Test Objective: Verify behavior when receiverId is null (if nulls are allowed).
     * Input: Null receiver ID and a pageable object.
     * Expected Output: May throw exception or return empty result based on implementation.
     */
    @Test
    @Transactional
    void getNotifications_nullReceiverId() {
        Pageable pageable = PageRequest.of(0, 10);
        String receiverId = null;

        System.out.println("=== Test: getNotifications_nullReceiverId ===");
        System.out.println("Receiver ID: " + receiverId);
        System.out.println("Calling getNotifications() with null receiverId...");

        PageableResultDto<NotificationDto> result = notificationService.getNotifications(receiverId, pageable);

        System.out.println("Result: " + result);

        assertNotNull(result, "Result should not be null when receiverId is null");
        assertTrue(result.getContent().isEmpty(), "Expected no notifications to be returned");
        assertEquals(0, result.getTotalElements(), "Total elements should be 0");
    }

    /**
     * TC_NS_006: Test getNotifications() with null pageable.
     * Test Objective: Ensure method handles null pageable appropriately by using default paging.
     * Input: Valid receiver ID and null pageable.
     * Expected Output: A result with default pagination applied.
     */
    @Test
    @Transactional
    void getNotifications_nullPageable() {
        String receiverId = "user1";

        System.out.println("=== Test: getNotifications_nullPageable ===");
        System.out.println("Receiver ID: " + receiverId);
        System.out.println("Calling getNotifications() with null pageable...");

        PageableResultDto<NotificationDto> result = notificationService.getNotifications(receiverId, null);

        System.out.println("Result: " + result);

        assertNotNull(result, "Result should not be null when pageable is null");
        // You can further assert defaults like page size/content if known
    }

    /**
     * TC_NS_007: Test getNotifications() with multiple pages.
     * Test Objective: Validate pagination works correctly across multiple pages.
     * Input: Receiver ID with multiple notifications and a small page size.
     * Expected Output: Partial list per page, consistent with pagination logic.
     */
    @Test
    @Transactional
    void getNotifications_paginationMultiplePages() {
        String receiverId = "userMulti";

        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle("Paginated Test");
        notificationDto.setContent("Testing pagination.");
        notificationDto.setReceiverId(receiverId);
        notificationDto.setObjectData("Test data");
        notificationDto.setStatus(NotificationStatus.OPEN);
        notificationDto.setType(NotificationType.APPOINTMENT_REMINDER);

        // Create 5 notifications for the test user
        for (int i = 0; i < 5; i++) {
            notificationService.sendNotifications(List.of(receiverId), notificationDto);
        }

        // === Page 1: Expect 2 notifications ===
        Pageable pageable1 = PageRequest.of(0, 2);
        PageableResultDto<NotificationDto> page1 = notificationService.getNotifications(receiverId, pageable1);
        System.out.println("Page 1: " + page1.getContent().size() + " notifications");
        assertEquals(2, page1.getContent().size(), "TC_NS_007 - Page 1: Expected 2 notifications");

        // === Page 2: Expect 2 notifications ===
        Pageable pageable2 = PageRequest.of(1, 2);
        PageableResultDto<NotificationDto> page2 = notificationService.getNotifications(receiverId, pageable2);
        System.out.println("Page 2: " + page2.getContent().size() + " notifications");
        assertEquals(2, page2.getContent().size(), "TC_NS_007 - Page 2: Expected 2 notifications");

        // === Page 3: Expect 1 notification ===
        Pageable pageable3 = PageRequest.of(2, 2);
        PageableResultDto<NotificationDto> page3 = notificationService.getNotifications(receiverId, pageable3);
        System.out.println("Page 3: " + page3.getContent().size() + " notifications");
        assertEquals(1, page3.getContent().size(), "TC_NS_007 - Page 3: Expected 1 notification");
    }

    /**
     * TC_NS_008: Test getNotification() when notification exists.
     * Test Objective: Ensure it returns the correct NotificationDto.
     * Input: Valid receiver ID and existing notification ID.
     * Expected Output: Matching NotificationDto is returned.
     */
    @Test
    @Transactional
    void getNotification_validId_returnsNotificationDto() {
        String receiverId = "userGet";

        System.out.println("TC_NS_008: Creating notification for receiver ID: " + receiverId);

        // Create and send a notification
        NotificationDto dto = new NotificationDto();
        dto.setTitle("Test Title");
        dto.setContent("Test Content");
        dto.setReceiverId(receiverId);
        dto.setObjectData("Sample Data");
        dto.setStatus(NotificationStatus.OPEN);
        dto.setType(NotificationType.APPOINTMENT_REMINDER);

        notificationService.sendNotifications(List.of(receiverId), dto);

        // Fetch the created notification ID
        Pageable pageable = PageRequest.of(0, 1);
        String notificationId = notificationService
                .getNotifications(receiverId, pageable)
                .getContent()
                .get(0)
                .getId();

        System.out.println("TC_NS_008: Retrieved notification ID: " + notificationId);

        // Call the service method
        NotificationDto result = notificationService.getNotification(receiverId, notificationId);

        System.out.println("TC_NS_008: Fetched NotificationDto:");
        System.out.println("- Title: " + result.getTitle());
        System.out.println("- Content: " + result.getContent());
        System.out.println("- Receiver ID: " + result.getReceiverId());

        // Assertions
        assertNotNull(result);
        assertEquals("Test Title", result.getTitle(), "TC_NS_008: Title should match");
        assertEquals("Test Content", result.getContent(), "TC_NS_008: Content should match");
        assertEquals(receiverId, result.getReceiverId(), "TC_NS_008: Receiver ID should match");
    }

    /**
     * TC_NS_009: Test getNotification() when notification does not exist.
     * Test Objective: Ensure it throws HttpException for non-existent ID.
     * Input: Valid receiver ID and invalid notification ID.
     * Expected Output: HttpException with NOT_FOUND status.
     */
    @Test
    void getNotification_invalidId_throwsNotFound() {
        String receiverId = "userFake";
        String fakeId = "nonexistent-id";

        System.out.println("TC_NS_009: Attempting to fetch notification with non-existent ID: " + fakeId);

        HttpException exception = assertThrows(HttpException.class, () -> {
            notificationService.getNotification(receiverId, fakeId);
        });

        System.out.println("TC_NS_009: Caught expected HttpException with message: " + exception.getMessage());

        assertEquals(Message.NOTIFICATION_NOT_FOUND.getMessage(), exception.getMessage(), "TC_NS_009: Error message should match");
    }
    /**
     * TC_NS_010: Test updateStatus() when notification exists.
     * Test Objective: Ensure notification status is updated successfully when valid id and receiverId are provided.
     * Input: Valid receiver ID, notification ID, and a new status.
     * Expected Output: The status of the notification is updated.
     */
    @Test
    @Transactional
    void updateStatus_validNotification_updatesStatus() {
        String receiverId = "userUpdate";
        String notificationId;

        // Create and send a notification
        NotificationDto dto = new NotificationDto();
        dto.setTitle("Test Title");
        dto.setContent("Test Content");
        dto.setReceiverId(receiverId);
        dto.setObjectData("Sample Data");
        dto.setStatus(NotificationStatus.OPEN);  // Initial status
        dto.setType(NotificationType.APPOINTMENT_REMINDER);

        notificationService.sendNotifications(List.of(receiverId), dto);

        // Fetch the created notification ID
        Pageable pageable = PageRequest.of(0, 1);
        notificationId = notificationService
                .getNotifications(receiverId, pageable)
                .getContent()
                .get(0)
                .getId();

        System.out.println("Retrieved notification ID: " + notificationId);

        // Call the updateStatus method with new status
        notificationService.updateStatus(receiverId, notificationId, NotificationStatus.READ);

        // Fetch the updated notification
        Notification updatedNotification = notificationRepository.findByIdAndReceiverId(notificationId, receiverId)
                .orElseThrow(() -> HttpException.notFound(Message.NOTIFICATION_NOT_FOUND.getMessage()));

        System.out.println("Fetched Updated Notification Status:");
        System.out.println("- Status: " + updatedNotification.getStatus());  // Output the status

        // Assertions
        assertEquals(NotificationStatus.READ, updatedNotification.getStatus(), "Status should be updated to CLOSED");
    }

    /**
     * TC_NS_011: Test updateStatus() when notification does not exist for specific type.
     * Test Objective: Ensure HttpException is thrown when notification does not exist for the provided id and receiverId,
     * even for different NotificationType.
     * Input: Non-existing notification ID and receiver ID with different NotificationType.
     * Expected Output: HttpException is thrown.
     */
    @Test
    @Transactional
    void updateStatus_notificationNotFound_throwsException_forSpecificNotificationType() {
        String receiverId = "nonExistentUser";
        String invalidNotificationId = "nonExistentId";

        System.out.println("Trying to update a non-existent notification...");

        // Expect exception to be thrown
        assertThrows(HttpException.class, () -> {
            notificationService.updateStatus(receiverId, invalidNotificationId, NotificationStatus.READ);
        });

        System.out.println("Correctly thrown exception for non-existent notification.");
    }

    /**
     * TC_NS_012: Test updateAllStatus() when notifications with OPEN status exist.
     * Test Objective: Ensure all notifications with OPEN status are updated to the new status.
     * Input: Valid receiver ID and status (e.g., READ).
     * Expected Output: All notifications with status OPEN are updated to the new status.
     */
    @Test
    @Transactional
    void updateAllStatus_validNotifications_updatesStatuses() {
        String receiverId = "userUpdateAll";
        NotificationStatus newStatus = NotificationStatus.READ;

        // Create and send a notification with OPEN status
        NotificationDto dto = new NotificationDto();
        dto.setTitle("Test Title");
        dto.setContent("Test Content");
        dto.setReceiverId(receiverId);
        dto.setObjectData("Sample Data");
        dto.setStatus(NotificationStatus.OPEN);  // Initial status is OPEN
        dto.setType(NotificationType.APPOINTMENT_REMINDER);

        // Create a second notification with OPEN status
        NotificationDto dto2 = new NotificationDto();
        dto2.setTitle("Test Title 2");
        dto2.setContent("Test Content 2");
        dto2.setReceiverId(receiverId);
        dto2.setObjectData("Sample Data 2");
        dto2.setStatus(NotificationStatus.OPEN);  // Initial status is OPEN
        dto2.setType(NotificationType.CANCEL_APPOINTMENT_SUCCESS);

        // Send notifications
        notificationService.sendNotifications(List.of(receiverId), dto);
        notificationService.sendNotifications(List.of(receiverId), dto2);

        // Verify that notifications were created with OPEN status
        Pageable pageable = PageRequest.of(0, 10);
        List<NotificationDto> notificationsBeforeUpdate = notificationService
                .getNotifications(receiverId, pageable)
                .getContent();

        System.out.println("Notifications before update:");
        notificationsBeforeUpdate.forEach(notification -> {
            System.out.println("ID: " + notification.getId() + ", Status: " + notification.getStatus());
        });

        assertTrue(notificationsBeforeUpdate.size() > 0, "Expected to have notifications with OPEN status.");

        // Call the updateAllStatus method to update status of all OPEN notifications
        notificationService.updateAllStatus(receiverId, newStatus);

        // Verify that notifications have been updated to the new status
        List<Notification> updatedNotifications = notificationRepository.findAllByReceiverIdAndStatusEquals(receiverId, newStatus);

        System.out.println("Notifications after update:");
        updatedNotifications.forEach(notification -> {
            System.out.println("ID: " + notification.getId() + ", Status: " + notification.getStatus());
        });

        // Assertions
        assertEquals(2, updatedNotifications.size(), "Expected all notifications to be updated.");
        updatedNotifications.forEach(notification -> {
            assertEquals(newStatus, notification.getStatus(), "Expected status to be updated to " + newStatus);
        });
    }



}
