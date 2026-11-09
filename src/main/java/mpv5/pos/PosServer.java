/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.pos;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import mpv5.logging.Log;

/**
 * A small web server in YaBS for the cash register: the touch page and its
 * API (sign in, products, sale). Requests are handled one after the other.
 */
public final class PosServer {

    private final PosService service;
    private final int port;
    private HttpServer server;
    private String shopName = "YaBS";

    public PosServer(PosService service, int port) {
        this.service = service;
        this.port = port;
    }

    /**
     * @param name shown on the page
     */
    public void setShopName(String name) {
        this.shopName = name == null || name.isEmpty() ? "YaBS" : name;
    }

    /**
     * @throws IOException if the port is taken
     */
    public synchronized void start() throws IOException {
        if (server != null) {
            return;
        }
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::page);
        server.createContext("/api/info", e -> json(e, 200, "{\"shop\":" + q(shopName) + "}"));
        server.createContext("/api/login", this::login);
        server.createContext("/api/logout", e -> {
            service.logout(e.getRequestHeaders().getFirst("X-Token"));
            json(e, 200, "{}");
        });
        server.createContext("/api/products", this::products);
        server.createContext("/api/sale", this::sale);
        server.setExecutor(Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "pos-server");
            t.setDaemon(true);
            return t;
        }));
        server.start();
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public synchronized boolean isRunning() {
        return server != null;
    }

    public int port() {
        return port;
    }

    /**
     * @return the addresses to open on a device in the network
     */
    public List<String> urls() {
        List<String> l = new ArrayList<String>();
        try {
            for (NetworkInterface n : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!n.isUp() || n.isLoopback()) {
                    continue;
                }
                for (InetAddress a : Collections.list(n.getInetAddresses())) {
                    if (a instanceof Inet4Address) {
                        l.add("http://" + a.getHostAddress() + ":" + port + "/");
                    }
                }
            }
        } catch (Exception e) {
            Log.Debug(PosServer.class, e);
        }
        l.add("http://localhost:" + port + "/");
        return l;
    }

    // ------------------------------------------------------------------ handlers
    private void page(HttpExchange e) throws IOException {
        if (!"/".equals(e.getRequestURI().getPath()) && !"/index.html".equals(e.getRequestURI().getPath())) {
            send(e, 404, "text/plain", "not found".getBytes(StandardCharsets.UTF_8));
            return;
        }
        try (InputStream in = PosServer.class.getResourceAsStream("/mpv5/pos/index.html")) {
            send(e, 200, "text/html; charset=utf-8", read(in));
        }
    }

    private void login(HttpExchange e) throws IOException {
        Map<String, String> f = form(e);
        String token = service.login(f.get("user"), f.get("password"));
        if (token == null) {
            try {
                //slows down guessing
                Thread.sleep(800);
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
            json(e, 401, "{\"error\":\"login\"}");
        } else {
            json(e, 200, "{\"token\":" + q(token) + ",\"name\":" + q(service.user(token).__getFullname()) + "}");
        }
    }

    private void products(HttpExchange e) throws IOException {
        if (service.user(e.getRequestHeaders().getFirst("X-Token")) == null) {
            json(e, 401, "{\"error\":\"login\"}");
            return;
        }
        StringBuilder b = new StringBuilder("[");
        for (PosService.Article a : service.articles()) {
            if (b.length() > 1) {
                b.append(',');
            }
            b.append("{\"id\":").append(a.id)
                    .append(",\"name\":").append(q(a.name))
                    .append(",\"number\":").append(q(a.number))
                    .append(",\"group\":").append(q(a.group))
                    .append(",\"gross\":").append(a.gross.toPlainString())
                    .append(",\"tax\":").append(a.taxPercent.stripTrailingZeros().toPlainString())
                    .append(",\"measure\":").append(q(a.measure)).append('}');
        }
        json(e, 200, b.append(']').toString());
    }

    private void sale(HttpExchange e) throws IOException {
        String token = e.getRequestHeaders().getFirst("X-Token");
        if (service.user(token) == null) {
            json(e, 401, "{\"error\":\"login\"}");
            return;
        }
        Map<String, String> f = form(e);
        Map<Integer, PosService.Article> byId = new HashMap<Integer, PosService.Article>();
        for (PosService.Article a : service.articles()) {
            byId.put(a.id, a);
        }
        List<PosService.Line> lines = new ArrayList<PosService.Line>();
        for (String part : f.getOrDefault("items", "").split(",")) {
            String[] p = part.split(":");
            if (p.length == 2) {
                try {
                    PosService.Article a = byId.get(Integer.valueOf(p[0].trim()));
                    BigDecimal qty = new BigDecimal(p[1].trim());
                    if (a != null && qty.signum() > 0) {
                        lines.add(new PosService.Line(a, qty));
                    }
                } catch (NumberFormatException ignore) {
                    //skip a broken line
                }
            }
        }
        try {
            PosService.Receipt r = service.sell(token, lines, PosService.amount(f.get("given")));
            json(e, 200, "{\"number\":" + q(r.number) + ",\"net\":" + r.net.toPlainString() + ",\"tax\":" + r.tax.toPlainString()
                    + ",\"gross\":" + r.gross.toPlainString() + ",\"change\":" + r.change.toPlainString() + "}");
        } catch (IllegalArgumentException ex) {
            json(e, 400, "{\"error\":" + q(ex.getMessage()) + "}");
        } catch (Exception ex) {
            Log.Debug(PosServer.class, ex);
            json(e, 500, "{\"error\":" + q(String.valueOf(ex.getMessage())) + "}");
        }
    }

    // ------------------------------------------------------------------ helpers
    static Map<String, String> form(HttpExchange e) throws IOException {
        Map<String, String> m = new HashMap<String, String>();
        String body = new String(read(e.getRequestBody()), StandardCharsets.UTF_8);
        for (String pair : body.split("&")) {
            int i = pair.indexOf('=');
            if (i > 0) {
                m.put(URLDecoder.decode(pair.substring(0, i), StandardCharsets.UTF_8),
                        URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8));
            }
        }
        return m;
    }

    static String q(String s) {
        if (s == null) {
            return "\"\"";
        }
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':
                    b.append("\\\"");
                    break;
                case '\\':
                    b.append("\\\\");
                    break;
                case '\n':
                    b.append("\\n");
                    break;
                case '\r':
                    break;
                case '<':
                    b.append("\\u003c");
                    break;
                default:
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
            }
        }
        return b.append('"').toString();
    }

    private static void json(HttpExchange e, int status, String body) throws IOException {
        send(e, status, "application/json; charset=utf-8", body.getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange e, int status, String type, byte[] body) throws IOException {
        e.getResponseHeaders().set("Content-Type", type);
        e.getResponseHeaders().set("Cache-Control", "no-store");
        e.sendResponseHeaders(status, body.length);
        try (OutputStream o = e.getResponseBody()) {
            o.write(body);
        }
    }

    private static byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) {
            b.write(buf, 0, n);
        }
        return b.toByteArray();
    }
}
