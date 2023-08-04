package ru.gazprombank.token.kms.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gazprombank.token.kms.entity.Token;
import ru.gazprombank.token.kms.entity.TokenHistory;

import java.util.List;

@Repository
public interface TokenHistoryRepository extends JpaRepository<TokenHistory, Long> {

    @EntityGraph(attributePaths = {"token"})
    List<TokenHistory> findByTokenAndMethodAndUser(Token token, String method, String user);
}
