package ch.admin.bit.jeap.initializer.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Slf4j
public class FileUtils {

    private FileUtils() {
    }

    public static void replaceInFiles(String fileNamePatternString, Path projectRoot, String original, String replacement) {
        Pattern fileNamePattern = Pattern.compile(fileNamePatternString, Pattern.CASE_INSENSITIVE);
        walkMatchingFiles(projectRoot, fileNamePattern, matchingFile -> {
            String originalContent = Files.readString(matchingFile);
            String updatedContent = originalContent.replace(original, replacement);
            if (!originalContent.equals(updatedContent)) {
                Files.writeString(matchingFile, updatedContent);
            }
        });
    }

    public static void removeRegionInFiles(Pattern fileNamePattern, Path projectRoot, String regionStart, String regionEnd) {
        FileProcessor fileProcessor = file -> {
            List<String> lines = Files.readAllLines(file);
            final AtomicBoolean inRegion = new AtomicBoolean(false);
            List<String> updatedLines = lines.stream()
                    .filter(line -> {
                        if (line.contains(regionStart)) {
                            inRegion.set(true);
                            return false;
                        }
                        if (line.contains(regionEnd)) {
                            inRegion.set(false);
                            return false;
                        }
                        return !inRegion.get();
                    }).toList();

            if (updatedLines.size() < lines.size()) {
                Files.write(file, updatedLines);
            }
        };
        walkMatchingFiles(projectRoot, fileNamePattern, fileProcessor);
    }

    public static void deleteFilesContainingMarker(Path projectRoot, Pattern fileNamePattern, String marker) {
        FileProcessor fileProcessor = file -> {
            String content = Files.readString(file);
            if (content.contains(marker)) {
                Files.delete(file);
            }
        };
        walkMatchingFiles(projectRoot, fileNamePattern, fileProcessor);
    }

    public static void walkMatchingFiles(Path projectRoot, Pattern fileNamePattern, FileProcessor fileProcessor) {
        try (var paths = Files.walk(projectRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> fileNamePattern.matcher(path.getFileName().toString()).matches())
                    .toList().forEach(matchingFile -> processFile(fileProcessor, matchingFile));
        } catch (IOException e) {
            throw FileProcessingException.ioException(e);
        }
    }

    private static void processFile(FileProcessor fileProcessor, Path matchingFile) {
        try {
            fileProcessor.process(matchingFile);
        } catch (MalformedInputException e) {
            // Ignore non-text files
        } catch (IOException e) {
            throw FileProcessingException.ioException(e);
        }
    }

    public static void deleteMatchingLines(Path projectRoot, Pattern sourceFilesPattern, Pattern removedLinePattern) {
        FileProcessor fileProcessor = file -> {
            List<String> lines = Files.readAllLines(file);
            int size = lines.size();

            // Remove all lines that match the pattern
            lines.removeIf(line -> removedLinePattern.matcher(line).matches());

            // Write updated content if changed
            if (lines.size() < size) {
                String content = String.join("\n", lines);
                Files.writeString(file, content);
            }
        };
        walkMatchingFiles(projectRoot, sourceFilesPattern, fileProcessor);
    }

    public static void replaceInFilesRegex(Pattern filePattern, Path projectRoot, String regex, String replacement) {
        walkMatchingFiles(projectRoot, filePattern, matchingFile -> {
            String originalContent = Files.readString(matchingFile);
            String updatedContent = originalContent.replaceAll(regex, replacement);
            if (!originalContent.equals(updatedContent)) {
                Files.writeString(matchingFile, updatedContent);
            }
        });
    }

    public static void overwriteFileContent(String fileName, Path projectRoot, String text) {
        Pattern fileNamePattern = Pattern.compile(Pattern.quote(fileName), Pattern.CASE_INSENSITIVE);
        AtomicInteger overwrittenFiles = new AtomicInteger(0);

        walkMatchingFiles(projectRoot, fileNamePattern, matchingFile -> {
            Files.writeString(matchingFile, text);
            overwrittenFiles.incrementAndGet();
        });

        if (overwrittenFiles.get() == 0) {
            try {
                Path targetFile = projectRoot.resolve(fileName);
                Path parent = targetFile.getParent();
                if (parent != null) {
                    Path newDirectory = Files.createDirectories(parent);
                    log.debug("Folder [{}] has been created.", newDirectory);
                }
                Files.writeString(targetFile, text);
            } catch (IOException e) {
                log.error("Write in the File [{}], in the folder [{}], is not possible.", fileName, projectRoot);
                throw FileProcessingException.ioException(e);
            }
        }
    }
}
