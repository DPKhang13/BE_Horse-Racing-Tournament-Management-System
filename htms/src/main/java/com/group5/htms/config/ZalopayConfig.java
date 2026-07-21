package com.group5.htms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "zalopay")
public class ZalopayConfig {
    private String appId;
    private String key1;
    private String key2;
    private String createUrl;
    private String queryUrl;
    private String redirectUrl;
    private String callbackUrl;
}
