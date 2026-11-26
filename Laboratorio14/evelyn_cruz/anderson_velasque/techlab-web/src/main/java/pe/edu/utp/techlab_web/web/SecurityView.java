package pe.edu.utp.techlab_web.web;

public record SecurityView(boolean authenticated, String username, boolean admin) {

    public static SecurityView anonymous() {
        return new SecurityView(false, "", false);
    }
}