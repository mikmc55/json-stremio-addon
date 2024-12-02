package com.stremio.addon.service.searcher.jackett;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.stremio.addon.configuration.AddonConfiguration;
import com.stremio.addon.controller.dto.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service("seriesJackett")
@Scope("prototype")
public class SeriesJackettSearcher extends AbstractJackettSearcher {

    protected SeriesJackettSearcher(AddonConfiguration addonConfiguration, @Qualifier("restTemplateXml") RestTemplate restTemplate) {
        super(addonConfiguration, restTemplate);
    }

    @Override
    public List<Stream> search(String title, String... args) {
        String season = args[0];
        String episode = args[1];
        return searchStreams(5000, title, season, episode);
    }
}
