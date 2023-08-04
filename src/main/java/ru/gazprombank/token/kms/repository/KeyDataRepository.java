package ru.gazprombank.token.kms.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gazprombank.token.kms.entity.KeyData;
import ru.gazprombank.token.kms.entity.KeyStatus;
import ru.gazprombank.token.kms.entity.KeyType;
import ru.gazprombank.token.kms.entity.PurposeType;

@Repository
public interface KeyDataRepository extends JpaRepository<KeyData, UUID> {

    List<KeyData> findByKeyTypeAndPurposeTypeAndStatus(
            KeyType keyType, PurposeType purposeType, KeyStatus keyStatus);

    List<KeyData> findByAlias(String alias);

    @EntityGraph(attributePaths = {"encKey"})
    List<KeyData> findByEncKey(KeyData key);

    @EntityGraph(attributePaths = {"relatedKey"})
    List<KeyData> findByPurposeTypeAndStatus(PurposeType purposeType, KeyStatus keyStatus);
}
