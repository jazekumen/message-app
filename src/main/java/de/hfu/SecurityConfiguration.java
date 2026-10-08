package de.hfu;

import de.hfu.model.User;
import de.hfu.service.MessageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/messageForm.html", "/createMessage.html").hasRole("USER")
                .anyRequest().permitAll()
                .and()
            .formLogin()
                .loginPage("/login.html")
                .loginProcessingUrl("/security/spring_security_check")
                .defaultSuccessUrl("/nachrichtenListe.html")
                .permitAll()
                .and()
            .logout()
                .logoutSuccessUrl("/nachrichtenListe.html")
                .logoutUrl("/security/spring_security_logout")
                .permitAll()
                .and()
            .sessionManagement()
                .invalidSessionUrl("/nachrichtenListe.html")
                .and()
            .csrf()
                .disable();
        return http.build();
    }

    /** Passwords are stored as BCrypt hashes. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Loads users for the login from our own database. */
    @Bean
    public UserDetailsService userDetailsService(MessageService messageService) {
        return username -> {
            User user = messageService.findUserByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException(username);
            }
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();
        };
    }
}
