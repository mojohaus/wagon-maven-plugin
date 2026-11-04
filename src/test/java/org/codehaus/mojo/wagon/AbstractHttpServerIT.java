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
 * an HTML list of links, which is what a wagon-http that can list parses for the {@code list} and {@code download}
 * goals. It also answers the {@code PROPFIND} requests of a WebDAV client ({@code dav:http://} URLs) with a minimal
 * {@code multistatus} response.
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
        if ("PROPFIND".equals(exchange.getRequestMethod())) {
            propfind(path, requested, exchange);
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

    /**
     * Answers a PROPFIND like a real WebDAV server: the resource first, then with {@code Depth: 1} its children, a
     * directory with {@code <collection/>} and a file with an empty {@code resourcetype}. As WsgiDAV does, it ignores a
     * trailing slash on a file.
     */
    private static void propfind(Path path, String requested, HttpExchange exchange) throws IOException {
        byte[] buffer = new byte[1024];
        while (exchange.getRequestBody().read(buffer) >= 0) {
            // drain the request
        }

        StringBuilder xml =
                new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<D:multistatus xmlns:D=\"DAV:\">\n");
        appendResponse(xml, path, requested);
        if (Files.isDirectory(path) && !"0".equals(exchange.getRequestHeaders().getFirst("Depth"))) {
            String base = requested.endsWith("/") ? requested : requested + "/";
            try (Stream<Path> children = Files.list(path)) {
                for (Path child : children.sorted().collect(Collectors.toList())) {
                    appendResponse(xml, child, base + child.getFileName());
                }
            }
        }
        byte[] body = xml.append("</D:multistatus>\n").toString().getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/xml; charset=utf-8");
        exchange.sendResponseHeaders(207, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private static void appendResponse(StringBuilder xml, Path path, String href) {
        boolean directory = Files.isDirectory(path);
        if (directory && !href.endsWith("/")) {
            href += "/";
        }
        xml.append("<D:response><D:href>")
                .append(href)
                .append("</D:href><D:propstat><D:prop><D:displayname>")
                .append(path.getFileName())
                .append("</D:displayname><D:resourcetype>")
                .append(directory ? "<D:collection/>" : "")
                .append("</D:resourcetype></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response>\n");
    }
}
