package com.abietic.ap1.configuration;

import java.util.Collection;
import java.util.Collections;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import com.abietic.ap1.mapper.UserMapper;
import com.abietic.ap1.mapper.UserRoleMapper;
import com.abietic.ap1.service.model.UserModel;

@Component
public class CASBeanFactory {

    private static final Logger logger = LoggerFactory.getLogger(CASBeanFactory.class);

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserRoleMapper userRoleMapper;

    @Bean
    public CasAuthenticationFilter casAuthenticationFilter(
            AuthenticationManager authenticationManager,
            ServiceProperties serviceProperties) throws Exception {
        CasAuthenticationFilter filter = new CasAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setServiceProperties(serviceProperties);
        return filter;
    }

    @Bean
    public ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        // 这里的服务是spring-app即cas-client负责的服务地址,应该使用browser进行访问的地址,因为这个是cas进行进行redirect时给出的地址
        serviceProperties.setService("http://localhost/login/cas");
        serviceProperties.setSendRenew(false);
        return serviceProperties;
    }

    @Bean
    public TicketValidator ticketValidator() {
        // 从源代码里看出这里使用的是后台的真正地址,这个是地址是spring-app要访问cas-server认证service ticket ST的
        return new Cas30ServiceTicketValidator("http://cas-server:8089/cas");
    }

    @Bean
    public CasAuthenticationProvider casAuthenticationProvider(
            TicketValidator ticketValidator,
            ServiceProperties serviceProperties) {
        CasAuthenticationProvider provider = new CasAuthenticationProvider();
        provider.setServiceProperties(serviceProperties);
        provider.setTicketValidator(ticketValidator);
        provider.setUserDetailsService(
                userName -> {
                    com.abietic.ap1.model.User userInfo = userMapper.selectByTelphone(userName);
                    UserModel userModel = new UserModel();
                    BeanUtils.copyProperties(userInfo, userModel);
                    // TODO 加入user的role使其能返回
                    com.abietic.ap1.model.UserRole userRole = userRoleMapper.selectByUserId(userInfo.getId());
                    userModel.defineAuthorities(AuthorityUtils.createAuthorityList(userRole.getRole()));
                    return userModel;
                });
        provider.setKey("CAS_PROVIDER_LOCALHOST_8900");
        return provider;
    }

    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        return new SecurityContextLogoutHandler();
    }

    @Bean
    public LogoutFilter logoutFilter() {
        LogoutFilter logoutFilter = new LogoutFilter("https://localhost/cas/logout", securityContextLogoutHandler());
        logoutFilter.setFilterProcessesUrl("/logout/cas");
        return logoutFilter;
    }

    @Bean
    public SingleSignOutFilter singleSignOutFilter() {
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setLogoutCallbackPath("/exit/cas");
        singleSignOutFilter.setIgnoreInitConfiguration(true);
        return singleSignOutFilter;
    }

}
