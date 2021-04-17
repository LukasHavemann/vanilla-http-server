package de.havemann.lukas.vanillahttpserver.protocol.specification;

import java.util.*;

/**
 * List of common media-types with corresponding filetypes. For full list see
 * {@see https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types}
 */
public enum MediaType implements ProtocolRepresentation {

    ASCII_TEXT("text/plain", "txt"),
    HTML("text/html", "html", "htm"),
    JSON("application/json", "json"),
    JAVASCRIPT("text/javascript,", "js"),
    JPEG_IMAGES("image/jpeg", "jpg", "jpeg"),
    PDF("application/pdf", "pdf"),
    CSS("text/css", "css"),
    XML("application/xml", "xml"),
    ZIP("application/zip", "zip"),
    UNKNOWN("application/octet-stream");

    private final String mediaTypeRepresentation;
    private final List<String> extensions;

    MediaType(String mediaTypeRepresentation, String... extensions) {
        this.mediaTypeRepresentation = mediaTypeRepresentation;
        this.extensions = Collections.unmodifiableList(Arrays.asList(extensions));
    }

    @Override
    public String getRepresentation() {
        return mediaTypeRepresentation;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public static class LookupTable {

        private static final Map<String, MediaType> FILE_EXTENSION_TO_MEDIA_TYPE = new HashMap<>();

        static {
            for (MediaType mediaType : MediaType.values()) {
                for (String extension : mediaType.getExtensions()) {
                    FILE_EXTENSION_TO_MEDIA_TYPE.put(extension, mediaType);
                }
            }
        }

        public static MediaType getByFileExtension(String fileExtension) {
            return FILE_EXTENSION_TO_MEDIA_TYPE.getOrDefault(fileExtension, MediaType.UNKNOWN);
        }
    }
}
