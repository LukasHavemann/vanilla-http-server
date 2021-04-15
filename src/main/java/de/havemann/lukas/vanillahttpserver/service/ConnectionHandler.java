package de.havemann.lukas.vanillahttpserver.service;

import de.havemann.lukas.vanillahttpserver.protocol.request.HttpRequest;
import de.havemann.lukas.vanillahttpserver.protocol.request.HttpRequestBuffer;
import de.havemann.lukas.vanillahttpserver.protocol.request.HttpRequestParsingException;
import de.havemann.lukas.vanillahttpserver.protocol.response.HttpResponseHeader;
import de.havemann.lukas.vanillahttpserver.protocol.response.HttpResponseWriter;
import de.havemann.lukas.vanillahttpserver.protocol.specification.HttpHeaderField;
import de.havemann.lukas.vanillahttpserver.protocol.specification.HttpProtocol;
import de.havemann.lukas.vanillahttpserver.protocol.specification.HttpStatusCode;
import de.havemann.lukas.vanillahttpserver.protocol.specification.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ConnectionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionHandler.class);

    private final InputStream inputStream;
    private final OutputStream outputstream;
    private final FileService fileService;
    private HttpStatusCode statusCode = HttpStatusCode.OK;
    private HttpRequest request;

    public ConnectionHandler(InputStream inputStream, OutputStream outputstream, FileService fileService) {
        this.inputStream = inputStream;
        this.outputstream = outputstream;
        this.fileService = fileService;
    }

    public ConnectionHandler readRequest() throws IOException {
        try {
            request = new HttpRequestBuffer().consume(inputStream);
            LOG.info("Received {} request", request.getHttpProtocol());
        } catch (HttpRequestParsingException ex) {
            statusCode = HttpStatusCode.BAD_REQUEST;
        }
        return this;
    }

    public ConnectionHandler dispatchRequestTo() {
        return this;
    }

    public ConnectionHandler writeResponse() throws IOException {
        final HttpResponseWriter httpResponseWriter = new HttpResponseWriter(outputstream);
        HttpResponseHeader.Builder builder = new HttpResponseHeader.Builder()
                .protocol(HttpProtocol.HTTP_1_1)
                .statusCode(statusCode);

        final File file = new File("./" + request.getUri());
        if (file.isDirectory()) {
            httpResponseWriter.writeHeader(builder.build());
            DirectoryHtmlPage directoryHtmlPage = new DirectoryHtmlPage(file.toURI().toString(), outputstream);
            directoryHtmlPage.render(fileService.listDirectory(file.toURI()));
        } else {
            MediaType mediaType = MediaType.LookupTable.getByFileExtension(getFileExtension(file));
            httpResponseWriter.writeHeader(builder.add(HttpHeaderField.CONTENT_TYPE, mediaType.getRepresentation()).build());
            httpResponseWriter.streamDataFrom(new FileInputStream(file));
        }


        httpResponseWriter.finish();

        return this;
    }

    private static String getFileExtension(File file) {
        final String fileName = file.getName();
        int fileExtensionsPoint = fileName.lastIndexOf(".");
        if (fileExtensionsPoint != -1 && fileExtensionsPoint != 0) {
            return fileName.substring(fileExtensionsPoint + 1);
        }

        return "";
    }
}
