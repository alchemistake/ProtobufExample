package main.java;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class SimpleRecordServer {
    private static final int BACKLOG = 0;
    private static final int PORT = 9000;
    private static final long FILE_DURATION_IN_MIN = 1;

    private static final String NAME = "name";
    private static final String ID = "id";

    private static final String POST = "POST";
    private static final byte[] ERROR_MESSAGE = "The request method should be POST with request body as JSON. e.g. {\"name\":\"your_name\",\"id\":0}".getBytes();
    private static final byte[] SUCCESS_MESSAGE = "Record has been added, successfully.".getBytes();

    static RecordMaster recordMaster;
    private HttpServer server;

    SimpleRecordServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), BACKLOG);
            server.createContext("/", new Handler());
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            System.out.println("Server started.");

            recordMaster = new RecordMaster(FILE_DURATION_IN_MIN);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    void stopServer() {
        server.stop(BACKLOG);
        ((ExecutorService) server.getExecutor()).shutdown();
        recordMaster.closeStore();
    }

    private static class Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("A request is received.");
            if (!POST.equalsIgnoreCase(exchange.getRequestMethod()))
                error(exchange, HttpURLConnection.HTTP_BAD_METHOD);
            else
                post(exchange);
        }

        private void error(HttpExchange exchange, int errorCode) throws IOException {
            System.out.println("Erroneous request, informing the user.");
            exchange.sendResponseHeaders(errorCode, ERROR_MESSAGE.length);
            OutputStream responseBody = exchange.getResponseBody();
            responseBody.write(ERROR_MESSAGE);
            exchange.close();
        }

        private void post(HttpExchange exchange) throws IOException {
            try {
                Headers requestHeaders = exchange.getRequestHeaders();
                int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));

                InputStream requestBody = exchange.getRequestBody();
                byte[] rawData = new byte[contentLength];
                requestBody.read(rawData);

                JSONObject requestJson = new JSONObject(new String(rawData));
                String name = requestJson.getString(NAME);
                Long id = requestJson.getLong(ID);

                System.out.println(String.format("Request is name=\"%s\",id=%d", name, id));
                recordMaster.addRecord(name, id);

                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, SUCCESS_MESSAGE.length);
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(SUCCESS_MESSAGE);
                exchange.close();
            } catch (Exception e) {
                e.printStackTrace();
                error(exchange, HttpURLConnection.HTTP_BAD_REQUEST);
            }
        }
    }
}
