package pe.edu.utp.techlab_web.web;

public record SecurityView(boolean authenticated, String username) {

    public static SecurityView anonymous() {
        return new SecurityView(false, "");
    }
}
