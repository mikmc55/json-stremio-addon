package com.stremio.addon.configuration;


import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.http.MediaType;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Configuration
@EnableJdbcRepositories(basePackages = "com.stremio.addon")
@Data
public class AddonConfiguration {
    private String name;
    @Value("${addon.searchers.file.path}")
    private String storageDir;
    @Value("${addon.jackett.apiKey}")
    private String apiKey;

    @Bean("restTemplateJson")
    public RestTemplate restTemplateJson() {
        return new RestTemplate();
    }

    @Bean("restTemplateXml")
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(xmlMessageConverter());
        return restTemplate;
    }

    @Bean
    public MappingJackson2XmlHttpMessageConverter xmlMessageConverter() {
        XmlMapper xmlMapper = new XmlMapper();
        MappingJackson2XmlHttpMessageConverter xmlConverter = new MappingJackson2XmlHttpMessageConverter(xmlMapper);

        // Añade soporte para application/rss+xml
        xmlConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_XML,
                MediaType.TEXT_XML,
                MediaType.parseMediaType("application/rss+xml")
        ));

        return xmlConverter;
    }

}
