package com.bengj.hirers.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@Getter @Setter
@EntityListeners( AuditingEntityListener.class)
public class BaseEntity {

    @CreatedDate
    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "CREATED_BY", nullable = false, length = 255, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @UpdateTimestamp
    @Column(name = "UPDATED_AT", insertable = false)
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "UPDATED_BY", length = 255, insertable = false)
    private String updatedBy;
}
