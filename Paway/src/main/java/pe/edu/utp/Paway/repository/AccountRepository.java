package pe.edu.utp.Paway.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.utp.Paway.entity.AccountEntity;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    /** Emails are stored lower-cased; callers pass the normalized address. */
    Optional<AccountEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Sign-up checks the customer table too, so a demo customer's email or DNI cannot be reused. */
    @Query("select count(c) > 0 from CustomerEntity c where lower(c.email) = :email")
    boolean customerEmailTaken(@Param("email") String normalizedEmail);

    @Query("select count(c) > 0 from CustomerEntity c where c.document = :document")
    boolean customerDocumentTaken(@Param("document") String document);
}
