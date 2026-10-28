package pe.edu.utp.Paway.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import pe.edu.utp.Paway.entity.ComplaintEntity;

public interface ComplaintRepository extends JpaRepository<ComplaintEntity, Long> {
    Optional<ComplaintEntity> findByToken(UUID token);

    List<ComplaintEntity> findAllByOrderBySheetNumberDesc();

    /** Correlative of the sheet (PW-WEB-000001...), safe under concurrent submissions. */
    @Query(value = "select nextval('complaint_number_seq')", nativeQuery = true)
    long nextSheetNumber();
}
