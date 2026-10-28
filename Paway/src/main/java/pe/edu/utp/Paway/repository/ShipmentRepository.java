package pe.edu.utp.Paway.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import pe.edu.utp.Paway.entity.ShipmentEntity;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long>,
        JpaSpecificationExecutor<ShipmentEntity> {

    Optional<ShipmentEntity> findByGuide(String guide);

    /** Reserves the next shipment id; the guide number is derived from it before the INSERT. */
    @Query(value = "select nextval('shipment_id_seq')", nativeQuery = true)
    long nextId();
}
