package com.sparkminds.library.systemconfig.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "system_configs")
@Getter
@Setter
@NoArgsConstructor
public class SystemConfig {

    @Id
    private Long id;

    @Column(name = "maintenance_mode", nullable = false)
    private boolean maintenanceMode;

    @Column(name = "maintenance_message", nullable = false, length = 500)
    private String maintenanceMessage;

    @Column(name = "updated_by")
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}