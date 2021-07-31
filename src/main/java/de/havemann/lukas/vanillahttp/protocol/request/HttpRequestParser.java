package de.havemann.lukas.vanillahttp.protocol.request;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpHeaderField;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpMethod;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import de.havemann.lukas.vanillahttp.protocol.specification.ProtocolRepresentation;
import java.util.StringTokenizer;

/**
 * Simple implementation of a recursive descent parser for HTTP/1.0 and HTTP/1.1 requests.
 */
public class HttpRequestParser {

  private final StringTokenizer tokenizer;
  private final HttpRequest.Builder requestBuilder = new HttpRequest.Builder();

  public HttpRequestParser(String request) {
    this.tokenizer = new StringTokenizer(request, HttpProtocol.DELIMITER);
  }

  public HttpRequest parse() {
    if (!tokenizer.hasMoreTokens()) {
      throw HttpRequestParsingException.Reason.EMPTY_STATUS_LINE.toException();
    }

    parseStatusLine(tokenizer.nextToken());

    parseHttpHeader();
    parseRequestBody();

    return requestBuilder.build();
  }

  private void parseRequestBody() {
    final StringBuilder messageBody = new StringBuilder();
    while (tokenizer.hasMoreTokens()) {
      messageBody.append(tokenizer.nextToken()).append(HttpProtocol.DELIMITER);
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

    final HttpMethod httpMethod = ProtocolRepresentation.detect(HttpMethod.class, currentToken);
    if (httpMethod == null) {
      throw HttpRequestParsingException.Reason.UNSUPPORTED_HTTP_METHOD.toException(currentToken);
    }

    requestBuilder.httpMethod(httpMethod);
  }

  private void parseUri(StringTokenizer statusLineTokens) {
    if (!statusLineTokens.hasMoreTokens()) {
      throw HttpRequestParsingException.Reason.URI_EXPECTED.toException();
    }

    requestBuilder.requestUri(statusLineTokens.nextToken());
  }

  private void parseHttpProtocol(StringTokenizer statusLineTokens) {
    if (!statusLineTokens.hasMoreTokens()) {
      throw HttpRequestParsingException.Reason.HTTP_PROTOCOL_EXPECTED.toException();
    }

    final String currentToken = statusLineTokens.nextToken();
    final HttpProtocol httpProtocol = ProtocolRepresentation
        .detect(HttpProtocol.class, currentToken);
    if (httpProtocol == null) {
      throw HttpRequestParsingException.Reason.UNSUPPORTED_HTTP_PROTOCOL.toException();
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
    int separator = httpHeaderField.indexOf(HttpHeaderField.KEY_VALUE_DELIMITER);
    if (separator == -1) {
      throw HttpRequestParsingException.Reason.INVALID_HTTP_HEADER_FIELD
          .toException(httpHeaderField);
    }

    requestBuilder.addHttpHeader(
        httpHeaderField.substring(0, separator),
        httpHeaderField.substring(separator + HttpHeaderField.KEY_VALUE_DELIMITER.length()));
  }
}