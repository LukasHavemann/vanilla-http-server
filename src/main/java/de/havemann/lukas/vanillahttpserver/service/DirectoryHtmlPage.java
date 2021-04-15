package de.havemann.lukas.vanillahttpserver.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class DirectoryHtmlPage {

    public static final String START_PAGE = String.join("\n",
            "<!DOCTYPE html PUBLIC \" -//W3C//DTD HTML 3.2 Final//EN\"><html>",
            "<title>Directory listing for #directoryPath</title>",
            "<body>",
            "<h2>Directory listing for #directoryPath</h2>",
            "<hr>",
            "<ul>");

    public static final String END_PAGE = String.join("\n",
            "</ul>",
            "<hr>",
            "</body>",
            "</html>");


    private final String directoryName;
    private final OutputStream outputStream;

    public DirectoryHtmlPage(String directoryName, OutputStream outputStream) {
        this.directoryName = Objects.requireNonNull(directoryName);
        this.outputStream = Objects.requireNonNull(outputStream);
    }

    public DirectoryHtmlPage render(List<String> files) throws IOException {
        outputStream.write(START_PAGE.replaceAll("#directoryPath", directoryName).getBytes(StandardCharsets.UTF_8));

        for (String file : files) {
            outputStream.write(renderListElement(file));
        }

        outputStream.write(END_PAGE.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    private byte[] renderListElement(String file) {
        return "<li><a href=\"#file\">#file</a>".replaceAll("#file", file).getBytes(StandardCharsets.UTF_8);
    }
}
