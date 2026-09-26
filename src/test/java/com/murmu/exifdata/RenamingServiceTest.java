package com.murmu.exifdata;

import com.drew.imaging.ImageProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RenamingServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void formatsExtensionCorrectly() {
        assertEquals("jpg", RenamingService.getFileExtension(Path.of("photo.jpg")));
        assertEquals("MP4", RenamingService.getFileExtension(Path.of("video.MP4")));
        assertEquals("", RenamingService.getFileExtension(Path.of("README")));
        assertEquals("", RenamingService.getFileExtension(Path.of(".hidden")));
        assertEquals("", RenamingService.getFileExtension(Path.of("photo.")));
    }

    @Test
    void collisionGetsNumericSuffixInsteadOfOverwriting() throws IOException {
        Path source = tempDir.resolve("IMG_001.jpg");
        Path existing = tempDir.resolve("2026-09-26_17-30-00.jpg");
        Files.writeString(source, "source");
        Files.writeString(existing, "existing");

        Path target = RenamingService.findAvailablePath(source, existing.getFileName().toString());

        assertEquals(tempDir.resolve("2026-09-26_17-30-00_1.jpg"), target);
        assertEquals("existing", Files.readString(existing));
    }

    @Test
    void renamesFileUsingExtractedTimestamp() throws Exception {
        Path source = tempDir.resolve("IMG_001.jpg");
        Files.writeString(source, "photo");

        DateTimeExtractor fakeExtractor = file -> Optional.of(Instant.parse("2026-09-26T12:00:00Z"));
        RenamingService service = new RenamingService(
                fakeExtractor,
                "yyyy-MM-dd_HH-mm-ss",
                ZoneId.of("Asia/Kolkata"),
                ZoneId.of("Asia/Kolkata"));

        Optional<Path> result = service.rename(source);

        assertTrue(result.isPresent());
        assertEquals(tempDir.resolve("2026-09-26_17-30-00.jpg"), result.get());
        assertTrue(Files.exists(result.get()));
        assertFalse(Files.exists(source));
        assertEquals("photo", Files.readString(result.get()));
    }

    @Test
    void duplicateTimestampDoesNotOverwriteExistingFile() throws Exception {
        Path source = tempDir.resolve("IMG_001.jpg");
        Path expectedFirst = tempDir.resolve("2026-09-26_17-30-00.jpg");
        Files.writeString(source, "source");
        Files.writeString(expectedFirst, "old file");

        DateTimeExtractor fakeExtractor = file -> Optional.of(Instant.parse("2026-09-26T12:00:00Z"));
        RenamingService service = new RenamingService(
                fakeExtractor,
                "yyyy-MM-dd_HH-mm-ss",
                ZoneId.of("Asia/Kolkata"),
                ZoneId.of("Asia/Kolkata"));

        Path result = service.rename(source).orElseThrow();

        assertEquals(tempDir.resolve("2026-09-26_17-30-00_1.jpg"), result);
        assertEquals("old file", Files.readString(expectedFirst));
        assertEquals("source", Files.readString(result));
    }

    @Test
    void alreadyCorrectlyNamedFileIsLeftInPlace() throws Exception {
        Path source = tempDir.resolve("2026-09-26_17-30-00.jpg");
        Files.writeString(source, "photo");

        DateTimeExtractor fakeExtractor = file -> Optional.of(Instant.parse("2026-09-26T12:00:00Z"));
        RenamingService service = new RenamingService(
                fakeExtractor,
                "yyyy-MM-dd_HH-mm-ss",
                ZoneId.of("Asia/Kolkata"),
                ZoneId.of("Asia/Kolkata"));

        Path result = service.rename(source).orElseThrow();

        assertEquals(source, result);
        assertTrue(Files.exists(source));
        assertEquals("photo", Files.readString(source));
    }

    @Test
    void fileWithoutMetadataIsSkipped() throws Exception {
        Path source = tempDir.resolve("IMG_002.jpg");
        Files.writeString(source, "photo");

        DateTimeExtractor fakeExtractor = file -> Optional.empty();
        RenamingService service = new RenamingService(fakeExtractor, "yyyy-MM-dd_HH-mm-ss");

        assertTrue(service.rename(source).isEmpty());
        assertTrue(Files.exists(source));
    }
}
