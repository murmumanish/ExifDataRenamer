package com.murmu.exifdata;

import com.drew.imaging.ImageProcessingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public final class RenamingService {
    private final DateTimeExtractor dateTimeExtractor;
    private final DateTimeFormatter formatter;
    private final ZoneId photoZone;
    private final ZoneId videoZone;

    public RenamingService(DateTimeExtractor dateTimeExtractor, String format) {
        this(dateTimeExtractor, format, ZoneId.systemDefault(), ZoneId.of("Asia/Kolkata"));
    }

    RenamingService(
            DateTimeExtractor dateTimeExtractor,
            String format,
            ZoneId photoZone,
            ZoneId videoZone) {

        this.dateTimeExtractor = dateTimeExtractor;
        this.formatter = DateTimeFormatter.ofPattern(format, Locale.ROOT);
        this.photoZone = photoZone;
        this.videoZone = videoZone;
    }

    public Optional<Path> rename(Path source) throws IOException, ImageProcessingException {
        if (!Files.isRegularFile(source)) {
            return Optional.empty();
        }

        Optional<Instant> extracted = dateTimeExtractor.extract(source);
        if (extracted.isEmpty()) {
            return Optional.empty();
        }

        Instant instant = extracted.get();
        boolean isVideo = isVideo(source);
        ZonedDateTime dateTime = toDisplayDateTime(instant, isVideo, photoZone, videoZone);
        String formatted = formatter.format(dateTime);
        String extension = getFileExtension(source);
        String baseName = extension.isEmpty() ? formatted : formatted + "." + extension;

        Path target = findAvailablePath(source, baseName);
        if (target.equals(source)) {
            return Optional.of(source);
        }
        // Do not use REPLACE_EXISTING: an unexpected race must not turn into data loss.
        Files.move(source, target);
        return Optional.of(target);
    }

    static ZonedDateTime toDisplayDateTime(
            Instant instant,
            boolean isVideo,
            ZoneId photoZone,
            ZoneId videoZone) {
        return instant.atZone(isVideo ? videoZone : photoZone);
    }

    static Path findAvailablePath(Path source, String fileName) {
        Path target = source.resolveSibling(fileName);
        if (target.equals(source) || !Files.exists(target)) {
            return target;
        }

        String extension = getFileExtension(target);
        String stem = extension.isEmpty()
                ? target.getFileName().toString()
                : target.getFileName().toString().substring(0,
                target.getFileName().toString().length() - extension.length() - 1);

        int counter = 1;
        while (true) {
            String candidateName = extension.isEmpty()
                    ? stem + "_" + counter
                    : stem + "_" + counter + "." + extension;
            Path candidate = target.resolveSibling(candidateName);
            if (!Files.exists(candidate) || candidate.equals(source)) {
                return candidate;
            }
            counter++;
        }
    }

    public static String getFileExtension(Path file) {
        String fileName = file.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1);
    }

    private static boolean isVideo(Path file) {
        String extension = getFileExtension(file).toLowerCase(Locale.ROOT);
        return extension.equals("mp4") || extension.equals("mov") || extension.equals("m4v")
                || extension.equals("3gp") || extension.equals("3g2") || extension.equals("qt");
    }
}
