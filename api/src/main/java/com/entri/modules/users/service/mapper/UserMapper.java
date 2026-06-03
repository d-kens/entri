package com.entri.modules.users.service.mapper;

import com.entri.modules.users.dto.UserResponse;
import com.entri.modules.users.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
