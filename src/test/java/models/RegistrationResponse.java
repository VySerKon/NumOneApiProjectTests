package models;

import lombok.Builder;
import lombok.Data;

@Data
public class RegistrationResponse {
    private String id;
    private String token;
    private String error;
}
