package org.codehaus.mojo.wagon;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Serves {@link #getDirectoryToServe()} over HTTP with the JDK's built-in server: files as they are, and directories as
 * an HTML list of links, which is what wagon-http parses for the {@code list} and {@code download} goals.
 */
public abstract class AbstractHttpServerIT extends AbstractMavenIT {
    private HttpServer server;

    @BeforeEach
    public void startServer() throws IOException {
        Path root = getDirectoryToServe().toAbsolutePath().normalize();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            try {
                serve(root, exchange);
            } finally {
                exchange.close();
            }
        });
        server.start();
    }

    @AfterEach
    public void stopServer() {
        server.stop(0);
    }

    protected int getServerPort() {
        return server.getAddress().getPort();
    }

    protected abstract Path getDirectoryToServe();

    private static void serve(Path root, HttpExchange exchange) throws IOException {
        String requested = URI.create(exchange.getRequestURI().getRawPath()).getPath();
        Path path = root.resolve(requested.substring(1)).normalize();
        if (!path.startsWith(root) || !Files.exists(path)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }
        if (Files.isDirectory(path) && !requested.endsWith("/")) {
            // relative links in the listing need the trailing slash
            exchange.getResponseHeaders().set("Location", requested + "/");
            exchange.sendResponseHeaders(301, -1);
            return;
        }

        byte[] body;
        if (Files.isDirectory(path)) {
            StringBuilder html = new StringBuilder("<html><body>\n");
            try (Stream<Path> children = Files.list(path)) {
                List<Path> sorted = children.sorted().collect(Collectors.toList());
                for (Path child : sorted) {
                    String name = child.getFileName() + (Files.isDirectory(child) ? "/" : "");
                    html.append("<a href=\"")
                            .append(name)
                            .append("\">")
                            .append(name)
                            .append("</a><br>\n");
                }
            }
            body = html.append("</body></html>\n").toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        } else {
            body = Files.readAllBytes(path);
            exchange.getResponseHeaders()
                    .set(
                            "Last-Modified",
                            DateTimeFormatter.RFC_1123_DATE_TIME.format(
                                    Files.getLastModifiedTime(path).toInstant().atZone(ZoneOffset.UTC)));
        }

        if ("HEAD".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(body.length));
            exchange.sendResponseHeaders(200, -1);
            return;
        }
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
