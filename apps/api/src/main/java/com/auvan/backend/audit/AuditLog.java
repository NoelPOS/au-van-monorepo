package com.auvan.backend.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Nullable — system actions (e.g. cron expiry) have no actor */
    @Column(name = "actor_id")
    private UUID actorId;

    /** Human-readable action name, e.g. "payment_reviewed", "booking_cancelled" */
    @Column(nullable = false, length = 100)
    private String action;

    /** The entity type acted upon, e.g. "Booking", "Payment" */
    @Column(nullable = false, length = 50)
    private String targetType;

    /** The string ID of the affected resource */
    @Column(length = 100)
    private String targetId;

    /** Before/after state, reason, or any additional context */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(length = 45)
    private String ip;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
