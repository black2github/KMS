package ru.gazprombank.token.kms.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gazprombank.token.kms.entity.KeyData;

@Repository
public interface KeyDataRepository extends JpaRepository<KeyData, UUID> {
}
