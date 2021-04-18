package de.havemann.lukas.vanillahttp.protocol.specification.etag;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpHeader;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpHeaderField;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


/**
 * Encapsulates the conditional http request logic of <a href="https://tools.ietf.org/html/rfc7232">RFC 7232</a>
 */
public class ETagEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(ETagEvaluator.class);

    private final HttpHeader httpHeader;

    public ETagEvaluator(HttpHeader httpHeader) {
        this.httpHeader = httpHeader;
    }

    public boolean shouldContentBeSend(@Nullable ETag currentETag, @Nullable ZonedDateTime currentLastModified) {
        if (currentETag != null) {
            if (httpHeader.has(HttpHeaderField.IF_NONE_MATCH)) {
                return !isAnyOfIfNoneMatch(currentETag);
            }

            if (httpHeader.has(HttpHeaderField.IF_MATCH)) {
                return !isAnyOfIfMatch(currentETag);
            }
        }
        if (httpHeader.has(HttpHeaderField.IF_MODIFIED_SINCE)) {
            return isRequestLastModifiedOutdated(currentLastModified);
        }
        return true;
    }

    public boolean isRequestLastModifiedOutdated(ZonedDateTime currentLastModified) {
        return getLastModified()
                .map(requestLastModified -> requestLastModified.isBefore(currentLastModified.truncatedTo(ChronoUnit.SECONDS)))
                .orElse(false);
    }

    public boolean isAnyOfIfMatch(ETag etag) {
        return httpHeader.getValues(HttpHeaderField.IF_MATCH)
                .map(ETag::from)
                .anyMatch(a -> ETag.Kind.STRONG.compare(a, etag));
    }

    public boolean isAnyOfIfNoneMatch(ETag etag) {
        return httpHeader.getValues(HttpHeaderField.IF_NONE_MATCH)
                .map(ETag::from)
                .anyMatch(a -> ETag.Kind.WEAK.compare(a, etag));
    }

    public Optional<ZonedDateTime> getLastModified() {
        final Optional<String> lastModified = httpHeader.getValueOf(HttpHeaderField.IF_MODIFIED_SINCE);
        try {
            return lastModified
                    .map(DateTimeFormatter.RFC_1123_DATE_TIME::parse)
                    .map(ZonedDateTime::from);
        } catch (RuntimeException e) {
            LOG.error("Error during parsing of last modified {}", lastModified);
            return Optional.empty();
        }
    }
}
