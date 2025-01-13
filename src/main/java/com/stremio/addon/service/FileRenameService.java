package com.stremio.addon.service;

import com.stremio.addon.configuration.AddonConfiguration;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;

@Service
public class FileRenameService {
    private final AddonConfiguration addonConfiguration;

    private static final String[] VIDEO_EXTENSIONS = {"mp4", "mkv", "avi", "mov", "flv", "wmv"};

    public FileRenameService(AddonConfiguration addonConfiguration) {
        this.addonConfiguration = addonConfiguration;
    }

    public String renameFileForPlex(String originalPath, String type, String title, int yearOrSeason, Integer episode) throws Exception {
        File originalFile = resolveFile(originalPath, title);

        if (!originalFile.exists()) {
            throw new IllegalArgumentException("El archivo o directorio no existe: " + originalPath);
        }

        String newFileName = generateNewFileName(type, title, yearOrSeason, episode, getFileExtension(originalFile));
        Path newPath = Path.of(newFileName);

        // Verificar que el path destino exista, y si no, crearlo
        createDirectoriesIfNotExist(newPath.getParent());

        Files.move(originalFile.toPath(), newPath, StandardCopyOption.REPLACE_EXISTING);
        return newPath.toString();
    }

    private File resolveFile(String originalPath, String title) throws Exception {
        File file = new File(originalPath);

        if (file.isDirectory()) {
            return findVideoFileInDirectory(file, title)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró un archivo de video en el directorio: " + originalPath));
        }

        return file;
    }

    private Optional<File> findVideoFileInDirectory(File directory, String title) {
        return Arrays.stream(directory.listFiles())
                .filter(file -> isValidVideoFile(file, title))
                .findFirst();
    }

    private boolean isValidVideoFile(File file, String title) {
        String fileName = file.getName().toLowerCase();
        return !file.isDirectory() &&
                fileName.startsWith(title.toLowerCase()) &&
                Arrays.stream(VIDEO_EXTENSIONS).anyMatch(fileName::endsWith);
    }

    private String generateNewFileName(String type, String title, int yearOrSeason, Integer episode, String extension) {
        String targetPath = addonConfiguration.getVideosPath();

        if ("movie".equalsIgnoreCase(type)) {
            // Crear directorio para la película con el nombre y el año
            return String.format("%s/movies/%s (%d)/%s (%d)%s",
                    targetPath,
                    sanitizeName(title),
                    yearOrSeason,
                    sanitizeName(title),
                    yearOrSeason,
                    extension);
        } else if ("series".equalsIgnoreCase(type)) {
            // Crear directorio de la serie y de la temporada
            return String.format("%s/series/%s/Season %02d/%s - S%02dE%02d%s",
                    targetPath,
                    sanitizeName(title),
                    yearOrSeason,
                    sanitizeName(title),
                    yearOrSeason,
                    episode,
                    extension);
        } else {
            throw new IllegalArgumentException("Tipo no soportado: " + type);
        }
    }

    private void createDirectoriesIfNotExist(Path directoryPath) throws Exception {
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
    }

    private String sanitizeName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "").trim();
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        return lastIndex == -1 ? "" : name.substring(lastIndex);
    }
}
