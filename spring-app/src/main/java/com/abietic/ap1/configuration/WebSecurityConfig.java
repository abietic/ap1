package com.abietic.ap1.configuration;

import java.util.Collections;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Autowired
    private SingleSignOutFilter singleSignOutFilter;
    @Autowired
    private LogoutFilter logoutFilter;
    @Autowired
    private CasAuthenticationProvider casAuthenticationProvider;
    @Autowired
    private ServiceProperties serviceProperties;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests().antMatchers("/api/user/register", "/api/user/getotp", "/api/user/getauthcode").permitAll()
                .antMatchers("/api/user/**", "/api/item/**", "/api/order/**").authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
                .and()
                .addFilterBefore(singleSignOutFilter, CasAuthenticationFilter.class)
                .addFilterBefore(logoutFilter, LogoutFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(casAuthenticationProvider);
    }

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return new ProviderManager(Collections.singletonList(casAuthenticationProvider));
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        CasAuthenticationEntryPoint entryPoint = new CasAuthenticationEntryPoint();
        entryPoint.setLoginUrl("https://localhost/cas/login"); // 当用户使用浏览器访问需要授权的地址时,spring返回给浏览器跳转到CAS的url
        entryPoint.setServiceProperties(serviceProperties);
        return entryPoint;
    }

}
