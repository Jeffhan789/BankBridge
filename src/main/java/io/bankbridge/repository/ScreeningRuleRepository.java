package io.bankbridge.repository;

import io.bankbridge.domain.ScreeningRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreeningRuleRepository extends JpaRepository<ScreeningRule, String> {
    List<ScreeningRule> findByActiveTrueOrderByCreatedAtAsc();
}
