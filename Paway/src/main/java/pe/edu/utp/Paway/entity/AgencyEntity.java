package pe.edu.utp.Paway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Origin agency where a shipment is dropped off. Table {@code agency} (reference data from V1). */
@Entity
@Table(name = "agency")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgencyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 60)
    @Column(nullable = false, length = 60, unique = true)
    private String name;

    /** District where the agency is located; a shipment cannot be sent to the same district. */
    @NotBlank @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String district;
}
