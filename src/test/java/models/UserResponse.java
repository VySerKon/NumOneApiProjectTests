package models;

import lombok.Data;

@Data
public class UserResponse {
    private String name;
    private String job;
    private String id;
    private String createdAt;
    private String updatedAt;
}
