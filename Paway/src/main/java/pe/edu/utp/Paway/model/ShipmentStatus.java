package pe.edu.utp.Paway.model;

public enum ShipmentStatus {
    REGISTERED("Registrado", "secondary"),
    WAREHOUSE("En almacén", "info"),
    TRANSIT("En tránsito", "warning"),
    DELIVERY("En reparto", "primary"),
    DELIVERED("Entregado", "success"),
    INCIDENT("Incidencia", "danger"),
    CANCELLED("Anulado", "dark");

    private final String label;
    private final String badge;

    ShipmentStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() { return label; }
    public String getBadge() { return badge; }
}
