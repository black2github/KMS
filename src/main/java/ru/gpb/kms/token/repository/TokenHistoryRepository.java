package ru.gpb.kms.token.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gpb.kms.token.entity.Token;
import ru.gpb.kms.token.entity.TokenHistory;

import java.util.List;

@Repository
public interface TokenHistoryRepository extends JpaRepository<TokenHistory, Long> {

    @EntityGraph(attributePaths = {"token"})
    List<TokenHistory> findByTokenAndMethodAndUser(Token token, String method, String user);
}
