package com.common_packages.common_packages.event;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class UserRegisteredEvent {
    private Long userId;
    private String email;
    private String fullName;
    private LocalDateTime registeredAt;

    public UserRegisteredEvent() {}

    public UserRegisteredEvent(Long userId, String email, String fullName, LocalDateTime registeredAt) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.registeredAt = registeredAt;
    }

}