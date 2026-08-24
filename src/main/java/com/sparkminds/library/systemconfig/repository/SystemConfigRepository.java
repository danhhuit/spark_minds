package com.sparkminds.library.systemconfig.repository;

import com.sparkminds.library.systemconfig.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigRepository
        extends JpaRepository<SystemConfig, Long> {
}