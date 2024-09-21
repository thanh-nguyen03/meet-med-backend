package com.thanhnd.clinic_application.filter;

import com.thanhnd.clinic_application.annotation.PermissionsAllowed;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.Message;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter implements ApplicationContextAware {
	private ApplicationContext applicationContext;
	private final JwtAuthenticationManager jwtAuthenticationManager;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
		try {
			RequestMappingHandlerMapping req2HandlerMapping = (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
			HandlerExecutionChain handlerExeChain = req2HandlerMapping.getHandler(request);
			if (Objects.nonNull(handlerExeChain)) {
				HandlerMethod handlerMethod = (HandlerMethod) handlerExeChain.getHandler();
				Method method = handlerMethod.getMethod();

				PermissionsAllowed permissionsAllowed = method.getAnnotation(PermissionsAllowed.class);
				if (Objects.nonNull(permissionsAllowed)) {
					authorize(permissionsAllowed);
				}
			}
		} catch (HttpException httpException) {
			response.sendError(httpException.getStatus().value(), httpException.getMessage());
		} catch (Exception e) {
			throw HttpException.internalServerError(e.getMessage());
		}
		filterChain.doFilter(request, response);
	}

	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	private void authorize(PermissionsAllowed permissionsAllowed) {
		if (permissionsAllowed == null) {
			return;
		}
		Set<String> userPermissions = new HashSet<>(jwtAuthenticationManager.getUserPermissions());
		Set<String> requiredPermissions = new HashSet<>(Arrays.asList(permissionsAllowed.permissions()));
		if (Strings.isNotEmpty(permissionsAllowed.value())) {
			requiredPermissions.add(permissionsAllowed.value());
		}

		if (requiredPermissions.isEmpty()) {
			return;
		}

		PermissionsAllowed.DecisionStrategy decisionStrategy = permissionsAllowed.decisionStrategy();
		if (decisionStrategy.equals(PermissionsAllowed.DecisionStrategy.ANY)) {
			requiredPermissions.retainAll(userPermissions);
			if (requiredPermissions.isEmpty()) {
				throw HttpException.forbidden(Message.PERMISSION_DENIED.getMessage());
			}
		} else if (decisionStrategy.equals(PermissionsAllowed.DecisionStrategy.ALL)) {
			if (!userPermissions.containsAll(requiredPermissions)) {
				throw HttpException.forbidden("You do not have permission to access this resource");
			}
		}
	}
}
