package de.havemann.lukas.vanillahttpserver.protocol;

import java.util.StringTokenizer;

/**
 * Simple implementation of a recursive descent parser for HTTP/1.0 and HTTP/1.1 request
 */
public class HttpRequestParser {

    public static final String HTTP_LINE_DELIMITER = "\r\n";
    public static final String HTTP_HEADER_FIELD_SEPERATOR = ": ";

    private final StringTokenizer tokenizer;
    private final HttpRequest.Builder requestBuilder = new HttpRequest.Builder();

    public HttpRequestParser(String request) {
        this.tokenizer = new StringTokenizer(request, HTTP_LINE_DELIMITER);
    }

    public HttpRequest parse() {
        if (!tokenizer.hasMoreTokens()) {
            throw new HttpParsingException(HttpParsingException.Reason.EMPTY_STATUS_LINE);
        }

        parseStatusLine(tokenizer.nextToken());

        parseHttpHeader();
        parseRequestBody();

        return requestBuilder.build();
    }

    private void parseRequestBody() {
        final StringBuilder messageBody = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            messageBody.append(tokenizer.nextToken()).append(HTTP_LINE_DELIMITER);
        }

        final String messageBodyResult = messageBody.toString();
        if (!messageBodyResult.isEmpty()) {
            requestBuilder.messageBody(messageBodyResult);
        }
    }

    private void parseStatusLine(String statusLine) {
        final StringTokenizer tokenizer = new StringTokenizer(statusLine);

        parseHttpMethod(tokenizer);
        parseUri(tokenizer);
        parseHttpProtocol(tokenizer);
    }

    private void parseHttpMethod(StringTokenizer statusLineTokens) {
        // no hasMoreTokens(), at least one token should be present.
        final String currentToken = statusLineTokens.nextToken();

        final HttpMethod httpMethod = HttpMethod.detect(currentToken);
        if (httpMethod == null) {
            throw new HttpParsingException(HttpParsingException.Reason.UNSUPPORTED_HTTP_METHOD);
        }

        requestBuilder.httpMethod(httpMethod);
    }

    private void parseUri(StringTokenizer statusLineTokens) {
        if (!statusLineTokens.hasMoreTokens()) {
            throw new HttpParsingException(HttpParsingException.Reason.URI_EXPECTED);
        }

        requestBuilder.requestUri(statusLineTokens.nextToken());
    }

    private void parseHttpProtocol(StringTokenizer statusLineTokens) {
        if (!statusLineTokens.hasMoreTokens()) {
            throw new HttpParsingException(HttpParsingException.Reason.NO_HTTP_PROTOCOL_FOUND);
        }

        final String currentToken = statusLineTokens.nextToken();
        final HttpProtocol httpProtocol = HttpProtocol.detect(currentToken);
        if (httpProtocol == null) {
            throw new HttpParsingException(HttpParsingException.Reason.UNSUPPORTED_HTTP_PROTOCOL);
        }

        requestBuilder.httpProtocol(httpProtocol);
    }

    private void parseHttpHeader() {
        while (tokenizer.hasMoreTokens()) {
            final String httpHeader = tokenizer.nextToken();

            if (httpHeader.isEmpty()) {
                break;
            }

            parseSingleHttpHeaderField(httpHeader);
        }
    }

    private void parseSingleHttpHeaderField(String httpHeaderField) {
        int seperator = httpHeaderField.indexOf(HTTP_HEADER_FIELD_SEPERATOR);
        if (seperator == -1) {
            throw new HttpParsingException(HttpParsingException.Reason.INVALID_HTTP_HEADER_FIELD, httpHeaderField);
        }

        requestBuilder.addHttpHeader(
                httpHeaderField.substring(0, seperator),
                httpHeaderField.substring(seperator + HTTP_HEADER_FIELD_SEPERATOR.length()));
    }
}