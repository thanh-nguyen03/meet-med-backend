package com.thanhnd.clinic_application.common.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageableResultDto<T> {
	private int totalElements;
	private int totalPage;
	private int pageSize;
	private List<T> content;

	private PageableResultDto(int totalElements, int totalPage, int pageSize, List<T> content) {
		this.totalElements = totalElements;
		this.totalPage = totalPage;
		this.pageSize = pageSize;
		this.content = content;
	}

	public static <T> PageableResultDto<T> of(int totalElements, int totalPage, int pageSize, List<T> content) {
		return new PageableResultDto<>(totalElements, totalPage, pageSize, content);
	}

	public static <T> PageableResultDto<T> parse(Page<T> pageable) {
		return new PageableResultDto<>(
			(int) pageable.getTotalElements(),
			pageable.getTotalPages(),
			pageable.getSize(),
			pageable.getContent());
	}
}
