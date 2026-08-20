package com.entri.users.service.mapper;

import com.entri.users.dto.UserResponse;
import com.entri.users.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
