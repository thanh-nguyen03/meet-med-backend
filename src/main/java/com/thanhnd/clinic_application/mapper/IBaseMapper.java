package com.thanhnd.clinic_application.mapper;


import org.mapstruct.MappingTarget;

public interface IBaseMapper<E, D> {
	D toDto(E entity);

	E toEntity(D dto);

	void merge(@MappingTarget E entity, D dto);
}
