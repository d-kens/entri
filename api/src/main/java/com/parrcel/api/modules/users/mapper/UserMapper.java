package com.parrcel.api.modules.users.mapper;

import com.parrcel.api.modules.users.dto.CreateUserDto;
import com.parrcel.api.modules.users.dto.UserResponse;
import com.parrcel.api.modules.users.model.User;
import com.parrcel.api.modules.users.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = Role.class)
public interface UserMapper {
    UserResponse toResponse(User user);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", expression = "java(Role.from(createUserDto.getRole()))")
    User toEntity(CreateUserDto createUserDto);
}