package de.havemann.lukas.vanillahttp.protocol.specification.etag;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpHeader;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpHeaderField;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * tests for {@link ETagEvaluator}
 */
class ETagEvaluatorTest {

    private static final ZonedDateTime FIX_POINT_IN_TIME =
            ZonedDateTime.of(LocalDateTime.of(2015, 10, 21, 0, 0, 0), ZoneOffset.UTC);

    public static final String NO_REPRESENTATION = "*";
    private Map<String, String> header;
    private ETagEvaluator testee;

    @BeforeEach
    void setup() {
        header = new HashMap<>();
        testee = new ETagEvaluator(new HttpHeader(header));
    }

    @Test
    void noLastModifiedProvidedTest() {
        assertThat(testee.isRequestLastModifiedOutdated(FIX_POINT_IN_TIME.plusDays(10))).isFalse();
        assertThat(testee.isRequestLastModifiedOutdated(FIX_POINT_IN_TIME.minusDays(10))).isFalse();
        assertThat(testee.shouldContentBeSend(null, FIX_POINT_IN_TIME)).isTrue();
    }

    @Test
    void lastModifiedTest() {
        givenRequest(HttpHeaderField.IF_MODIFIED_SINCE, "Wed, 21 Oct 2015 00:00:00 GMT");

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(testee.isRequestLastModifiedOutdated(FIX_POINT_IN_TIME.plusDays(10))).as("outdated").isTrue();
        softly.assertThat(testee.isRequestLastModifiedOutdated(FIX_POINT_IN_TIME)).as("equals test").isFalse();
        softly.assertThat(testee.isRequestLastModifiedOutdated(FIX_POINT_IN_TIME.minusNanos(100))).as("only second precision").isFalse();
        softly.assertThat(testee.isRequestLastModifiedOutdated(FIX_POINT_IN_TIME.minusDays(10))).as("not outdated").isFalse();
        softly.assertAll();
    }

    @Test
    void ifMatchEqualsTest() {
        givenRequest(HttpHeaderField.IF_MATCH, "'ETAG'");
        assertThat(testee.isAnyOfIfMatch(ETag.from("ETAG"))).isTrue();
    }

    @Test
    void ifMatchNoRepresentationTest() {
        givenRequest(HttpHeaderField.IF_MATCH, NO_REPRESENTATION);
        assertThat(testee.isAnyOfIfMatch(new ETag("ETAG", ETag.Kind.STRONG))).isFalse();
        assertThat(testee.isAnyOfIfMatch(new ETag("ETAG", ETag.Kind.WEAK))).isFalse();
    }

    @Test
    void ifMatchWeakCompareTest() {
        givenRequest(HttpHeaderField.IF_MATCH, "'ETAG'");
        assertThat(testee.isAnyOfIfMatch(new ETag("ETAG", ETag.Kind.WEAK))).isFalse();
    }

    @Test
    void ifMatchMultipleTest() {
        givenRequest(HttpHeaderField.IF_MATCH, "'ETAG1', 'ETAG2'");
        assertThat(testee.isAnyOfIfMatch(new ETag("ETAG2", ETag.Kind.STRONG))).isTrue();
        assertThat(testee.isAnyOfIfMatch(new ETag("ETAG2", ETag.Kind.WEAK))).isFalse();
    }

    @Test
    void ifNoneMatchEqualsTest() {
        givenRequest(HttpHeaderField.IF_NONE_MATCH, "'ETAG'");
        assertThat(testee.isAnyOfIfNoneMatch(ETag.from("ETAG"))).isTrue();
    }

    @Test
    void ifNoneMatchNoRepresentationTest() {
        givenRequest(HttpHeaderField.IF_NONE_MATCH, NO_REPRESENTATION);
        assertThat(testee.isAnyOfIfNoneMatch(new ETag("ETAG", ETag.Kind.STRONG))).isFalse();
        assertThat(testee.isAnyOfIfNoneMatch(new ETag("ETAG", ETag.Kind.WEAK))).isFalse();
    }

    @Test
    void ifNoneMatchWeakCompareTest() {
        givenRequest(HttpHeaderField.IF_NONE_MATCH, "'ETAG'");
        assertThat(testee.isAnyOfIfNoneMatch(new ETag("ETAG", ETag.Kind.WEAK))).isTrue();
    }

    @Test
    void ifNoneMatchMultipleTest() {
        givenRequest(HttpHeaderField.IF_NONE_MATCH, "'ETAG1', 'ETAG2'");
        assertThat(testee.isAnyOfIfNoneMatch(new ETag("ETAG2", ETag.Kind.STRONG))).isTrue();
    }


    private void givenRequest(HttpHeaderField httpHeaderField, String value) {
        // replacing ' with " here for readability of test
        value = value.replaceAll("'", "\"");
        header.put(httpHeaderField.getRepresentation(), value);
    }
}