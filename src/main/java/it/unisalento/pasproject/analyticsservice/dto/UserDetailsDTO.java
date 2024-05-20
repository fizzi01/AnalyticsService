package it.unisalento.pasproject.analyticsservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailsDTO {
    private String email;
    private String role;
    private Boolean enabled;

    public UserDetailsDTO() {
    }

    public UserDetailsDTO(String email, String role, Boolean enable) {
        this.email = email;
        this.role = role;
        this.enabled = true;
    }

}
