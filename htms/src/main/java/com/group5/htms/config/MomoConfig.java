package com.group5.htms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "momo")
public class MomoConfig {
    private String partnerCode;
    private String accessKey;
    private String secretKey;
    private String createUrl;
    private String queryUrl;
    private String redirectUrl;
    private String ipnUrl;
    private String requestType = "captureWallet";
}
