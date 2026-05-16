package com.api.modules.properties.repository;

import com.api.modules.properties.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {}
