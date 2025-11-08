package com.example.localserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class MyClass{
    private static final Logger logger = Logger.getLogger(MyClass.class.getName());
    private static final int PORT = 8080;
    public static void main(String[] args) throws IOException {
        // create a simple HTTP server that listens on PORT
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT),0);

        // register a simple handler
        server.createContext("/Upload",new UploadHandler());
        server.createContext("/Download",new DownloadHandler());

        // start the server
        server.setExecutor(null);
        server.start();
    }

    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if(!"POST".equals(exchange.getRequestMethod())){
                sendResponse(exchange, 405, "仅支持POST请求");
                return;
            }

            // 读取 POST 请求体
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String decoded = URLDecoder.decode(body, StandardCharsets.UTF_8);
            System.out.println("📩 解码后的请求体:\n" + decoded);

            // 提取 data 值
            String value = decoded.substring(decoded.indexOf('=') + 1);

            // 保存到文件 history.txt（追加模式）
            File file = new File("history.txt");
            try (FileWriter fw = new FileWriter(file, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(value);
                bw.newLine();
            }

            //return a simple response
            sendResponse(exchange, 200, "Upload Successful!");
        }
    }

    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if(!"GET".equals(exchange.getRequestMethod())){
                sendResponse(exchange, 405, "仅支持GET请求");
                return;
            }

            // handle file download logic here
            String query = exchange.getRequestURI().getQuery();

            //return a simple response
            sendResponse(exchange, 200, "Download Successful!");
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}