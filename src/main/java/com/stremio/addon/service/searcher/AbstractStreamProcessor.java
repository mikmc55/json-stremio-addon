package com.stremio.addon.service.searcher;

import be.adaxisoft.bencode.BDecoder;
import be.adaxisoft.bencode.BEncodedValue;
import be.adaxisoft.bencode.BEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractStreamProcessor {

    // Caché para almacenar los mapas de torrents en función de la URL
    private final Map<String, Map<String, BEncodedValue>> torrentCache = new ConcurrentHashMap<>();

    // Método para obtener el infoHash desde un archivo .torrent
    protected String getInfoHashFromTorrent(String torrentUrl) {
        try {
            Map<String, BEncodedValue> torrentMap = getOrDownloadTorrentMap(torrentUrl);
            return bytesToHex(getInfoHash(torrentMap.get("info").getMap()));
        } catch (Exception e) {
            log.error("Error obtaining infoHash from torrent URL: {}", torrentUrl, e);
            throw new RuntimeException("Error obtaining infoHash from torrent URL: " + torrentUrl, e);
        }
    }

    // Método para obtener los trackers desde un archivo .torrent en formato "tracker:<url>"
    protected List<String> getTrackersFromTorrent(String torrentUrl) {
        try {
            Map<String, BEncodedValue> torrentMap = getOrDownloadTorrentMap(torrentUrl);
            return extractTrackers(torrentMap);
        } catch (Exception e) {
            log.error("Error obtaining trackers from torrent URL: {}", torrentUrl, e);
            throw new RuntimeException("Error obtaining trackers from torrent URL: " + torrentUrl, e);
        }
    }

    // Método para obtener los behaviorHints desde un archivo .torrent
    protected Map<String, Object> getBehaviorHintsFromTorrent(String torrentUrl) {
        try {
            Map<String, BEncodedValue> torrentMap = getOrDownloadTorrentMap(torrentUrl);
            return extractBehaviorHints(torrentMap.get("info").getMap());
        } catch (Exception e) {
            log.error("Error obtaining behaviorHints from torrent URL: {}", torrentUrl, e);
            throw new RuntimeException("Error obtaining behaviorHints from torrent URL: " + torrentUrl, e);
        }
    }

    // Método para obtener el nombre de archivo desde un archivo .torrent
    protected String getFilenameFromTorrent(String torrentUrl) {
        try {
            Map<String, BEncodedValue> torrentMap = getOrDownloadTorrentMap(torrentUrl);
            return extractFilename(torrentMap.get("info").getMap());
        } catch (Exception e) {
            log.error("Error extracting filename from torrent URL: {}", torrentUrl, e);
            throw new RuntimeException("Error extracting filename from torrent URL: " + torrentUrl, e);
        }
    }

    // Método que verifica si el torrent está en caché o lo descarga y almacena en la caché
    private Map<String, BEncodedValue> getOrDownloadTorrentMap(String torrentUrl) {
        return torrentCache.computeIfAbsent(torrentUrl, this::downloadAndDecodeTorrent);
    }

    // Método para descargar y decodificar el archivo .torrent
    private Map<String, BEncodedValue> downloadAndDecodeTorrent(String torrentUrl) {
        try {
            URL url = new URL(torrentUrl);
            HttpURLConnection connection = followRedirects(url);
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

    // Método para extraer los trackers de un mapa Bencoded
    private List<String> extractTrackers(Map<String, BEncodedValue> torrentMap) {
        try {
            List<String> trackerList = new ArrayList<>();
            if (torrentMap.containsKey("announce")) {
                trackerList.add("tracker:" + torrentMap.get("announce").getString());
            }
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
            byte[] infoBytes = BEncoder.encode(infoMap).array();
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

    // Método que extrae los behaviorHints, incluyendo el nombre del archivo y el tamaño total
    private Map<String, Object> extractBehaviorHints(Map<String, BEncodedValue> infoMap) {
        Map<String, Object> behaviorHints = new HashMap<>();
        try {
            if (infoMap.containsKey("bingeGroup")) {
                behaviorHints.put("bingeGroup", infoMap.get("bingeGroup").getString());
            }
            if (infoMap.containsKey("filename")) {
                behaviorHints.put("filename", infoMap.get("filename").getString());
            }
            if (infoMap.containsKey("videoSize")) {
                behaviorHints.put("videoSize", infoMap.get("videoSize").getLong());
            }
            if (infoMap.containsKey("name")) {
                behaviorHints.put("filename", infoMap.get("name").getString());
            }
            behaviorHints.put("videoSize", extractTotalFileSize(infoMap));
            return behaviorHints;
        } catch (Exception e) {
            log.error("Error extracting behaviorHints from torrent info map", e);
            throw new RuntimeException("Error extracting behaviorHints from torrent info map", e);
        }
    }

    // Método que extrae el tamaño total de los archivos en la sección "info"
    private long extractTotalFileSize(Map<String, BEncodedValue> infoMap) {
        try {
            if (infoMap.containsKey("length")) {
                return infoMap.get("length").getLong();
            }
            if (infoMap.containsKey("files")) {
                long totalSize = 0;
                for (BEncodedValue fileEntry : infoMap.get("files").getList()) {
                    totalSize += fileEntry.getMap().get("length").getLong();
                }
                return totalSize;
            }
            throw new RuntimeException("No file size information found in torrent");
        } catch (Exception e) {
            log.error("Error extracting total file size from info map", e);
            throw new RuntimeException("Error extracting total file size from info map", e);
        }
    }

    // Método que extrae el nombre de archivo del mapa de información del torrent
    private String extractFilename(Map<String, BEncodedValue> infoMap) {
        if (infoMap.containsKey("name")) {
            try {
                return infoMap.get("name").getString();
            } catch (Exception e) {
                log.error("Error extracting filename from info map", e);
                throw new RuntimeException("Error extracting filename from info map", e);
            }
        }
        throw new RuntimeException("Filename not found in torrent info map");
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
        return withoutAccents.replaceAll("[^a-zA-Z0-9 ª/-/%]", "");
    }
}
