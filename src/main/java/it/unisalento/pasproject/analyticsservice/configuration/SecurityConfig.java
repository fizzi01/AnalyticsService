package it.unisalento.pasproject.analyticsservice.configuration;

import it.unisalento.pasproject.analyticsservice.security.ExceptionFilter;
import it.unisalento.pasproject.analyticsservice.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {
    /**
     * Configures the security filter chain for the application.
     * <p>
     * This method is used to configure the security settings for the application. It sets up the authorization rules,
     * session management policy, and Cross-Origin Resource Sharing (CORS) and Cross-Site Request Forgery (CSRF) settings.
     *
     * @param http The HttpSecurity object to be configured.
     * @return A SecurityFilterChain object that contains the security settings for the application.
     * @throws Exception If an error occurs during the configuration process.
     */
   @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Configurazione CORS
        http.cors(AbstractHttpConfigurer::disable); // Disabilita CORS, se necessario

        // Configurazione CSRF
        http.csrf(AbstractHttpConfigurer::disable); // Disabilita CSRF

        // Configurazione gestione eccezioni, adatta la gestione eccezioni al Servlet (carica prima degli altri componenti)
        http.addFilterBefore(exceptionFilter(), UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);


       return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public ExceptionFilter exceptionFilter() {
        return new ExceptionFilter();
    }

}