package it.unisalento.pasproject.analyticsservice.exceptions.global;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomErrorResponse {
    private String traceId;
    private String timestamp;
    private HttpStatus status;
    private String message;
}
