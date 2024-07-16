package it.unisalento.pasproject.analyticsservice.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Getter
public class SecurityConstants {

    private SecurityConstants() {}

    @Value("${secret.key}")
    public String JWT_SECRET;

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_UTENTE = "UTENTE";
    public static final String ROLE_MEMBRO = "MEMBRO";
}
