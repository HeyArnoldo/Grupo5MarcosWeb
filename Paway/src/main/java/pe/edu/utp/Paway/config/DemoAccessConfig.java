package pe.edu.utp.Paway.config;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DemoAccessConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws IOException {
                HttpSession session = request.getSession(false);
                String profile = session == null ? "" : (String) session.getAttribute("profile");
                String path = request.getRequestURI().substring(request.getContextPath().length());
                boolean allowed = path.startsWith("/admin") ? "admin".equals(profile)
                        : "client1".equals(profile) || "client2".equals(profile);
                if (!allowed) response.sendRedirect(request.getContextPath() + "/login");
                return allowed;
            }
        }).addPathPatterns("/admin", "/admin/**", "/cliente", "/cliente/**");
    }
}
