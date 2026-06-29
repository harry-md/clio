package com.harry.clio.security;

import lombok.Getter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUser extends User {
    private final Integer id;
    private final String firstName;
    private final String lastName;
    private final String avatar;
    private final String role;

    public CustomUser(
            Integer id,
            String username,
            String password,
            String firstName,
            String lastName,
            String avatar,
            String role,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.role = role;
    }
}
