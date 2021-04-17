package de.havemann.lukas.vanillahttpserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FileService {

    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    @Value("${vanilla.server.basedir}")
    private String basedir;

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
            LOG.error("exception during file read", e);
        }

        return files;
    }

    public File getFile(String uri) {
        final String requestedPath = "./" + basedir + URI.create(uri).getPath();
        LOG.info("requestedPath " + requestedPath);
        final File file = new File(requestedPath);
        if (file.isDirectory()) {
            return Arrays.stream(file.listFiles())
                    .filter(File::isFile)
                    .filter(f -> f.getName().equals("index.html") || f.getName().equals("index.htm"))
                    .findFirst()
                    .orElse(file);
        }
        return file;
    }
}
