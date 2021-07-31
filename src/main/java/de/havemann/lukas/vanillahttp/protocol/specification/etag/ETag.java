package de.havemann.lukas.vanillahttp.protocol.specification.etag;

import de.havemann.lukas.vanillahttp.protocol.specification.ProtocolRepresentation;
import java.util.Base64;
import java.util.Objects;
import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;

public class ETag implements ProtocolRepresentation {

  public static final String WEAK_TAG_PREFIX = "W/";
  public static final ETag NO_REPRESENTATION = new ETag("*", Kind.STRONG);
  private final Kind kind;
  private final String data;

  /**
   * Encodes data to BASE64
   *
   * @param data etag data
   * @param kind type of etag
   */
  public ETag(byte[] data, Kind kind) {
    this(Base64.getEncoder().encodeToString(data), kind);
  }

  public ETag(String data, Kind kind) {
    this.data = Objects.requireNonNull(data);
    this.kind = Objects.requireNonNull(kind);
  }

  /**
   * Parses etag string representation
   */
  public static ETag from(String eTagValue) {
    final String normalizedETag = removeQuotes(eTagValue);
    if (eTagValue.startsWith(WEAK_TAG_PREFIX)) {
      return new ETag(removeQuotes(normalizedETag.substring(WEAK_TAG_PREFIX.length())), Kind.WEAK);
    }

    if (eTagValue.equals(NO_REPRESENTATION.getData())) {
      return NO_REPRESENTATION;
    }

    return new ETag(normalizedETag, Kind.STRONG);
  }

  @NotNull
  private static String removeQuotes(String value) {
    if (value.startsWith("\"") && value.endsWith("\"")) {
      return value.substring(1, value.length() - 1);
    }

    return value;
  }

  public String getRepresentation() {
    if (kind == Kind.WEAK) {
      return WEAK_TAG_PREFIX + '"' + data + '"';
    }

    return '"' + data + '"';
  }

  public Kind getKind() {
    return kind;
  }

  public String getData() {
    return data;
  }

  @Override
  public String toString() {
    return "ETag{" +
        "kind=" + kind +
        ", data='" + data + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
      if (this == o) {
          return true;
      }
      if (o == null || getClass() != o.getClass()) {
          return false;
      }
    ETag eTag = (ETag) o;
    return kind == eTag.kind && data.equals(eTag.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kind, data);
  }

  /**
   * Representing the different kind of ETag in RFC 7232
   */
  public enum Kind {

    /**
     * A "weak validator" is representation metadata that might not change for every change to the
     * representation data.  This weakness might be due to limitations in how the value is
     * calculated, such as clock resolution, an inability to ensure uniqueness for all possible
     * representations of the resource, or a desire of the resource owner to group representations
     * by some self-determined set of equivalency rather than unique sequences of data.
     */
    WEAK((ETag a, ETag b) -> {
      return a.getData().equals(b.getData());
    }),

    /**
     * A "strong validator" is representation metadata that changes value whenever a change occurs
     * to the representation data that would be observable in the payload body of a 200 (OK)
     * response to GET.
     */
    STRONG((ETag a, ETag b) -> {
      if (a.getKind() == Kind.WEAK || b.getKind() == Kind.WEAK) {
        return false;
      }

      return a.getData().equals(b.getData());
    });

    private final BiFunction<ETag, ETag, Boolean> compareStrategy;

    Kind(BiFunction<ETag, ETag, Boolean> compareStrategy) {
      this.compareStrategy = compareStrategy;
    }

    /**
     * Compares ETag according to <a href=https://tools.ietf.org/html/rfc7232#section-2.3.2">comparison
     * strategy</a>
     * <p>
     * <code>
     * +--------+--------+-------------------+-----------------+ | ETag 1 | ETag 2 | Strong
     * Comparison | Weak Comparison | +--------+--------+-------------------+-----------------+ |
     * W/"1"  | W/"1"  | no match          | match           | | W/"1"  | W/"2"  | no match
     * | no match        | | W/"1"  | "1"    | no match          | match           | | "1"    | "1"
     * | match             | match           | +--------+--------+-------------------+-----------------+
     * </code>
     *
     * @param a ETag 1
     * @param b Etag 2
     * @return true, if match
     */
    public boolean compare(ETag a, ETag b) {
      return compareStrategy.apply(a, b);
    }
  }
}
