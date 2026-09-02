package com.entri.payouts.repository;

import com.entri.payouts.entity.OrganizerPayoutAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizerPayoutAccountRepository extends JpaRepository<OrganizerPayoutAccount, Long> {

    List<OrganizerPayoutAccount> findByOrganizerExternalKey(String organizerKey);

    Optional<OrganizerPayoutAccount> findByExternalId(String externalId);

    Optional<OrganizerPayoutAccount> findByExternalIdAndOrganizerExternalKey(String externalId, String organizerKey);

    Optional<OrganizerPayoutAccount> findByOrganizerExternalKeyAndIsDefaultTrue(String organizerKey);

    boolean existsByOrganizerExternalKeyAndIsDefaultTrue(String organizerKey);
}
