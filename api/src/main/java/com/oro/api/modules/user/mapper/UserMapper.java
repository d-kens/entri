package com.oro.api.modules.user.mapper;

import com.oro.api.modules.user.dto.CreateUserRequest;
import com.oro.api.modules.user.dto.UserResponse;
import com.oro.api.modules.user.entity.Role;
import com.oro.api.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = Role.class)
public interface UserMapper {
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "twoFactorCode", ignore = true)
    User toEntity(CreateUserRequest request);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    UserResponse toResponse(User user);

    @Named("rolesToStrings")
    static Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}