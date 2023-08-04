package ru.gpb.kms.token.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Builder
@Accessors(chain = true)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TokenHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Long id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "token_id")
    private Token token;

    /**
     * Creation date and time.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "method", length = 16, nullable = false)
    private String method;

    @Column(name = "user_id", length = 16, nullable = false)
    private String user;

    @Column(name = "doc_id", length = 16, nullable = false)
    private String document;

    @Column(name = "channel", length = 16, nullable = true)
    private String channel;

    @Column(name = "system_id", length = 16, nullable = false)
    private String system;
}
