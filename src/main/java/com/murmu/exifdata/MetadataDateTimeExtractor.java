package com.murmu.exifdata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mp4.Mp4Directory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

public final class MetadataDateTimeExtractor implements DateTimeExtractor {

    @Override
    public Optional<Instant> extract(Path filePath) throws IOException, ImageProcessingException {
        try (InputStream inputStream = new FileInputStream(filePath.toFile())) {
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);

            return getDateTime(metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
                    .or(() -> getDateTime(metadata, Mp4Directory.class, Mp4Directory.TAG_CREATION_TIME))
                    .or(() -> getDateTime(metadata, ExifIFD0Directory.class, ExifIFD0Directory.TAG_DATETIME))
                    .map(Date::toInstant);
        }
    }

    private static Optional<Date> getDateTime(
            Metadata metadata,
            Class<? extends Directory> directoryClass,
            int tagType) {

        Directory directory = metadata.getFirstDirectoryOfType(directoryClass);
        if (directory == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(directory.getDate(tagType));
    }
}
