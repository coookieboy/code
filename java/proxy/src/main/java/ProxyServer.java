import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Base64;

public class ProxyServer {

    public static int LISTEN_PORT = 0;
    public static int TRANSFER_PORT = 0;

    public static String USER_NAME = "";

    public static String PASS_WORD = "";

    public static String HOST = "";

    public static void main(String[] args) {

        if (args.length != 5) {
            return;
        }
        HOST = args[0];
        LISTEN_PORT = Integer.parseInt(args[1]);
        TRANSFER_PORT = Integer.parseInt(args[2]);
        USER_NAME = args[3];
        PASS_WORD = args[4];

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(LISTEN_PORT);
            serverSocket.setSoTimeout(1000 * 60);
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        while (true) {
            try (Socket socket = serverSocket.accept();
                 InputStream is = socket.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is));
                 OutputStream os = socket.getOutputStream()) {

                String line;
                String host;
                int port;

                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    if (line.isEmpty()) {
                        break;
                    }
                    sb.append(line);
                    sb.append("\r\n");
                }

                // auth
                String auth = USER_NAME + ":" + PASS_WORD;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

                sb.append("Proxy-Authorization: Basic ").append(encodedAuth).append("\r\n");
                sb.append("\r\n");

                host = HOST;
                port = TRANSFER_PORT;

                Socket proxySocket = null;
                if (host != null && !host.isEmpty()) {
                    proxySocket = new Socket(host, port);
                    proxySocket.setSoTimeout(1000 * 60);
                    OutputStream proxyOs = proxySocket.getOutputStream();
                    InputStream proxyIs = proxySocket.getInputStream();
                    proxyOs.write(encrypt(sb.toString()).getBytes());
                    proxyOs.flush();
                    new ProxyHandleThread(is, proxyOs).start();
                    new ProxyHandleThread(proxyIs, os).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String encrypt(String string) {
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] ^= 42;
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
