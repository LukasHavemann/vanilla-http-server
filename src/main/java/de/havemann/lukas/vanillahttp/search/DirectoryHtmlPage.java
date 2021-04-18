package de.havemann.lukas.vanillahttp.search;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private final File directory;

    public DirectoryHtmlPage(String directoryName, File directory, OutputStream outputStream) {
        this.directoryName = Objects.requireNonNull(directoryName);
        this.directory = Objects.requireNonNull(directory);
        this.outputStream = Objects.requireNonNull(outputStream);
    }

    public void render() throws IOException {
        outputStream.write(START_PAGE.replaceAll("#directoryPath", directoryName).getBytes(StandardCharsets.UTF_8));
        renderFiles();
        outputStream.write(END_PAGE.getBytes(StandardCharsets.UTF_8));
    }

    private void renderFiles() throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory.toPath())) {
            for (Path path : stream) {
                if (path.toFile().canRead()) {
                    outputStream.write(renderListElement(getFilename(path)));
                }
            }
        }
    }

    private String getFilename(Path path) {
        if (Files.isDirectory(path)) {
            return path.getFileName().toString() + "/";
        }

        return path.getFileName().toString();
    }

    private byte[] renderListElement(String file) {
        return "<li><a href=\"#file\">#file</a>".replaceAll("#file", file).getBytes(StandardCharsets.UTF_8);
    }
}
