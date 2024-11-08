package com.thanhnd.clinic_application.mapper;


import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

public interface IBaseMapper<E, D> {
	D toDto(E entity);

	E toEntity(D dto);

	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void merge(@MappingTarget E entity, D dto);
}
