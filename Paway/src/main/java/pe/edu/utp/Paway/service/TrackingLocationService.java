package pe.edu.utp.Paway.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import pe.edu.utp.Paway.model.Shipment;
import pe.edu.utp.Paway.model.ShipmentStatus;

@Service
public class TrackingLocationService {
    // Approximate town centers, not courier GPS or customer addresses.
    private final Map<String, Point> locations = Map.ofEntries(
            Map.entry("Lima Centro", new Point(-12.0464, -77.0428)),
            Map.entry("Huaral Centro", new Point(-11.4950, -77.2078)),
            Map.entry("Matucana", new Point(-11.8431, -76.3861)),
            Map.entry("San Vicente", new Point(-13.0756, -76.3856)),
            Map.entry("Ate", new Point(-12.0260, -76.9190)),
            Map.entry("San Borja", new Point(-12.1072, -76.9989)),
            Map.entry("Miraflores", new Point(-12.1211, -77.0298)),
            Map.entry("San Isidro", new Point(-12.0990, -77.0360)),
            Map.entry("Lince", new Point(-12.0840, -77.0320)),
            Map.entry("Surco", new Point(-12.1460, -77.0050)),
            Map.entry("Antioquía", new Point(-12.0806, -76.5108)),
            Map.entry("San Bartolomé", new Point(-11.9114, -76.5297)),
            Map.entry("San Mateo", new Point(-11.7592, -76.3008)),
            Map.entry("Huaral", new Point(-11.4950, -77.2078)),
            Map.entry("Chancay", new Point(-11.5631, -77.2700)),
            Map.entry("Aucallama", new Point(-11.5592, -77.1806)),
            Map.entry("Asia", new Point(-12.7792, -76.5561)),
            Map.entry("Lunahuaná", new Point(-12.9611, -76.1406)),
            Map.entry("Mala", new Point(-12.6581, -76.6300)));

    public record Point(double latitude, double longitude) {}
    public record Location(double latitude, double longitude, String label, String explanation) {}

    public Optional<Location> locate(Shipment shipment) {
        if (shipment.status() == ShipmentStatus.CANCELLED) return Optional.empty();
        boolean delivered = shipment.status() == ShipmentStatus.DELIVERED;
        String place = delivered ? shipment.district() : shipment.origin();
        Point point = locations.get(place);
        if (point == null) return Optional.empty();
        return Optional.of(new Location(point.latitude(), point.longitude(),
                (delivered ? "Destino: " : "Origen: ") + place,
                delivered ? "Envío entregado. El punto representa el centro aproximado del distrito de destino, no la dirección de entrega."
                        : "El punto representa la zona de origen registrada. Aún no hay coordenadas de la ubicación actual del paquete; no es seguimiento GPS en tiempo real."));
    }
}
