package com.bengj.hirers.security.oauth2;

import com.bengj.hirers.enums.AuthProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oauth2User;
    private final AuthProvider authProvider;
    private final String providerId;
    private final String email;
    private final String name;
    private final String avatarUrl;


    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return this.name != null ? this.name : (this.email != null ? this.email : this.providerId);
    }
}
