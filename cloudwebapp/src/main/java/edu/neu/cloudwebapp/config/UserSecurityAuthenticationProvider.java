package edu.neu.cloudwebapp.config;

import edu.neu.cloudwebapp.model.UserRegistration;
import edu.neu.cloudwebapp.services.UserWebService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UserSecurityAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    UserWebService userWebService;

    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();
        UserRegistration user = userWebService.getUser(email, password);
        if (user != null)
            return new UsernamePasswordAuthenticationToken(email, password, new ArrayList<>());
        else {
            throw new BadCredentialsException("External user authentication failed");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
