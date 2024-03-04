package org.coookieboy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

public class NewProxyServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream clientInput = clientSocket.getInputStream();
                OutputStream clientOutput = clientSocket.getOutputStream();

                // 读取客户端发送的HTTP请求的第一行，获取目标地址和端口
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput));
                String[] requestLine = reader.readLine().split(" ");
                URL url = new URL(requestLine[1]);
                String host = url.getHost();
                int port = url.getPort() == -1 ? 80 : url.getPort();

                // 创建到目标服务器的连接
                Socket serverSocket = new Socket(host, port);
                InputStream serverInput = serverSocket.getInputStream();
                OutputStream serverOutput = serverSocket.getOutputStream();

                // 将客户端的请求转发到服务器
                new Thread(new Transfer(clientInput, serverOutput)).start();
                // 将服务器的响应转发到客户端
                new Thread(new Transfer(serverInput, clientOutput)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Transfer implements Runnable {
        private final InputStream input;
        private final OutputStream output;

        public Transfer(InputStream input, OutputStream output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[4096];
            int read;
            try {
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    output.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
