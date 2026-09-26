package com.murmu.exifdata;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class ExifDataRenamerTest {

    @Test
    void defaultFormatIsUsedWhenOnlyFolderIsProvided() {
        ExifDataRenamer.Config config = ExifDataRenamer.Config.fromArgs(new String[]{"photos"});

        assertEquals(Path.of("photos").toAbsolutePath().normalize(), config.folder());
        assertEquals("yyyy-MM-dd_HH-mm-ss", config.format());
    }

    @Test
    void customFormatIsAccepted() {
        ExifDataRenamer.Config config = ExifDataRenamer.Config.fromArgs(
                new String[]{"photos", "yyyy/MM/dd HH-mm"});

        assertEquals("yyyy/MM/dd HH-mm", config.format());
    }

    @Test
    void missingFolderArgumentIsRejected() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> ExifDataRenamer.Config.fromArgs(new String[]{}));

        assertTrue(error.getMessage().contains("FolderPath is required")
                || error.getMessage().contains("Usage:"));
    }

    @Test
    void invalidDateFormatIsRejectedEarly() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ExifDataRenamer.Config.fromArgs(new String[]{"photos", "[bad"}));
    }

    @Test
    void videoUsesIndiaTimezone() {
        var instant = java.time.Instant.parse("2026-09-26T12:00:00Z");

        var result = RenamingService.toDisplayDateTime(
                instant,
                true,
                ZoneId.of("UTC"),
                ZoneId.of("Asia/Kolkata"));

        assertEquals("2026-09-26T17:30+05:30[Asia/Kolkata]", result.toString());
    }
}
