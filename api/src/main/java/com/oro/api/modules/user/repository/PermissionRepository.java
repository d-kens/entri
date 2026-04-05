package com.oro.api.modules.user.repository;

import com.oro.api.modules.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
