package config;

import jakarta.annotation.PostConstruct;
import model.Role;
import model.User;
import model.User.LifeStage;
import org.springframework.http.HttpMethod;
import repository.RoleRepository;
import repository.UserRepository;
import service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // PERMITE REQUISIÇÕES OPTIONS SEM AUTENTICAÇÃO
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Rotas públicas
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/create-admin-**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Rotas protegidas
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/roles/**").hasRole("ADMIN")

                        // Todas as outras rotas requerem autenticação
                        .anyRequest().authenticated()
                );

        // Adiciona nosso JWT filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Criar usuário admin padrão na inicialização
    @PostConstruct
    public void init() {
        // Verificar se o papel de ADMIN existe, senão criar
        Role adminRole = createAdminRoleIfNotExists();

        // Verificar se o usuário admin existe, senão criar
        createAdminUserIfNotExists(adminRole);
    }

    private Role createAdminRoleIfNotExists() {
        Optional<Role> existingAdminRole = roleRepository.findByName("ADMIN");

        if (existingAdminRole.isPresent()) {
            return existingAdminRole.get();
        }

        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrador do sistema");
        adminRole.setCanManageUsers(true);
        adminRole.setCanManageRoles(true);
        adminRole.setCanManageStages(true);
        adminRole.setCanManageDocuments(true);

        return roleRepository.save(adminRole);
    }

    private void createAdminUserIfNotExists(Role adminRole) {
        Optional<User> existingAdmin = userRepository.findByUsername("admin");

        if (existingAdmin.isPresent()) {
            return;
        }

        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder().encode("admin123"));
        adminUser.setName("Administrador");
        adminUser.setRole(adminRole);
        adminUser.setCity("Cidade Padrão");
        adminUser.setState("Estado Padrão");
        adminUser.setLifeStage(LifeStage.CONSECRATED_PERMANENT);
        adminUser.setCommunityYears(0);
        adminUser.setCommunityMonths(0);
        adminUser.setIsEnabled(true);
        adminUser.setIsAccountNonExpired(true);
        adminUser.setIsAccountNonLocked(true);
        adminUser.setIsCredentialsNonExpired(true);

        userRepository.save(adminUser);

        System.out.println("Usuário admin criado com sucesso! Login: admin, Senha: admin123");
    }
}