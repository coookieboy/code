package org.coookieboy;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {

    public static final int LISTEN_PORT = 9992;

    public static void main(String[] args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(LISTEN_PORT)) {
//            System.out.println("Server is listening on port " + LISTEN_PORT);

            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    System.out.println("accept success");
                    OutputStream output = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);

                    writer.println("HTTP/1.1 200 OK");
                    writer.println("Content-Type: text/html");
                    writer.println("\r\n");
                    writer.println("<h1>Hello, World!</h1>");
                }
            }
        }
    }
}
