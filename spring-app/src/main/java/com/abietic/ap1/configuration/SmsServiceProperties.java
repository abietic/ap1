package com.abietic.ap1.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:sms.properties")
@ConfigurationProperties(prefix = "sms")
public class SmsServiceProperties {
    private String smsSdkAppId;

    private String smsSignName = "abietic的个人日志";

    private String smsTemplateId;

    private String smsRegion;

    private String smsSecretId;

    private String smsSecretKey;

    /**
     * @return the smsSdkAppId
     */
    public String getSmsSdkAppId() {
        return smsSdkAppId;
    }

    /**
     * @param smsSdkAppId the smsSdkAppId to set
     */
    public void setSmsSdkAppId(String smsSdkAppId) {
        this.smsSdkAppId = smsSdkAppId;
    }

    /**
     * @return the smsSignName
     */
    public String getSmsSignName() {
        return smsSignName;
    }

    /**
     * @param smsSignName the smsSignName to set
     */
    public void setSmsSignName(String smsSignName) {
        this.smsSignName = smsSignName;
    }

    /**
     * @return the smsTemplateId
     */
    public String getSmsTemplateId() {
        return smsTemplateId;
    }

    /**
     * @param smsTemplateId the smsTemplateId to set
     */
    public void setSmsTemplateId(String smsTemplateId) {
        this.smsTemplateId = smsTemplateId;
    }

    /**
     * @return the smsRegion
     */
    public String getSmsRegion() {
        return smsRegion;
    }

    /**
     * @param smsRegion the smsRegion to set
     */
    public void setSmsRegion(String smsRegion) {
        this.smsRegion = smsRegion;
    }

    /**
     * @return the smsSecretId
     */
    public String getSmsSecretId() {
        return smsSecretId;
    }

    /**
     * @param smsSecretId the smsSecretId to set
     */
    public void setSmsSecretId(String smsSecretId) {
        this.smsSecretId = smsSecretId;
    }

    /**
     * @return the smsSecretKey
     */
    public String getSmsSecretKey() {
        return smsSecretKey;
    }

    /**
     * @param smsSecretKey the smsSecretKey to set
     */
    public void setSmsSecretKey(String smsSecretKey) {
        this.smsSecretKey = smsSecretKey;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    
    @Override
    public String toString() {
        return "SmsServiceProperties [smsRegion=" + smsRegion + ", smsSdkAppId=" + smsSdkAppId + ", smsSecretId="
                + smsSecretId + ", smsSecretKey=" + smsSecretKey + ", smsSignName=" + smsSignName + ", smsTemplateId="
                + smsTemplateId + "]";
    }

}
