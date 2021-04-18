package de.havemann.lukas.vanillahttp.search;

import de.havemann.lukas.vanillahttp.protocol.specification.MediaType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Service
class FilesystemContentSearchService implements ContentSearchService {

    private static final Logger LOG = LoggerFactory.getLogger(FilesystemContentSearchService.class);

    @DataSizeUnit(DataUnit.BYTES)
    @Value("${vanilla.server.filesystem.maxInMemory}")
    private DataSize maxInMemoryFileSize;

    @Value("${vanilla.server.filesystem.basedir}")
    private String basedir;

    public Response fetch(String uri) {
        try {
            return internalFetch(uri);
        } catch (IOException ex) {
            LOG.error("error during processing of search", ex);
        }

        return new InMemoryResponse(Result.ERROR);
    }

    @NotNull
    private Response internalFetch(String uri) throws IOException {
        final File file = getFile(uri);

        if (!file.exists()) {
            return new InMemoryResponse(Result.NOT_FOUND);
        }

        if (!file.canRead() || !isInsideBaseDir(file)) {
            return new InMemoryResponse(Result.PERMISSION_DENIED);
        }

        if (file.isDirectory()) {
            return new InMemoryResponse(Result.FOUND, MediaType.HTML, renderDirectoryPage(file));
        }

        return new FileSystemResponse(Result.FOUND, file);
    }

    private boolean isInsideBaseDir(File file) {
        final Path requestedPath = file.toPath().toAbsolutePath().normalize();
        final Path baseDir = new File(basedir).toPath().toAbsolutePath().normalize();
        return requestedPath.startsWith(baseDir);
    }

    private byte[] renderDirectoryPage(File file) throws IOException {
        final ByteArrayOutputStream byteData = new ByteArrayOutputStream();
        new DirectoryHtmlPage(file, byteData).render();
        return byteData.toByteArray();
    }

    private File getFile(String uri) {
        final String requestedPath = basedir + URI.create(uri).getPath();

        LOG.debug(requestedPath);

        final File file = new File(requestedPath);
        if (file.isDirectory()) {
            return findIndexFileIn(file).orElse(file);
        }

        return file;
    }

    @NotNull
    private Optional<File> findIndexFileIn(File file) {
        return Arrays.stream(file.listFiles())
                .filter(File::isFile)
                .filter(f -> f.getName().equals("index.html") || f.getName().equals("index.htm"))
                .findFirst();
    }

    private static MessageDigest getMessageDigest() {
        try {
            // md5 is pretty fast. security ist not needed
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("algorithm not found");
        }
    }

    class FileSystemResponse implements Response {
        private final Result result;
        private final File file;
        private final MediaType mediaType;
        private final ZonedDateTime lastModified;
        private InputStream inputStream;

        public FileSystemResponse(Result result, File file) throws IOException {
            this.result = Objects.requireNonNull(result);
            this.file = Objects.requireNonNull(file);
            this.lastModified = Files.getLastModifiedTime(file.toPath()).toInstant().atZone(ZoneOffset.UTC);
            this.mediaType = getMediaType(file);
            this.inputStream = new BufferedInputStream(new FileInputStream(file));
        }

        public Result getResult() {
            return result;
        }

        public Optional<MediaType> getMediaType() {
            return Optional.of(mediaType);
        }

        public Optional<InputStream> getInputStream() {
            return Optional.of(inputStream);
        }

        @Override
        public Optional<byte[]> getHash() throws IOException {
            // performance optimization. do not stream file twice.
            if (file.length() < maxInMemoryFileSize.toBytes()) {
                return loadFileIntoMemoryAndCalculateHash();
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("big file request for {} with size of {}", file.getName(), DataSize.of(file.length(), DataUnit.BYTES));
            }
            return calculateHashStreamed();
        }

        @Override
        public Optional<ZonedDateTime> getLastModified() {
            return Optional.ofNullable(lastModified);
        }

        @NotNull
        private Optional<byte[]> loadFileIntoMemoryAndCalculateHash() throws IOException {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            this.inputStream.transferTo(outputStream);
            final byte[] buffer = outputStream.toByteArray();
            // replace inputstream with In-Memory to prevent double load
            this.inputStream = new ByteArrayInputStream(buffer);
            return Optional.of(getMessageDigest().digest(buffer));
        }

        @NotNull
        private Optional<byte[]> calculateHashStreamed() throws IOException {
            final MessageDigest messageDigest = getMessageDigest();
            try (InputStream firstStream = this.inputStream) {
                while (firstStream.available() != 0) {
                    messageDigest.digest(firstStream.readNBytes((int) maxInMemoryFileSize.toBytes()));
                }
            }

            this.inputStream = new BufferedInputStream(new FileInputStream(file));
            return Optional.of(messageDigest.digest());
        }

        private MediaType getMediaType(File file) {
            if (file.isDirectory()) {
                return MediaType.HTML;
            }

            return MediaType.LookupTable.getByFileExtension(getFileExtension(file));
        }

        private String getFileExtension(File file) {
            final String fileName = file.getName();
            int fileExtensionsPoint = fileName.lastIndexOf(".");
            if (fileExtensionsPoint != -1 && fileExtensionsPoint != 0) {
                return fileName.substring(fileExtensionsPoint + 1);
            }

            return "";
        }
    }

    static class InMemoryResponse implements Response {

        private final Result result;
        private final MediaType mediaType;
        private final byte[] inMemory;

        public InMemoryResponse(Result result, MediaType mediaType, byte[] inMemory) {
            this.result = Objects.requireNonNull(result);
            this.mediaType = mediaType;
            this.inMemory = inMemory;
        }

        public InMemoryResponse(Result result) {
            this(result, null, null);
        }

        public Result getResult() {
            return result;
        }

        public Optional<MediaType> getMediaType() {
            return Optional.ofNullable(mediaType);
        }

        public Optional<InputStream> getInputStream() {
            return Optional.ofNullable(inMemory).map(ByteArrayInputStream::new);
        }

        @Override
        public Optional<byte[]> getHash() {
            return Optional.of(getMessageDigest().digest(inMemory));
        }

        @Override
        public Optional<ZonedDateTime> getLastModified() {
            // Currently no caching of generated content or heuristics implemented to detect change of folder content
            return Optional.empty();
        }
    }
}
