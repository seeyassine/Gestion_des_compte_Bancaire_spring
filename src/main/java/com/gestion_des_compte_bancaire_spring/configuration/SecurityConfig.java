package com.gestion_des_compte_bancaire_spring.configuration;


import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private PasswordEncoder passwordEncoder;
    private RsaConfig rsaConfig;

    public SecurityConfig(PasswordEncoder passwordEncoder, RsaConfig rsaConfig) {
        this.passwordEncoder = passwordEncoder;
        this.rsaConfig = rsaConfig;
    }

    // service d'authentification
    @Bean
    public AuthenticationManager authenticationManager( UserDetailsManager userDetailsManager){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userDetailsManager);
        return  new ProviderManager(daoAuthenticationProvider);
    }





    @Bean
    UserDetailsManager userDetailsManager(){
        return  new InMemoryUserDetailsManager(
                User.withUsername("admin").password(passwordEncoder.encode("1234")).authorities("ADMIN").build(),
                User.withUsername("user1").password(passwordEncoder.encode("1234")).authorities("USER").build(),
                User.withUsername("user2").password(passwordEncoder.encode("1234")).authorities("USER").build()
        );

    }




    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity httpSecurity) throws Exception{

        return httpSecurity
                .sessionManagement(sess-> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeRequests(auth -> auth
                        // Permit all requests to login and refresh token endpoints
                        .requestMatchers("/login/**", "/RefreshToken/**").permitAll()

                        // .authorizeRequests(auth-> auth.requestMatchers("/login/**").permitAll())
                       //  .authorizeRequests(auth-> auth.requestMatchers("/RefreshToken/**").permitAll())

                        // Restrict add, modify, and delete account operations to ADMIN role
                        .requestMatchers(HttpMethod.POST, "/v1/comptes").hasRole("ADMIN")  // Adding a new account
                        .requestMatchers(HttpMethod.PUT, "/v1/comptes/**").hasRole("ADMIN")  // Modifying an account
                        .requestMatchers(HttpMethod.DELETE, "/v1/comptes/**").hasRole("ADMIN")  // Deleting an account

                        //.requestMatchers(HttpMethod.POST, "/v1/comptes", HttpMethod.PUT, "/v1/comptes/**", HttpMethod.DELETE, "/v1/comptes/**").hasRole("ADMIN")

                        //Allow any authenticated user to retrieve an account by ID
                        .requestMatchers(HttpMethod.GET, "/v1/comptes/{id}").authenticated()

                        // Allow any authenticated user to credit or debit an account
                        .requestMatchers(HttpMethod.GET, "/v1/comptes/crediter/{id}/{m}").authenticated()  // Credit operation
                        .requestMatchers(HttpMethod.GET, "/v1/comptes/debiter/{id}/{m}").authenticated()   // Debit operation

                        //Restrict retrieving all accounts to users with ADMIN role
                        .requestMatchers(HttpMethod.GET, "/v1/comptes").hasRole("ADMIN")  // Retrieving all accounts


                        // Any other request requires authentication
                        .anyRequest().authenticated()
                // .authorizeRequests(auth -> auth.anyRequest().authenticated())
                )

                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    // signé le token
    @Bean
    JwtEncoder jwtEncoder(){
        JWK  jwk = new RSAKey.Builder(rsaConfig.publicKey()).privateKey(rsaConfig.privateKey()).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    JwtDecoder jwtDecoder(){
        return NimbusJwtDecoder.withPublicKey(rsaConfig.publicKey()).build();
    }



}
