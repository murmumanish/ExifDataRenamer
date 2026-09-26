package com.murmu.exifdata;

import com.drew.imaging.ImageProcessingException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

@FunctionalInterface
public interface DateTimeExtractor {
    Optional<Instant> extract(Path file) throws IOException, ImageProcessingException;
}
