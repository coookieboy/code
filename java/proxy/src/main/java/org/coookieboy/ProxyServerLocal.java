package org.coookieboy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class ProxyServerLocal {

    public static final int LISTEN_PORT = 9991;
    public static final int TRANSFER_PORT = 9999;

    public static final String SPACE = " ";

    public static final String COLON = ":";

    public static void main(String[] args) {

        ServerSocket ss = null;
        try {
            ss = new ServerSocket(LISTEN_PORT);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        while(true) try {
            Socket socket = ss.accept();
            socket.setSoTimeout(1000 * 60); // 设置代理服务器与客户端的连接未活动超时时间

            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            OutputStream os = socket.getOutputStream();

            String line;
            String address = "";
            String host;
            int port;
            String type = null;
            int temp = 1;

            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                // 获取请求行中请求方法，下面会需要这个来判断是http还是https
                if (temp == 1) {
                    type = line.split(SPACE)[0];
                    if (type == null) continue;
                }
                temp++;
                String[] s1 = line.split(COLON + SPACE);
                if (line.isEmpty()) {
                    break;
                }
                if ("host".equalsIgnoreCase(s1[0])) {
                    address = s1[1];
                }
                sb.append(line).append("\r\n");
            }
            sb.append("\r\n"); // 不加上这行http请求则无法进行。这其实就是告诉服务端一个请求结束了

            // 解析地址和端口
            host = address.split(COLON)[0];
            port = address.split(COLON).length > 1 ? Integer.parseInt(address.split(COLON)[1]) : 80;

            Socket proxySocket = null;
            port = TRANSFER_PORT;
            if (host != null && !host.isEmpty()) {
                proxySocket = new Socket(host, port);
                proxySocket.setSoTimeout(1000 * 60); // 设置代理服务器与服务器端的连接未活动超时时间
                OutputStream proxyOs = proxySocket.getOutputStream();
                InputStream proxyIs = proxySocket.getInputStream();
                if (type.equalsIgnoreCase("connect")) {
                    // https请求的话，告诉客户端连接已经建立（下面代码建立）
                    os.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                    os.flush();
                } else {
                    // http请求则直接转发
                    proxyOs.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                    proxyOs.flush();
                }
                new ProxyHandleThread(is, proxyOs).start();//监听客户端传来消息并转发给服务器
                new ProxyHandleThread(proxyIs, os).start(); //监听服务器传来消息并转发给客户端
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String xorEncryptDecrypt(String input, int key) {
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] ^ key);
        }
        return new String(chars);
    }
}


class ProxyHandleThread extends Thread {

    private InputStream input;
    private OutputStream output;
    public ProxyHandleThread(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void run() {
        try {
            while (true) {
                BufferedInputStream bis = new BufferedInputStream(input);
                byte[] buffer = new byte[16 * 1024];
                int length;
                while ((length = bis.read(buffer)) != -1) {
                    // 这里最好是字节转发，不要用上面的InputStreamReader，因为https传递的都是密文，那样会乱码，消息传到服务器端也会出错。
                    for (int i = 0; i < length; i++) {
                        buffer[i] ^= 42;
                    }
                    output.write(buffer, 0, length);
                }
                output.flush();
            }
        } catch (SocketTimeoutException e) {
            try {
                input.close();
                output.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }catch (IOException e) {
            System.out.println(e);
        }finally {
            try {
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
