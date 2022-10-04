package com.abietic.ap1.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "spring.cluster-cache-redis")
public class ClusterRedisProperties {
    private String password;
    private List<String> hosts;
    private List<Integer> ports;
    private Integer maxRedirects;
    
    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    /**
     * @return the hosts
     */
    public List<String> getHosts() {
        return hosts;
    }
    /**
     * @param hosts the hosts to set
     */
    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }
    /**
     * @return the ports
     */
    public List<Integer> getPorts() {
        return ports;
    }
    /**
     * @param ports the ports to set
     */
    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }
    /**
     * @return the maxRedirects
     */
    public Integer getMaxRedirects() {
        return maxRedirects;
    }
    /**
     * @param maxRedirects the maxRedirects to set
     */
    public void setMaxRedirects(Integer maxRedirects) {
        this.maxRedirects = maxRedirects;
    }
}
