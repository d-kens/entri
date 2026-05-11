package com.api.modules.users.service.mapper;

import com.api.modules.users.dto.UserResponse;
import com.api.modules.users.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
