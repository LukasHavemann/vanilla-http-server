package de.havemann.lukas.vanillahttp.protocol.specification.etag;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

/**
 * tests for {@link ETag}
 */
class ETagTest {

    @Test
    void getRepresentationTest() {
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ETag.NO_REPRESENTATION.getRepresentation()).isEqualTo("\"*\"");
        softly.assertThat(new ETag("TEST", ETag.Kind.STRONG).getRepresentation()).isEqualTo("\"TEST\"");
        softly.assertThat(new ETag("TEST", ETag.Kind.WEAK).getRepresentation()).isEqualTo("W/\"TEST\"");
        softly.assertAll();
    }

    @Test
    void fromTest() {
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ETag.from("\"*\"")).isEqualTo(ETag.NO_REPRESENTATION);
        softly.assertThat(ETag.from("\"TEST\"")).extracting(ETag::getKind).isEqualTo(ETag.Kind.STRONG);
        softly.assertThat(ETag.from("W/\"TEST\"")).extracting(ETag::getKind).isEqualTo(ETag.Kind.WEAK);
        softly.assertThat(ETag.from("W/\"TEST\"")).extracting(ETag::getData).isEqualTo("TEST");
        softly.assertAll();
    }

    /**
     * +--------+--------+-------------------+-----------------+
     * | ETag 1 | ETag 2 | Strong Comparison | Weak Comparison |
     * +--------+--------+-------------------+-----------------+
     * | W/"1"  | W/"1"  | no match          | match           |
     * | W/"1"  | W/"2"  | no match          | no match        |
     * | W/"1"  | "1"    | no match          | match           |
     * | "1"    | "1"    | match             | match           |
     * +--------+--------+-------------------+-----------------+
     */
    @Test
    void eTagComparisonStrategyTest() {
        final ETag eWea1 = new ETag("1", ETag.Kind.WEAK);
        final ETag eWea2 = new ETag("2", ETag.Kind.WEAK);
        final ETag eStr1 = new ETag("1", ETag.Kind.STRONG);

        final SoftAssertions softlyStrong = new SoftAssertions();
        softlyStrong.assertThat(ETag.Kind.STRONG.compare(eWea1, eWea1)).isFalse();
        softlyStrong.assertThat(ETag.Kind.STRONG.compare(eWea1, eWea2)).isFalse();
        softlyStrong.assertThat(ETag.Kind.STRONG.compare(eWea1, eStr1)).isFalse();
        softlyStrong.assertThat(ETag.Kind.STRONG.compare(eStr1, eStr1)).isTrue();
        softlyStrong.assertAll();


        final SoftAssertions softlyWeak = new SoftAssertions();
        softlyWeak.assertThat(ETag.Kind.WEAK.compare(eWea1, eWea1)).isTrue();
        softlyWeak.assertThat(ETag.Kind.WEAK.compare(eWea1, eWea2)).isFalse();
        softlyWeak.assertThat(ETag.Kind.WEAK.compare(eWea1, eStr1)).isTrue();
        softlyWeak.assertThat(ETag.Kind.WEAK.compare(eStr1, eStr1)).isTrue();
        softlyWeak.assertAll();
    }
}