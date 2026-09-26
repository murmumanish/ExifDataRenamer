package com.murmu.exifdata;

import com.drew.imaging.ImageProcessingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class ExifDataRenamer {
    private ExifDataRenamer() {
    }

    public record Config(Path folder, String format) {
        public Config {
            if (folder == null) {
                throw new IllegalArgumentException("FolderPath is required.");
            }
            if (format == null || format.isBlank()) {
                throw new IllegalArgumentException("Format cannot be empty.");
            }
            // Validate the pattern once at startup instead of failing halfway through a folder.
            java.time.format.DateTimeFormatter.ofPattern(format);
        }

        public static Config fromArgs(String[] args) {
            if (args == null || args.length == 0 || args[0].isBlank()) {
                throw new IllegalArgumentException("Usage: java -jar ExifDataRenamer.jar <folderPath> [format]");
            }

            String format = args.length > 1 && !args[1].isBlank()
                    ? args[1]
                    : "yyyy-MM-dd_HH-mm-ss";

            return new Config(Path.of(args[0]).toAbsolutePath().normalize(), format);
        }
    }

    public static void main(String[] args) {
        try {
            Config config = Config.fromArgs(args);
            validateFolder(config.folder());

            RenamingService service = new RenamingService(new MetadataDateTimeExtractor(), config.format());
            int renamed = 0;
            int skipped = 0;
            int errors = 0;

            try (Stream<Path> files = Files.list(config.folder())) {
                for (Path file : files.filter(Files::isRegularFile).toList()) {
                    try {
                        var result = service.rename(file);
                        if (result.isPresent()) {
                            System.out.println("File renamed: " + result.get().getFileName());
                            renamed++;
                        } else {
                            System.out.println("No DateTime metadata: " + file.getFileName());
                            skipped++;
                        }
                    } catch (IOException | ImageProcessingException e) {
                        System.err.println("Error processing " + file.getFileName() + ": " + e.getMessage());
                        errors++;
                    }
                }
            }

            System.out.printf("Done. Renamed: %d, skipped: %d, errors: %d%n", renamed, skipped, errors);
        } catch (IllegalArgumentException | IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void validateFolder(Path folder) throws IOException {
        if (!Files.exists(folder)) {
            throw new IllegalArgumentException("Folder does not exist: " + folder);
        }
        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("Path is not a folder: " + folder);
        }
        if (!Files.isReadable(folder)) {
            throw new IOException("Folder is not readable: " + folder);
        }
    }
}
