package org.coookieboy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(9990);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("accept access");

            InputStream inputStream = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while (!(line = reader.readLine()).isEmpty()) {
                System.out.println(line);
            }

            OutputStream outputStream = clientSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);

            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/html");
            writer.println("Connection: close");
            writer.println("");
            writer.println("<html><body><h1>Hello, world!</h1></body></html>");

            clientSocket.close();
        }
    }
}
