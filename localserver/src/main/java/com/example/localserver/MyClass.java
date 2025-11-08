package com.example.localserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MyClass{
    private static final int PORT = 8080;
    public static void main(String[] args) throws IOException {
        // create a simple HTTP server that listens on PORT
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT),0);

        // register a simple handler
        server.createContext("/Upload",new UploadHandler());
        server.createContext("/Download",new DownloadHandler());

        // start the server
        server.start();
    }

    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if(!"POST".equals(exchange.getRequestMethod())){
                sendResponse(exchange, 405, "仅支持POST请求");
                return;
            }

            // handle file upload logic here
            String query = exchange.getRequestURI().getQuery();

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
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }
}