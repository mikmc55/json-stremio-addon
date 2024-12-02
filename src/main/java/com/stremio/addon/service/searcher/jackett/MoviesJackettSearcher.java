package com.stremio.addon.service.searcher.jackett;

import com.stremio.addon.configuration.AddonConfiguration;
import com.stremio.addon.controller.dto.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service("movieJackett")
@Scope("prototype")
public class MoviesJackettSearcher extends AbstractJackettSearcher {

    protected MoviesJackettSearcher(AddonConfiguration addonConfiguration, @Qualifier("restTemplateXml") RestTemplate restTemplate) {
        super(addonConfiguration, restTemplate);
    }

    @Override
    public List<Stream> search(String title, String... args) {

        return searchStreams(2000, title);
    }


}
