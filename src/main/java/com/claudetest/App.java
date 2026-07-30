package com.claudetest;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.concurrent.Executors;

public class App {

    private static final Path STATIC_DIR = Paths.get("src/main/resources/static");
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", exchange -> {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }
            serveFile(exchange, requestPath);
        });

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("======================================");
        System.out.println("  🚀 服务器已启动！");
        System.out.println("  🌐 贪吃蛇: http://localhost:" + PORT + "/snake.html");
        System.out.println("  🍅 番茄钟: http://localhost:" + PORT + "/pomodoro.html");
        System.out.println("  📄 首页:   http://localhost:" + PORT + "/");
        System.out.println("======================================");
    }

    private static void serveFile(HttpExchange exchange, String requestPath) throws IOException {
        if (requestPath.contains("..")) {
            sendResponse(exchange, 403, "Forbidden");
            return;
        }

        Path filePath = STATIC_DIR.resolve(requestPath.startsWith("/") ? requestPath.substring(1) : requestPath);

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            String html = "<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">" +
                    "<title>claudetest</title><style>" +
                    "body{font-family:sans-serif;display:flex;flex-direction:column;align-items:center;justify-content:center;height:100vh;margin:0;background:#1a1a2e;color:#eee}" +
                    "h1{color:#e94560}a{display:block;margin:16px;padding:16px 32px;background:#16213e;color:#eee;text-decoration:none;border-radius:8px;font-size:20px;width:240px;text-align:center}" +
                    "a:hover{background:#0f3460;transform:scale(1.05)}</style></head><body>" +
                    "<h1>🎮 claudetest 项目</h1>" +
                    "<a href=\"/snake.html\">🐍 贪吃蛇游戏</a>" +
                    "<a href=\"/pomodoro.html\">🍅 番茄钟定时器</a>" +
                    "</body></html>";
            sendResponse(exchange, 200, html);
            return;
        }

        byte[] content = Files.readAllBytes(filePath);
        String mime = getMimeType(filePath.toString());
        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.sendResponseHeaders(200, content.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }

    private static void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String getMimeType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html; charset=UTF-8";
        if (fileName.endsWith(".css")) return "text/css; charset=UTF-8";
        if (fileName.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        if (fileName.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }
}