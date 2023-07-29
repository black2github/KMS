package ru.gazprombank.token.kms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gazprombank.token.kms.entity.KeyDataHistory;

@Repository
public interface KeyDataHistoryRepository extends JpaRepository<KeyDataHistory, Long> {

}
