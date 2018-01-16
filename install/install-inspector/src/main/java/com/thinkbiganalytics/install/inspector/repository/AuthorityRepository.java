package com.thinkbiganalytics.install.inspector.repository;

import com.thinkbiganalytics.install.inspector.domain.Authority;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Authority entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
