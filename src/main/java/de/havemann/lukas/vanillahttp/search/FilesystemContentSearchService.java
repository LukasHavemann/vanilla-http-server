package de.havemann.lukas.vanillahttp.search;

import de.havemann.lukas.vanillahttp.protocol.specification.MediaType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Service
class FilesystemContentSearchService implements ContentSearchService {

    private static final Logger LOG = LoggerFactory.getLogger(FilesystemContentSearchService.class);

    @Value("${vanilla.server.basedir}")
    private String basedir;

    private static String getFileExtension(File file) {
        final String fileName = file.getName();
        int fileExtensionsPoint = fileName.lastIndexOf(".");
        if (fileExtensionsPoint != -1 && fileExtensionsPoint != 0) {
            return fileName.substring(fileExtensionsPoint + 1);
        }

        return "";
    }

    public Response fetch(String uri) {
        try {
            return internalFetch(uri);
        } catch (IOException ex) {
            LOG.error("error during processing of search", ex);
        }

        return new ResponseImpl(Result.ERROR);
    }

    @NotNull
    private ResponseImpl internalFetch(String uri) throws IOException {
        final File file = getFile(uri);

        if (!file.exists()) {
            return new ResponseImpl(Result.NOT_FOUND);
        }

        if (!file.canRead()) {
            return new ResponseImpl(Result.PERMISSION_DENIED);
        }

        if (file.isDirectory()) {
            return new ResponseImpl(Result.FOUND, MediaType.HTML, renderDirectoryPage(file));
        }

        return new ResponseImpl(Result.FOUND, getMediaType(file), new FileInputStream(file));
    }

    @NotNull
    private ByteArrayInputStream renderDirectoryPage(File file) throws IOException {
        final ByteArrayOutputStream byteData = new ByteArrayOutputStream();
        new DirectoryHtmlPage(file, byteData).render();
        return new ByteArrayInputStream(byteData.toByteArray());
    }

    private MediaType getMediaType(File file) {
        if (file.isDirectory()) {
            return MediaType.HTML;
        }

        return MediaType.LookupTable.getByFileExtension(getFileExtension(file));
    }

    private File getFile(String uri) {
        final String requestedPath = basedir + URI.create(uri).getPath();
        LOG.info(requestedPath);
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

    static class ResponseImpl implements Response {
        private final MediaType mediaType;
        private final Result result;
        private final InputStream inputStream;

        public ResponseImpl(Result result) {
            this(result, null, null);
        }

        public ResponseImpl(Result result, MediaType mediaType, InputStream inputStream) {
            this.result = Objects.requireNonNull(result);
            this.mediaType = mediaType;
            this.inputStream = inputStream;
        }

        public Result getResult() {
            return result;
        }

        public Optional<MediaType> getMediaType() {
            return Optional.ofNullable(mediaType);
        }

        public Optional<InputStream> getInputStream() {
            return Optional.ofNullable(inputStream);
        }

        @Override
        public String toString() {
            return "ResponseImpl{" +
                    "mediaType=" + mediaType +
                    ", result=" + result +
                    ", inputStream=" + inputStream +
                    '}';
        }
    }
}
