package xyz.bolitao.springsecuritydemo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
//        return NoOpPasswordEncoder.getInstance();
    }

//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication()
//                .withUser("bolitao")
//                .password("bolitao")
//                .roles("USER", "ADMIN")
//                .and()
//                .withUser("test1")
//                .password("test")
//                .roles("USER");
//    }


    @Autowired
    DataSource dataSource;

    @Override
    @Bean
    protected UserDetailsService userDetailsService() {
        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
        if (!userDetailsManager.userExists("bolitao")) {
            userDetailsManager.createUser(
                    User.withUsername("bolitao").password(passwordEncoder().encode("bolitao")).roles("ADMIN").build());
        }
        if (!userDetailsManager.userExists("test1")) {
            userDetailsManager.createUser(
                    User.withUsername("test1").password(passwordEncoder().encode("test")).roles("USER").build());
        }
        return userDetailsManager;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/image/**", "/css/**", "/js/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasRole("USER")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("passwd")
                .successHandler((req, resp, authentication) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter writer = resp.getWriter();
                    writer.write(new ObjectMapper().writeValueAsString(authentication.getPrincipal()));
                    writer.flush();
                    writer.close();
                })
                .failureHandler((req, resp, exception) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter writer = resp.getWriter();
                    String exceptionMessage = exception.getMessage();
                    if (exception instanceof LockedException) {
                        exceptionMessage = "账户被锁";
                    } else if (exception instanceof CredentialsExpiredException) {
                        exceptionMessage = "密码过期";
                    } else if (exception instanceof AccountExpiredException) {
                        exceptionMessage = "账户过期";
                    } else if (exception instanceof DisabledException) {
                        exceptionMessage = "账户已被禁用";
                    } else if (exception instanceof BadCredentialsException) {
                        exceptionMessage = "用户名或密码错误";
                    }
                    writer.write(new ObjectMapper().writeValueAsString(exceptionMessage));
                    writer.flush();
                    writer.close();
                })
                .permitAll()
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessHandler((req, resp, authentication) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter writer = resp.getWriter();
                    writer.write(new ObjectMapper().writeValueAsString("注销成功"));
                    writer.flush();
                    writer.close();
                })
                .and()
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(
                (req, resp, exception) -> {
                    resp.setStatus(HttpStatus.UNAUTHORIZED.value());
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter writer = resp.getWriter();
                    writer.write(new ObjectMapper().writeValueAsString("尚未登陆"));
                    writer.flush();
                    writer.close();
                });
    }

    @Bean
    RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return roleHierarchy;
    }
}
