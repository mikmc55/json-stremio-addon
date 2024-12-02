package com.stremio.addon;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.stremio.addon.service.searcher.jackett.dto.CapsInfo;
import com.stremio.addon.service.searcher.jackett.dto.TorrentRss;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

public class TestXmlDeserialization {

    public static void main(String[] args) {
        XmlMapper xmlMapper = new XmlMapper();
        try {
            var classPathResource = new ClassPathResource("/caps-info.xml");

            // Supongamos que tienes un archivo XML llamado "rss_feed.xml"
            CapsInfo torrentRss = xmlMapper.readValue(classPathResource.getInputStream(), CapsInfo.class);
            System.out.println(torrentRss);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}