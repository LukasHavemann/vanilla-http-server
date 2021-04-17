package de.havemann.lukas.vanillahttp.protocol.specification;

import java.nio.charset.StandardCharsets;

/**
 * Represents a standardized protocol element
 */
public interface ProtocolRepresentation {

    /**
     * Determines whether the passed token belongs to the given protocol elements
     *
     * @param enumClass to determine possible protocol elements from
     * @param token     to check
     * @param <T>       Type of enum-class with possible protocol elements
     * @return null, if no matching protocol element can be found
     */
    static <T extends ProtocolRepresentation> T detect(Class<T> enumClass, String token) {
        final T[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null) {
            throw new IllegalArgumentException("static helper method cannot be called on non enum class");
        }

        for (T element : enumConstants) {
            if (element.getRepresentation().equals(token)) {
                return element;
            }
        }

        return null;
    }

    /**
     * @return String-Representation of protocol element
     */
    default String getRepresentation() {
        return this.toString();
    }

    /**
     * @return byte[] of protocol representation of given protocol element
     */
    default byte[] asUTF8Bytes() {
        return getRepresentation().getBytes(StandardCharsets.UTF_8);
    }
}
