package ru.gpb.kms.token.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gpb.kms.token.entity.KeyDataHistory;

@Repository
public interface KeyDataHistoryRepository extends JpaRepository<KeyDataHistory, Long> {

}
