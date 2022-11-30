package com.integralblue.demo.jumpstart.security;

import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/** Security configuration when using fixed username/passwords.
 *
 * This configuration should only be used for testing!
 *
 */
@Configuration
public class FixedUsernamePasswordSecurityConfiguration {

	@Bean
	@SuppressWarnings("PMD.SignatureDeclareThrowsException")
	public SecurityFilterChain filterChain(final @NonNull HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests()
			.anyRequest()
			.authenticated()
			.and()
			.formLogin()
			.and()
			.httpBasic();
		return http.build();
	}

	@Bean
	public InMemoryUserDetailsManager userDetailsService() {
		@SuppressWarnings("deprecation")
		final UserDetails user = User.withDefaultPasswordEncoder()
				.username("user")
				.password("password")
				.roles("USER")
				.build();
		return new InMemoryUserDetailsManager(user);
	}

}
