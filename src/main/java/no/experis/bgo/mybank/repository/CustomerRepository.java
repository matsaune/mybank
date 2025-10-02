package no.experis.bgo.mybank.repository;

import no.experis.bgo.mybank.model.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long>, JpaSpecificationExecutor<CustomerEntity> {
    Optional<CustomerEntity> findByEmail(String email);
    Optional<CustomerEntity> findByPersonalIdNumber(String personalIdNumber);
    boolean existsByEmail(String email);
    boolean existsByPersonalIdNumber(String personalIdNumber);
}
