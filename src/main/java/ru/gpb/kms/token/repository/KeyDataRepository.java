package ru.gpb.kms.token.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gpb.kms.token.entity.KeyData;
import ru.gpb.kms.token.entity.KeyStatus;
import ru.gpb.kms.token.entity.KeyType;
import ru.gpb.kms.token.entity.PurposeType;

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
