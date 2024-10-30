package com.stremio.addon.service.searcher;

import be.adaxisoft.bencode.BDecoder;
import be.adaxisoft.bencode.BEncodedValue;
import be.adaxisoft.bencode.BEncoder;
import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.model.TorrentSearcher;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.web.util.UriUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractTorrentSearcher implements TorrentSearcherStrategy, InterfaceTorrentSearcher {

    private TorrentSearcher torrentSearcher;

    public AbstractTorrentSearcher() {
    }

    @Override
    public String getSearchUrl(String title) {
        try {
            log.info("Searching title [{}]", title);
            String encodedId = UriUtils.encode(title, StandardCharsets.UTF_8);
            return getUrl() + getSearchPath() + encodedId;
        } catch (Exception e) {
            log.error("Error encoding search URL for title: {}", title, e);
            throw new RuntimeException("Error encoding search URL for title: " + title, e);
        }
    }

    @Override
    public void initialize(TorrentSearcher torrentSearcher) {
        this.torrentSearcher = torrentSearcher;
    }

    protected String getUrl() {
        return torrentSearcher.getUrl();
    }

    // Método para obtener el mapa Bencoded del archivo .torrent desde una URL
    private Map<String, BEncodedValue> getTorrentMap(String torrentUrl) {
        try {
            URL url = new URL(torrentUrl);
            HttpURLConnection connection = followRedirects(url);

            // Verificar si la respuesta es 200 (OK)
            try (InputStream inputStream = connection.getInputStream()) {
                BDecoder decoder = new BDecoder(inputStream);
                return decoder.decodeMap().getMap();
            }
        } catch (Exception e) {
            log.error("Error decoding torrent file from URL: {}", torrentUrl, e);
            throw new RuntimeException("Error downloading or decoding torrent file from URL: " + torrentUrl, e);
        }
    }

    // Método que sigue redirecciones si es necesario
    private HttpURLConnection followRedirects(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int status = connection.getResponseCode();

            if (status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_MOVED_TEMP) {
                String newUrl = connection.getHeaderField("Location");
                log.info("Redirecting to: {}", newUrl);
                return (HttpURLConnection) new URL(newUrl).openConnection();
            }

            if (status != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed to download torrent file, HTTP response code: " + status);
            }

            return connection;
        } catch (Exception e) {
            log.error("Error following redirects for URL: {}", url, e);
            throw new RuntimeException("Error following redirects for URL: " + url, e);
        }
    }

    // Método para obtener el infoHash desde un archivo .torrent
    private String getInfoHashFromTorrent(String torrentUrl) {
        try {
            Map<String, BEncodedValue> torrentMap = getTorrentMap(torrentUrl);
            return bytesToHex(getInfoHash(torrentMap.get("info").getMap()));
        } catch (Exception e) {
            log.error("Error obtaining infoHash from torrent URL: {}", torrentUrl, e);
            throw new RuntimeException("Error obtaining infoHash from torrent URL: " + torrentUrl, e);
        }
    }

    // Método para obtener los trackers desde un archivo .torrent en formato "tracker:<url>"
    private List<String> getTrackersFromTorrent(String torrentUrl) {
        try {
            Map<String, BEncodedValue> torrentMap = getTorrentMap(torrentUrl);
            return extractTrackers(torrentMap);
        } catch (Exception e) {
            log.error("Error obtaining trackers from torrent URL: {}", torrentUrl, e);
            throw new RuntimeException("Error obtaining trackers from torrent URL: " + torrentUrl, e);
        }
    }

    // Método que extrae los trackers de un mapa Bencoded
    private List<String> extractTrackers(Map<String, BEncodedValue> torrentMap) {
        try {
            List<String> trackerList = new ArrayList<>();

            // Extraer el campo "announce"
            if (torrentMap.containsKey("announce")) {
                trackerList.add("tracker:" + torrentMap.get("announce").getString());
            }

            // Extraer la lista de trackers "announce-list"
            if (torrentMap.containsKey("announce-list")) {
                for (BEncodedValue announceList : torrentMap.get("announce-list").getList()) {
                    for (BEncodedValue trackerEntry : announceList.getList()) {
                        trackerList.add("tracker:" + trackerEntry.getString());
                    }
                }
            }

            return trackerList;
        } catch (Exception e) {
            log.error("Error extracting trackers from torrent map", e);
            throw new RuntimeException("Error extracting trackers from torrent map", e);
        }
    }

    // Método para obtener el infoHash del mapa "info"
    private byte[] getInfoHash(Map<String, BEncodedValue> infoMap) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] infoBytes = BEncoder.encode(infoMap).array();  // Necesitas una clase BEncoder para convertir el mapa en bytes
            return digest.digest(infoBytes);
        } catch (Exception e) {
            log.error("Error obtaining infoHash from info map", e);
            throw new RuntimeException("Error obtaining infoHash from info map", e);
        }
    }

    // Método para convertir bytes a formato hexadecimal
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }



    protected String normalizeText(String input) {
        if (input == null) {
            return null;
        }

        // Primero, reemplazamos las vocales acentuadas por sus equivalentes sin acento
        String withoutAccents = input
                .replaceAll("[áÁ]", "a")
                .replaceAll("[éÉ]", "e")
                .replaceAll("[íÍ]", "i")
                .replaceAll("[óÓ]", "o")
                .replaceAll("[úÚüÜ]", "u");

        // Luego, eliminamos todos los caracteres especiales (deja solo letras y números)
        return withoutAccents.replaceAll("[^a-zA-Z0-9 ª/-]", "");
    }

    // Método para generar la URL basada en el path
    protected String getUrl(String path) {
        try {
            String encodedId = UriUtils.encode(path, StandardCharsets.UTF_8);
            return torrentSearcher.getUrl() + "/" + path;
        } catch (Exception e) {
            log.error("Error encoding URL for path: {}", path, e);
            throw new RuntimeException("Error encoding URL for path: " + path, e);
        }
    }

    // Método para invocar una URL y obtener el documento HTML con JSoup
    protected Document invokeUrl(String url) {
        try {
            return Jsoup.connect(url)
                    .header("Referer", torrentSearcher.getUrl()) // Agregar Referer aquí
                    .get();
        } catch (Exception e) {
            log.error("Error invoking URL: {}", url, e);
            throw new RuntimeException("Error invoking URL: " + url, e);
        }
    }

    // Método para obtener los behaviorHints desde un archivo .torrent
    private Map<String, Object> getBehaviorHintsFromTorrent(String torrentUrl) {
        try {
            Map<String, BEncodedValue> torrentMap = getTorrentMap(torrentUrl);
            return extractBehaviorHints(torrentMap.get("info").getMap());
        } catch (Exception e) {
            log.error("Error obtaining behaviorHints from torrent URL: {}", torrentUrl, e);
            throw new RuntimeException("Error obtaining behaviorHints from torrent URL: " + torrentUrl, e);
        }
    }

    // Método que extrae los behaviorHints, incluyendo el nombre del archivo y el tamaño total
    private Map<String, Object> extractBehaviorHints(Map<String, BEncodedValue> infoMap) {
        Map<String, Object> behaviorHints = new HashMap<>();
        try {
            // Extraer bingeGroup (si está presente)
            if (infoMap.containsKey("bingeGroup")) {
                behaviorHints.put("bingeGroup", infoMap.get("bingeGroup").getString());
            }

            // Extraer filename (si está presente)
            if (infoMap.containsKey("filename")) {
                behaviorHints.put("filename", infoMap.get("filename").getString());
            }

            // Extraer videoSize (si está presente)
            if (infoMap.containsKey("videoSize")) {
                behaviorHints.put("videoSize", infoMap.get("videoSize").getLong());
            }

            // Extraer nombre del archivo o directorio principal
            if (infoMap.containsKey("name")) {
                behaviorHints.put("filename", infoMap.get("name").getString());
            }

            // Extraer el tamaño total del archivo o archivos
            long totalFileSize = extractTotalFileSize(infoMap);
            behaviorHints.put("videoSize", totalFileSize);

            return behaviorHints;
        } catch (Exception e) {
            log.error("Error extracting behaviorHints from torrent info map", e);
            throw new RuntimeException("Error extracting behaviorHints from torrent info map", e);
        }
    }

    // Método que extrae el tamaño total de los archivos en la sección "info"
    private long extractTotalFileSize(Map<String, BEncodedValue> infoMap) {
        try {
            // Si es un torrent de un solo archivo
            if (infoMap.containsKey("length")) {
                return infoMap.get("length").getLong(); // Tamaño en bytes de un archivo único
            }

            // Si es un torrent de múltiples archivos
            if (infoMap.containsKey("files")) {
                long totalSize = 0;
                List<BEncodedValue> filesList = infoMap.get("files").getList();

                for (BEncodedValue fileEntry : filesList) {
                    Map<String, BEncodedValue> fileMap = fileEntry.getMap();
                    totalSize += fileMap.get("length").getLong(); // Sumar tamaño de cada archivo
                }

                return totalSize; // Tamaño total en bytes
            }

            throw new RuntimeException("No file size information found in torrent");
        } catch (Exception e) {
            log.error("Error extracting total file size from info map", e);
            throw new RuntimeException("Error extracting total file size from info map", e);
        }
    }

    private String getFilenameFromTorrent(String torrentUrl) {
        try {
            Map<String, BEncodedValue> torrentMap = getTorrentMap(torrentUrl);
            return extractFilename(torrentMap.get("info").getMap());
        } catch (Exception e) {
            log.error("Error extracting filename from torrent URL: {}", torrentUrl, e);
            throw new RuntimeException("Error extracting filename from torrent URL: " + torrentUrl, e);
        }
    }

    // Método que extrae el nombre de archivo del mapa de información del torrent
    private String extractFilename(Map<String, BEncodedValue> infoMap) {
        if (infoMap.containsKey("name")) {
            try {
                String filename = infoMap.get("name").getString();
                log.info("Extracted filename: {}", filename);
                return filename;
            } catch (Exception e) {
                log.error("Error extracting filename from info map", e);
                throw new RuntimeException("Error extracting filename from info map", e);
            }
        }
        throw new RuntimeException("Filename not found in torrent info map");
    }

    protected List<Stream> generateStreams(String title, List<String> torrentLinks) {
        List<Stream> streams = new ArrayList<>();
        if (!torrentLinks.isEmpty()) {
            int idx = 0;
            for (String torrentLink : torrentLinks) {
                log.info("Torrent link found for movie: {}", title);
                log.info("[{}]", torrentLink);

                Stream stream = new Stream();
                stream.setName(torrentSearcher.getName());
                stream.setDescription(getFilenameFromTorrent(torrentLink));
                stream.setInfoHash(getInfoHashFromTorrent(torrentLink));
                stream.setSources(getTrackersFromTorrent(torrentLink));
                stream.setBehaviorHints(getBehaviorHintsFromTorrent(torrentLink));
                streams.add(stream);
            }
        }
        return streams;
    }
}
