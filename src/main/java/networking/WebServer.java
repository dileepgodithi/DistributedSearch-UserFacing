package networking;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;

public class WebServer {
    private static final String STATUS_ENDPOINT = "/status";
    private static final String HOME_PAGE_ENDPOINT = "/";
    private final int port;
    private final Controller controller;
    private HttpServer server;

    public WebServer(int port ,Controller controller){
        this.port = port;
        this.controller = controller;
    }

    public void startServer(){
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
        HttpContext taskContext = server.createContext(controller.getEndPoint());
        HttpContext homePageContext = server.createContext(HOME_PAGE_ENDPOINT);

        statusContext.setHandler(this::handleStatusRequest);
        taskContext.setHandler(this::handleTaskRequest);
        homePageContext.setHandler(this::handleHomeUIAssetRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void handleHomeUIAssetRequest(HttpExchange httpExchange){

    }

    private void handleTaskRequest(HttpExchange httpExchange) throws IOException {
        if(!httpExchange.getRequestMethod().equalsIgnoreCase("post")){
            httpExchange.close();
            return;
        }

        byte[] requestBytes = httpExchange.getRequestBody().readAllBytes();
        byte[] responseBytes = controller.handleRequest(requestBytes);

        sendResponse(responseBytes, httpExchange);
    }
    private void handleStatusRequest(HttpExchange httpExchange) throws IOException {
        if(!httpExchange.getRequestMethod().equalsIgnoreCase("get")){
            httpExchange.close();
            return;
        }

        String response = "I am alive";
        sendResponse(response.getBytes(), httpExchange);
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
    }
}
