package de.havemann.lukas.vanillahttp.protocol.specification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * List of common media-types with corresponding filetypes. For full list see {@see
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types}
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
    this.extensions = List.of(extensions);
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

    /**
     * Determines the {@link MediaType} instance to the given file extension. If the file extension
     * is unknown, {@link MediaType#UNKNOWN} will be returned.
     */
    public static MediaType getByFileExtension(String fileExtension) {
      return FILE_EXTENSION_TO_MEDIA_TYPE.getOrDefault(fileExtension, MediaType.UNKNOWN);
    }
  }
}
