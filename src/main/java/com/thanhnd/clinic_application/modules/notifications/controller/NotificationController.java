package com.thanhnd.clinic_application.modules.notifications.controller;

import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.constants.NotificationStatus;
import com.thanhnd.clinic_application.constants.PaginationConstants;
import com.thanhnd.clinic_application.modules.appointments.service.AppointmentReminderService;
import com.thanhnd.clinic_application.modules.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ControllerPath.NOTIFICATION_CONTROLLER)
@RequiredArgsConstructor
public class NotificationController extends BaseController {
	private final NotificationService notificationService;
	private final AppointmentReminderService appointmentReminderService;

	@GetMapping
	public ResponseEntity<ResponseDto> getAllNotifications(
		@RequestParam(value = PaginationConstants.PAGE, defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
		@RequestParam(value = PaginationConstants.SIZE, defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int size,
		@RequestParam(value = PaginationConstants.ORDER_BY, defaultValue = "createdAt") String orderBy,
		@RequestParam(value = PaginationConstants.ORDER, defaultValue = "asc") String order
	) {
		String userId = jwtAuthenticationManager.getUserId();
		PageRequest pageRequest = parsePageRequest(page, size, orderBy, order);
		return createSuccessResponse(ResponseDto.success(notificationService.getNotifications(userId, pageRequest)));
	}

	@GetMapping("/{notificationId}")
	public ResponseEntity<ResponseDto> getNotification(@PathVariable String notificationId) {
		String userId = jwtAuthenticationManager.getUserId();
		return createSuccessResponse(ResponseDto.success(notificationService.getNotification(userId, notificationId)));
	}

	@PostMapping("/appointment/{appointmentId}")
	public ResponseEntity<ResponseDto> createAppointmentNotification(@PathVariable String appointmentId) {
		appointmentReminderService.sendMockReminder(appointmentId);
		return createSuccessResponse(ResponseDto.success());
	}

	@PutMapping("/{notificationId}/read")
	public ResponseEntity<ResponseDto> updateNotificationStatus(@PathVariable String notificationId) {
		String userId = jwtAuthenticationManager.getUserId();
		notificationService.updateStatus(userId, notificationId, NotificationStatus.READ);
		return createSuccessResponse(ResponseDto.success());
	}

	@PutMapping("/read-all")
	public ResponseEntity<ResponseDto> updateAllNotificationStatus() {
		String userId = jwtAuthenticationManager.getUserId();
		notificationService.updateAllStatus(userId, NotificationStatus.READ);
		return createSuccessResponse(ResponseDto.success());
	}

	@GetMapping("/socket-io-name-rooms")
	public ResponseEntity<ResponseDto> getSocketIONameRooms() {
		String userId = jwtAuthenticationManager.getUserId();
		return createSuccessResponse(ResponseDto.success(notificationService.getSocketIONameRooms(userId)));
	}
}
