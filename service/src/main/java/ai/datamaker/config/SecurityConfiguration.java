/*
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

package ai.datamaker.config;

import ai.datamaker.model.Authority;
import ai.datamaker.model.CustomUserDetails;
import ai.datamaker.model.User;
import ai.datamaker.model.UserGroup;
import ai.datamaker.repository.UserGroupRepository;
import ai.datamaker.repository.UserRepository;
import com.azure.spring.aad.webapp.AADWebSecurityConfigurerAdapter;
import com.google.api.client.util.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class SecurityConfiguration {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private DaoAuthenticationProvider authenticationProvider;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    public static final class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.addHeader("Content-Type", "application/json");
            response.getWriter().print("{\"success\": false, \"title\": \"Unauthorized exception\", \"status\": 401, \"timestamp\": \"" + new Date() + "\"}");
            //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }

    public static class MyAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

        @Override
        public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

            log.warn("Login exception for user: {}, {}", request.getParameter("username"), exception.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.addHeader("Content-Type", "application/json");
            response.getWriter().print("{\"success\": false, \"title\": \"Authentication exception\"}");
        }
    }

    public static class MySavedRequestAwareAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        private RequestCache requestCache = new HttpSessionRequestCache();

        @Override
        public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws ServletException, IOException {

            SavedRequest savedRequest = requestCache.getRequest(request, response);

            response.addHeader("Content-Type", "application/json");
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            StringBuilder sb = new StringBuilder();
            authorities.forEach(a -> sb.append("\"").append(a.toString()).append("\",") );
            sb.setLength(sb.length() - 1);
            response.getWriter().print("{\"success\": true, \"username\": \"" + authentication.getName() + "\", \"roles\": [" + sb.toString() + "] }");

            if (savedRequest == null) {
                clearAuthenticationAttributes(request);
                return;
            }
            String targetUrlParam = getTargetUrlParameter();
            if (isAlwaysUseDefaultTargetUrl()
                || (targetUrlParam != null
                && StringUtils.hasText(request.getParameter(targetUrlParam)))) {
                requestCache.removeRequest(request, response);
                clearAuthenticationAttributes(request);
                return;
            }

            clearAuthenticationAttributes(request);
        }

        public void setRequestCache(RequestCache requestCache) {
            this.requestCache = requestCache;
        }
    }

    // Configure no security
    @Configuration
    @Profile("noauth")
    public static class NoAuthSecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().antMatchers("/**").permitAll().and()
                .authorizeRequests().antMatchers("/h2-console/**").permitAll();
            http.csrf().ignoringAntMatchers("/h2-console/**");
            http.headers().frameOptions().sameOrigin();
            //http.csrf().disable();
            //http.headers().frameOptions().disable();
        }
    }

    @Order(3)
    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @Profile("!azure & !ldap & !gcp & !amazon & !ldap-ad")
    //@Profile("dev")
    public class JdbcAuthSecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Value("${admin.password}")
        private String adminPassword;

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(authenticationProvider);

            // Fallback mechanism
            auth.inMemoryAuthentication()
                .withUser("administrator")
                .password(adminPassword)
                .roles("ADMIN")
                .and()
                .withUser("user")
                .password(passwordEncoder.encode("userPass"))
                .roles("USER");
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource()
        {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOriginPatterns(Arrays.asList("*"));
            configuration.setAllowCredentials(true);
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE"));
            configuration.setAllowedHeaders(Arrays.asList("DNT",
                "X-CustomHeader",
                "Keep-Alive",
                "User-Agent",
                "X-Requested-With",
                "If-Modified-Since",
                "Cache-Control",
                "Content-Type",
                "If-Range",
                "Content-Range",
                "Range"));
            configuration.addExposedHeader("Content-Disposition");
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .cors().and()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
                .authorizeRequests()
                .antMatchers("/api/me").authenticated()
                .antMatchers("/api/messages").anonymous()
                .antMatchers(HttpMethod.GET, "/api/user/principal").hasAnyRole("USER")
                .antMatchers(HttpMethod.PUT, "/api/user/change-password").hasAnyRole("USER")
                .antMatchers("/api/user").hasAnyRole("ADMIN")
                .antMatchers("/api/files").hasAnyRole("ADMIN")
                .antMatchers("/api/system").hasAnyRole("ADMIN")
                .antMatchers("/api/**").authenticated()
                .and()
                .formLogin()
                .successHandler(new MySavedRequestAwareAuthenticationSuccessHandler())
                .failureHandler(new MyAuthenticationFailureHandler())
                .and()
                .logout();
//                .addLogoutHandler(new LogoutHandler() {
//                    @SneakyThrows
//                    @Override
//                    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
//                        response.setStatus(HttpServletResponse.SC_OK);
//                        response.addHeader("Content-Type", "application/json");
//                        response.getWriter().print("{\"success\": true, \"title\": \"Logout successfully\", \"status\": 200, \"timestamp\": \"" + new Date() + "\"}");
//                    }
//                })
//                .logoutUrl(null);

            http.authorizeRequests().antMatchers("/h2-console/**").permitAll();
            http.csrf().disable();
            http.headers().frameOptions().disable();

        }
    }

    @Configuration
    @EnableWebSecurity
    @Profile({"ldap", "ldap-ad"})
    @Order(2)
    public class LdapSecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Value("${spring.profiles.active}")
        private String activeProfile;

        @Autowired
        private DataSource dataSource;

        @Value("${admin.roles}")
        private Set<String> adminRoles;

        @Value("${user.roles}")
        private Set<String> userRoles;

        @Value("${security.ldap.url}")
        private String ldapUrl;

        @Value("${security.ldap.userDnPatterns:}")
        private String userDnPatterns;

        @Value("${security.ldap.userSearchBase:}")
        private String userSearchBase;

        @Value("${security.ldap.userSearchFilter:}")
        private String userSearchFilter;

        @Value("${security.ldap.groupSearchBase:}")
        private String groupSearchBase;

        @Value("${security.ldap.groupSearchFilter:}")
        private String groupSearchFilter;

        @Value("${security.ldap.passwordAttribute:}")
        private String passwordAttribute;

        @Value("${security.ldap.managerDn:}")
        private String managerDn;

        @Value("${security.ldap.managerPassword:}")
        private String managerPassword;

        @Value("${security.ldap.domain:}")
        private String domain;

        @Value("${security.ldap.rootDn:}")
        private String rootDn;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private UserGroupRepository userGroupRepository;

        public static final String USER_QUERY = "select username,authority from user where username = ?";

        public class CustomJdbcUserDetailsService extends JdbcDaoImpl {

            @Override
            public List<GrantedAuthority> loadUserAuthorities(String username) {
                return getJdbcTemplate().query(USER_QUERY,
                                               new String[] { username }, (rs, rowNum) -> {
                            String roleName = rs.getString(2);

                            return new SimpleGrantedAuthority(roleName);
                        });
            }
        }

        public class CustomLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {

            /**
             * Constructor for group search scenarios. <tt>userRoleAttributes</tt> may still be
             * set as a property.
             *
             * @param contextSource   supplies the contexts used to search for user roles.
             * @param groupSearchBase if this is an empty string the search will be performed from
             */
            public CustomLdapAuthoritiesPopulator(ContextSource contextSource, String groupSearchBase) {
                super(contextSource, groupSearchBase);
            }

            @Override
            protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username) {
                return super.getAdditionalRoles(user, username);
            }
        }

        public class CustomUserDetailsContextMapper implements UserDetailsContextMapper {

            private final Set<String> adminRoles;
            private final UserRepository userRepository;
            private final UserGroupRepository userGroupRepository;

            public CustomUserDetailsContextMapper(Set<String> adminRoles, UserRepository userRepository, UserGroupRepository userGroupRepository) {
                this.adminRoles = adminRoles;
                this.userRepository = userRepository;
                this.userGroupRepository = userGroupRepository;
            }

            @Override
            @Transactional
            public UserDetails mapUserFromContext(DirContextOperations ctx,
                                                  String username,
                                                  Collection<? extends GrantedAuthority> authorities) {
                User user = userRepository.findByUsername(username);
                if (user == null) {
                    user = new User();
                    user.setUsername(username);
                    user.setUserType(User.UserType.EXTERNAL);
                    user.setFirstName(ctx.getStringAttribute("cn"));
                    user.setLastName("");
                }
                updateUserGroups(authorities, user, adminRoles, userGroupRepository);
                userRepository.save(user);

                return new CustomUserDetails(user);
            }

            @Override
            public void mapUserToContext(UserDetails userDetails, DirContextAdapter dirContextAdapter) {

            }
        }

//        @Bean
//        CorsConfigurationSource corsConfigurationSource()
//        {
//            CorsConfiguration configuration = new CorsConfiguration();
//            configuration.setAllowedOrigins(Arrays.asList("*"));
//            configuration.setAllowCredentials(true);
//            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE"));
//            configuration.setAllowedHeaders(Arrays.asList("DNT",
//                                                          "X-CustomHeader",
//                                                          "Keep-Alive",
//                                                          "User-Agent",
//                                                          "X-Requested-With",
//                                                          "If-Modified-Since",
//                                                          "Cache-Control",
//                                                          "Content-Type",
//                                                          "If-Range",
//                                                          "Content-Range",
//                                                          "Range"));
//            configuration.addExposedHeader("Content-Disposition");
//            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//            source.registerCorsConfiguration("/**", configuration);
//            return source;
//        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .cors().and()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
                .authorizeRequests()
                .antMatchers("/api/me").authenticated()
                .antMatchers("/api/messages").anonymous()
                .antMatchers(HttpMethod.GET, "/api/user/principal").hasAnyRole("USER")
                .antMatchers(HttpMethod.PUT, "/api/user/change-password").hasAnyRole("USER")
                .antMatchers("/api/user").hasAnyRole("ADMIN")
                .antMatchers("/api/system").hasAnyRole("ADMIN")
                .antMatchers("/api/**").authenticated()
                .and()
                .formLogin()
                .successHandler(new MySavedRequestAwareAuthenticationSuccessHandler())
                .failureHandler(new MyAuthenticationFailureHandler())
                .and()
                .logout();
        }

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
            CustomJdbcUserDetailsService customJdbcUserDetailsService = new CustomJdbcUserDetailsService();
            customJdbcUserDetailsService.setDataSource(dataSource);

            if (activeProfile.contains("ldap-ad")) {
                ActiveDirectoryLdapAuthenticationProvider adProvider = new ActiveDirectoryLdapAuthenticationProvider(domain, ldapUrl, rootDn);
                adProvider.setConvertSubErrorCodesToExceptions(true);
                adProvider.setUseAuthenticationRequestCredentials(true);
                if (StringUtils.hasText(userSearchFilter)) {
                    adProvider.setSearchFilter(userSearchFilter);
                }
                adProvider.setUserDetailsContextMapper(new UserDetailsContextMapper() {

                    @Override
                    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
                        log.debug("Username: {}", username);

                        User user = userRepository.findByUsername(username);
                        if (user == null) {
                            user = new User();
                            user.setUsername(username);
                            user.setUserType(User.UserType.EXTERNAL);
                            user.setFirstName(ctx.getStringAttribute("givenName"));
                            user.setLastName(ctx.getStringAttribute("sn"));
                        }
                        updateUserGroups(authorities, user, adminRoles, userGroupRepository);
                        userRepository.save(user);

                        return new CustomUserDetails(user);
                    }

                    @Override
                    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {

                    }
                });
//                adProvider.setAuthoritiesMapper(authorities -> {
//                    authorities.forEach(a -> log.debug("authority: {}", a));
//                    SimpleGrantedAuthority userRole = new SimpleGrantedAuthority("ROLE_USER");
//                    List<GrantedAuthority> authorityList = Lists.newArrayList(authorities);
//                    authorityList.add(userRole);
//                    return authorityList;
//                });
                auth.authenticationProvider(adProvider);
            } else {

                auth
                        .ldapAuthentication()
                        //.ldapAuthoritiesPopulator(customLdapAuthoritiesPopulator)
                        .userDetailsContextMapper(new CustomUserDetailsContextMapper(adminRoles, userRepository, userGroupRepository))
                        .userDnPatterns(userDnPatterns)
                        .userSearchBase(userSearchBase)
                        .userSearchFilter(userSearchFilter == null ? null : (userSearchFilter.isBlank() ? null : userSearchFilter))
                        .groupSearchBase(groupSearchBase)
                        .groupSearchFilter(groupSearchFilter)
                        .contextSource()
                        .managerDn(managerDn)
                        .managerPassword(managerPassword)
                        .url(ldapUrl)
                        .and()
                        .passwordCompare()
                        // TODO configure password encoder
                        .passwordEncoder(new BCryptPasswordEncoder())
                        .passwordAttribute(passwordAttribute);
            }

            // CustomLdapAuthoritiesPopulator customLdapAuthoritiesPopulator = new CustomLdapAuthoritiesPopulator(null, "ou=groups");
            auth.authenticationProvider(authenticationProvider);
        }

    }

    @Configuration
    @EnableWebSecurity
    @Profile("cognito")
    @Order(1)
    public class AmazonCognitoSecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Value("${admin.roles}")
        private Set<String> adminRoles;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private UserGroupRepository userGroupRepository;

        @Bean
        public ApplicationListener<AuthenticationSuccessEvent> doSomething() {
            return new ApplicationListener<AuthenticationSuccessEvent>() {
                @Override
                public void onApplicationEvent(AuthenticationSuccessEvent event) {
                    if (event.getAuthentication() instanceof OAuth2LoginAuthenticationToken) {
                        OAuth2LoginAuthenticationToken authentication = (OAuth2LoginAuthenticationToken) event.getAuthentication();
                        User user = userRepository.findByUsername(authentication.getPrincipal().getName());
                        if (user == null) {
                            user = new User();
                            user.setUsername(authentication.getPrincipal().getName());
                            user.setUserType(User.UserType.EXTERNAL);
                            user.setFirstName(authentication.getName());
                            user.setLastName(authentication.getName());
//                            user.setFirstName(authentication.getPrincipal().getAttribute("given_name"));
//                            user.setLastName(authentication.getPrincipal().getAttribute("family_name"));

                        }
                        List<GrantedAuthority> authorities = Lists.newArrayList(authentication.getAuthorities());
                        Iterable<Object> groups = authentication.getPrincipal().getAttribute("cognito:groups");
                        if (groups != null) {
                            groups.forEach(g -> authorities.add(new SimpleGrantedAuthority(g.toString())));
                        }

                        updateUserGroups(authorities, user, adminRoles, userGroupRepository);
                        userRepository.save(user);
                    }
                }
            };
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            adminRoles.add("ADMIN");

            http
                    .csrf().disable()
                    .cors().and()
                    .exceptionHandling()
                    .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                    .and()
                    .authorizeRequests()
                    .antMatchers("/api/me").authenticated()
                    .antMatchers("/api/messages").anonymous()
                    .antMatchers(HttpMethod.GET, "/api/user/principal").authenticated()
                    .antMatchers(HttpMethod.PUT, "/api/user/change-password").authenticated()
                    .antMatchers("/api/user").hasAnyRole(adminRoles.toArray(new String[]{}))
                    .antMatchers("/api/system").hasAnyRole(adminRoles.toArray(new String[]{}))
                    .antMatchers("/api/**").authenticated()
                    .and()
                    .formLogin()
                    .successHandler(new MySavedRequestAwareAuthenticationSuccessHandler())
                    .failureHandler(new MyAuthenticationFailureHandler())
                    .and()
                    .logout()
//                    .addLogoutHandler(new LogoutHandler() {
//                        @SneakyThrows
//                        @Override
//                        public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
//                            response.setStatus(HttpServletResponse.SC_OK);
//                            response.addHeader("Content-Type", "application/json");
//                            response.getWriter().print("{\"success\": true, \"title\": \"Logout successfully\", \"status\": 200, \"timestamp\": \"" + new Date() + "\"}");
//                        }
//                    })
                    .and()
                    .oauth2Login()
                    //.userInfoEndpoint()
                    //.oidcUserService(oidcUserService)
                    //.and()
                    .defaultSuccessUrl("/success");
        }

    }

    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @Profile("azure")
    @Order(1)
    public class AADOAuth2LoginSecurityConfig extends AADWebSecurityConfigurerAdapter {

        @Value("${admin.roles}")
        private Set<String> adminRoles;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private UserGroupRepository userGroupRepository;

        @Bean
        public ApplicationListener<AuthenticationSuccessEvent> doSomething() {
            return new ApplicationListener<AuthenticationSuccessEvent>() {
                @Override
                public void onApplicationEvent(AuthenticationSuccessEvent event) {
                    if (event.getAuthentication() instanceof OAuth2LoginAuthenticationToken) {
                        OAuth2LoginAuthenticationToken authentication = (OAuth2LoginAuthenticationToken) event.getAuthentication();
                        String name = authentication.getName();
                        User user = userRepository.findByUsername(name);
                        if (user == null) {
                            user = new User();
                            //user.setUsername(authentication.getPrincipal().getAttribute("preferred_username"));
                            user.setUserType(User.UserType.EXTERNAL);
                            user.setUsername(name);
                            user.setFirstName(name.split(" ")[0]);
                            user.setLastName(name.split(" ")[1]);
                        }
                        updateUserGroups(authentication.getAuthorities(), user, adminRoles, userGroupRepository);
                        userRepository.save(user);
                    }
                }
            };
        }

        @Autowired
        private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            adminRoles.add("ADMIN");
//            super.configure(http);
//            http.formLogin().defaultSuccessUrl("/success");
            http
                .csrf().disable()
                .cors().and()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
                .authorizeRequests()
                .antMatchers("/api/me").authenticated()
                .antMatchers("/api/messages").anonymous()
                .antMatchers(HttpMethod.GET, "/api/user/principal").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/user/change-password").authenticated()
                .antMatchers("/api/user").hasAnyRole(adminRoles.toArray(new String[]{}))
                .antMatchers("/api/system").hasAnyRole(adminRoles.toArray(new String[]{}))
                .antMatchers("/api/**").authenticated()
                .and()
                .formLogin()
                .successHandler(new MySavedRequestAwareAuthenticationSuccessHandler())
                .failureHandler(new MyAuthenticationFailureHandler())
                .and()
                .logout()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .oidcUserService(oidcUserService)
                .and()
                .defaultSuccessUrl("/success");
        }
    }


    private static void updateUserGroups(Collection<? extends GrantedAuthority> authorities,
                                         User user,
                                         Set<String> adminRoles,
                                         UserGroupRepository userGroupRepository) {
        boolean isAdmin = authorities
                .stream()
                .anyMatch(a -> adminRoles.contains(a.getAuthority().replace("ROLE_", "")));
        user.setAuthority(isAdmin ? Authority.ROLE_ADMIN : Authority.ROLE_USER);
        user.getGroups().clear();
        UserGroup everyoneUserGroup = userGroupRepository.findByName("Everyone");
        user.getGroups().add(everyoneUserGroup);
        user.getGroups().addAll(authorities.stream().map(a -> {
            String groupName = a.getAuthority().replace("ROLE_", "");

            UserGroup userGroup = userGroupRepository.findByName(groupName);
            if (userGroup == null) {
                userGroup = new UserGroup();
                userGroup.setName(groupName);
                userGroupRepository.save(userGroup);
            }
            return userGroup;
        }).collect(Collectors.toSet()));
    }

}
