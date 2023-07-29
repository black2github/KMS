package ru.gazprombank.token.kms.entity;

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
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;

@Data
@Builder
@Accessors(chain = true)
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class KeyDataHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "key_id")
    private KeyData key;

    /**
     * Creation date and time.
     */
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "status", length = 16, nullable = false)
    private KeyStatus status;

    @Column(name = "principal", length = 16, nullable = false)
    private UserDetails user;

    @Column(name = "userType", length = 16, nullable = false)
    private String userType;

    public String toString() {
        return "KetDataHistory{id=" + id + ", key="+key.getId()+", created="
                +createdDate+", status="+status+", user="+user+", userType="+userType+"}";
    }
}
