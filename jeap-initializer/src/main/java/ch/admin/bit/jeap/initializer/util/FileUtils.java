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

    /**
     * Replaces all occurrences of a literal {@code original} string with {@code replacement} in all files
     * whose file name matches {@code fileNamePatternString} under {@code projectRoot}.
     * <p>
     * This method performs a simple, literal string replacement (it does <strong>not</strong> interpret
     * {@code original} as a regular expression).
     * <p>
     * Files are only written back if the replacement changes the content, avoiding unnecessary writes.
     *
     * @param fileNamePatternString a regular expression applied to the file name (case-insensitive)
     * @param projectRoot           the root directory to search recursively
     * @param original              the literal text to replace
     * @param replacement           the literal replacement text
     *
     * @throws FileProcessingException if walking the tree or reading/writing a text file fails
     */
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

    /**
     * Removes a line-oriented region from matching files.
     * <p>
     * For each matching file, the content is read as lines and then filtered:
     * <ul>
     *   <li>When a line contains {@code regionStart}, that line is removed and subsequent lines are considered
     *       inside the region.</li>
     *   <li>While inside the region, lines are removed.</li>
     *   <li>When a line contains {@code regionEnd}, that line is removed and the region ends.</li>
     * </ul>
     * In other words, both marker lines are removed, and everything between them is removed as well.
     * <p>
     * If multiple regions exist, they are removed independently in a single pass.
     * <p>
     * The file is only rewritten if at least one line was removed.
     *
     * <h3>Important</h3>
     * If {@code regionStart} is found without a later {@code regionEnd}, then all remaining lines to the end of
     * the file will be removed. If {@code regionEnd} appears before any {@code regionStart}, it will simply be
     * removed and processing continues.
     *
     * @param fileNamePattern pattern applied to file names to select which files to process
     * @param projectRoot     root directory to search recursively
     * @param regionStart     substring that marks the first line of the region to remove
     * @param regionEnd       substring that marks the last line of the region to remove
     *
     * @throws FileProcessingException if walking the tree or reading/writing a text file fails
     */
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

    /**
     * Deletes any file (under {@code projectRoot}) whose file name matches {@code fileNamePattern} and whose
     * textual content contains the given {@code marker} substring.
     * <p>
     * This is intended for template cleanups where a marker indicates a file should be removed entirely.
     * <p>
     * Non-text files (i.e., those failing with {@link MalformedInputException}) are ignored.
     *
     * @param projectRoot      root directory to search recursively
     * @param fileNamePattern  pattern applied to file names to select candidate files
     * @param marker           substring whose presence triggers deletion
     *
     * @throws FileProcessingException if walking the tree, reading a file, or deleting a file fails
     */
    public static void deleteFilesContainingMarker(Path projectRoot, Pattern fileNamePattern, String marker) {
        FileProcessor fileProcessor = file -> {
            String content = Files.readString(file);
            if (content.contains(marker)) {
                Files.delete(file);
            }
        };
        walkMatchingFiles(projectRoot, fileNamePattern, fileProcessor);
    }

    /**
     * Walks the directory tree below {@code projectRoot} and applies {@code fileProcessor} to each regular file
     * whose <em>file name</em> matches {@code fileNamePattern}.
     * <p>
     * The walk is recursive and uses .
     * <p>
     * Processing behavior:
     * <ul>
     *   <li>Only regular files are considered (directories, symlinks-to-directories, etc. are skipped).</li>
     *   <li>The pattern is applied to {@code path.getFileName().toString()}.</li>
     *   <li>Each matching file is processed via {@link #processFile(FileProcessor, Path)} to centralize error handling.</li>
     * </ul>
     *
     * @param projectRoot     root directory to walk recursively
     * @param fileNamePattern pattern to match against the file name
     * @param fileProcessor   callback applied to each matching file
     *
     * @throws FileProcessingException if walking the tree fails
     */
    public static void walkMatchingFiles(Path projectRoot, Pattern fileNamePattern, FileProcessor fileProcessor) {
        try (var paths = Files.walk(projectRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> fileNamePattern.matcher(path.getFileName().toString()).matches())
                    .toList().forEach(matchingFile -> processFile(fileProcessor, matchingFile));
        } catch (IOException e) {
            throw FileProcessingException.ioException(e);
        }
    }

    /**
     * Processes a single file using the provided {@link FileProcessor}, applying common exception handling.
     * <p>
     * {@link MalformedInputException} is swallowed to allow directory walks to skip binary or otherwise non-decodable
     * files when the processor expects text input.
     *
     * @param fileProcessor the processor to execute
     * @param matchingFile  the file to process
     *
     * @throws FileProcessingException if processing fails with an {@link IOException} other than
     *                                 {@link MalformedInputException}
     */
    private static void processFile(FileProcessor fileProcessor, Path matchingFile) {
        try {
            fileProcessor.process(matchingFile);
        } catch (MalformedInputException e) {
            // Ignore non-text files
        } catch (IOException e) {
            throw FileProcessingException.ioException(e);
        }
    }

    /**
     * Deletes (removes) all lines in matching files whose full line content matches {@code removedLinePattern}.
     * <p>
     * The method is line-based:
     * <ul>
     *   <li>Reads all lines using {@link Files#readAllLines(Path)}.</li>
     *   <li>Removes lines for which {@code removedLinePattern.matcher(line).matches()} is {@code true}
     *       (i.e., the pattern must match the entire line unless you use {@code .*} yourself).</li>
     *   <li>If any line was removed, writes the updated content back.</li>
     * </ul>
     *
     * <h3>Line endings</h3>
     * When writing, lines are joined with {@code "\n"} regardless of the original line separators, which may
     * normalize CRLF to LF.
     *
     * @param projectRoot         root directory to search recursively
     * @param sourceFilesPattern  pattern applied to file names to select which files to process
     * @param removedLinePattern  pattern used to decide which lines to remove (must match the whole line)
     *
     * @throws FileProcessingException if walking the tree or reading/writing a text file fails
     */
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

    /**
     * Performs a regular-expression-based replacement in all matching files.
     * <p>
     * For each file whose name matches {@code filePattern}, reads the complete file content and applies
     * {@link String#replaceAll(String, String)} using the provided {@code regex}.
     * <p>
     * Files are only written back if the content changes.
     *
     * <h3>Regex semantics</h3>
     * {@code regex} is a Java regular expression. The {@code replacement} string uses
     * {@link java.util.regex.Matcher} replacement rules (e.g., {@code $1} for capture groups and escaping with {@code \}).
     *
     * @param filePattern  pattern applied to file names to select which files to process
     * @param projectRoot  root directory to search recursively
     * @param regex        Java regular expression to replace
     * @param replacement  replacement string (may refer to capture groups)
     *
     * @throws FileProcessingException if walking the tree or reading/writing a text file fails
     */
    public static void replaceInFilesRegex(Pattern filePattern, Path projectRoot, String regex, String replacement) {
        walkMatchingFiles(projectRoot, filePattern, matchingFile -> {
            String originalContent = Files.readString(matchingFile);
            String updatedContent = originalContent.replaceAll(regex, replacement);
            if (!originalContent.equals(updatedContent)) {
                Files.writeString(matchingFile, updatedContent);
            }
        });
    }

    /**
     * Overwrites the content of a file named {@code fileName} with {@code text}.
     * <p>
     * Behavior:
     * <ol>
     *   <li>Searches for any existing file(s) under {@code projectRoot} whose file name equals {@code fileName}
     *       (case-insensitive) and overwrites each match with {@code text}.</li>
     *   <li>If no matching file exists, creates {@code projectRoot.resolve(fileName)} (including parent directories)
     *       and writes {@code text} to that new file.</li>
     * </ol>
     *
     * <h3>Multiple matches</h3>
     * If the same file name exists in multiple subdirectories, all of them will be overwritten.
     * This is intentional for bulk template operations, but callers should be aware of this behavior.
     *
     * @param fileName     the file name to overwrite (matched case-insensitively against leaf file names)
     * @param projectRoot  root directory to search recursively and/or create the file in
     * @param text         the content to write
     *
     * @throws FileProcessingException if writing fails or required directories cannot be created
     */
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
