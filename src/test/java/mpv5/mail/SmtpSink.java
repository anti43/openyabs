package mpv5.mail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Minimal SMTP server for tests: accepts one session, records the envelope
 * recipients and the message data
 */
class SmtpSink implements AutoCloseable {

    final ServerSocket server;
    final List<String> recipients = new CopyOnWriteArrayList<String>();
    final StringBuffer data = new StringBuffer();
    private final Thread thread;

    SmtpSink() throws IOException {
        server = new ServerSocket(0);
        thread = new Thread(this::serve, "smtp-sink");
        thread.setDaemon(true);
        thread.start();
    }

    int port() {
        return server.getLocalPort();
    }

    private void serve() {
        try (Socket s = server.accept()) {
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            OutputStream out = s.getOutputStream();
            send(out, "220 localhost test");
            String line;
            while ((line = in.readLine()) != null) {
                String cmd = line.toUpperCase();
                if (cmd.startsWith("EHLO") || cmd.startsWith("HELO")) {
                    send(out, "250 localhost");
                } else if (cmd.startsWith("RCPT TO:")) {
                    recipients.add(line.substring(8).replaceAll("[<>\\s]", ""));
                    send(out, "250 ok");
                } else if (cmd.startsWith("DATA")) {
                    send(out, "354 go");
                    while ((line = in.readLine()) != null && !line.equals(".")) {
                        data.append(line).append('\n');
                    }
                    send(out, "250 queued");
                } else if (cmd.startsWith("QUIT")) {
                    send(out, "221 bye");
                    return;
                } else {
                    send(out, "250 ok");
                }
            }
        } catch (IOException e) {
            //test ends
        }
    }

    private static void send(OutputStream out, String reply) throws IOException {
        out.write((reply + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    void await() throws InterruptedException {
        thread.join(10000);
    }

    @Override
    public void close() throws IOException {
        server.close();
    }
}
