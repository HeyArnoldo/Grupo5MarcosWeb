package pe.edu.utp.Paway.config;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import pe.edu.utp.Paway.entity.AccountEntity.Role;
import pe.edu.utp.Paway.service.AccountService;

/** /admin/** requires the ADMIN account and /cliente/** a CLIENT account; anyone else goes to /login. */
@Configuration
public class AccessConfig implements WebMvcConfigurer {
    private final AccountService accounts;

    public AccessConfig(AccountService accounts) {
        this.accounts = accounts;
    }

    /**
     * One interceptor per protected pattern, so the required role never comes from the raw request URI:
     * Spring matches patterns against the decoded path, and a check such as {@code startsWith("/admin")}
     * on the raw URI is bypassed by {@code /%61dmin}.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requiring(Role.ADMIN)).addPathPatterns("/admin", "/admin/**");
        registry.addInterceptor(requiring(Role.CLIENT)).addPathPatterns("/cliente", "/cliente/**");
    }

    private HandlerInterceptor requiring(Role required) {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws IOException {
                boolean allowed = AccountSession.current(request, accounts)
                        .filter(account -> account.role() == required).isPresent();
                if (!allowed) response.sendRedirect(request.getContextPath() + "/login");
                return allowed;
            }
        };
    }
}
