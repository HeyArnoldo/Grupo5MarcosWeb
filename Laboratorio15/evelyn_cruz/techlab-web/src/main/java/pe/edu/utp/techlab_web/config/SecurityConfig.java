package pe.edu.utp.techlab_web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider(
            UserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurity(HttpSecurity http,
            AuthenticationProvider provider) throws Exception {
        http.securityMatcher("/api/**")
                .authenticationProvider(provider)
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.GET, "/api/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.HEAD, "/api/**")
                        .hasAnyRole("USER", "ADMIN")
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain webSecurity(HttpSecurity http,
            AuthenticationProvider provider) throws Exception {
        http.authenticationProvider(provider)
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/", "/login", "/error", "/css/**",
                                "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/cursos/nuevo", "/cursos/*/editar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/cursos", "/cursos/", "/cursos/*").permitAll()
                        .requestMatchers("/cursos", "/cursos/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login")
                        .defaultSuccessUrl("/cursos", true)
                        .failureUrl("/login?error").permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout").permitAll())
                .sessionManagement(s -> s
                        .sessionFixation(f -> f.changeSessionId()))
                .csrf(Customizer.withDefaults());
        return http.build();
    }
}