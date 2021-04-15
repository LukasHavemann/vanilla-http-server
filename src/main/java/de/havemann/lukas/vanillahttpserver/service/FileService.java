package de.havemann.lukas.vanillahttpserver.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileService {

    public List<String> listDirectory(URI uri) {
        final List<String> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(uri))) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    files.add(path.getFileName().toString() + "/");
                } else {
                    files.add(path.getFileName().toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return files;
    }
}
