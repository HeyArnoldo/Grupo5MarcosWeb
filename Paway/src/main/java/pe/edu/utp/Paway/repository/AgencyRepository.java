package pe.edu.utp.Paway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.Paway.entity.AgencyEntity;

public interface AgencyRepository extends JpaRepository<AgencyEntity, Long> {
    Optional<AgencyEntity> findByName(String name);

    List<AgencyEntity> findAllByOrderByIdAsc();
}
