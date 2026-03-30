package com.parrcel.api.modules.users.mapper;

import com.parrcel.api.modules.users.dto.CreateUserReq;
import com.parrcel.api.modules.users.entity.Role;
import com.parrcel.api.modules.users.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = Role.class)
public interface UserMapper {

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "twoFactorCode", ignore = true)
    User toEntity(CreateUserReq createUserReq);

    @Named("rolesToStrings")
    static Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}