package pe.edu.utp.Paway.repository;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import pe.edu.utp.Paway.entity.ShipmentEntity;
import pe.edu.utp.Paway.model.ShipmentStatus;

/** Optional filters of the administration search, combined with AND. */
public final class ShipmentSpecifications {
    private ShipmentSpecifications() { }

    /** Case-insensitive match on guide or recipient; blank text matches everything. */
    public static Specification<ShipmentEntity> text(String text) {
        String pattern = "%" + text.strip().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> text.isBlank() ? cb.conjunction()
                : cb.or(cb.like(cb.lower(root.get("guide")), pattern),
                        cb.like(cb.lower(root.get("recipient")), pattern));
    }

    public static Specification<ShipmentEntity> status(ShipmentStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<ShipmentEntity> origin(String agencyName) {
        return (root, query, cb) -> agencyName.isBlank() ? cb.conjunction()
                : cb.equal(root.get("originAgency").get("name"), agencyName);
    }

    public static Specification<ShipmentEntity> customer(long customerId) {
        return (root, query, cb) -> cb.equal(root.get("customer").get("id"), customerId);
    }
}
