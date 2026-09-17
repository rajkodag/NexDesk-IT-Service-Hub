package com.nexdesk.web;

import com.nexdesk.db.DatabaseManager;
import com.nexdesk.model.User;
import com.nexdesk.service.TechnicianService;
import com.nexdesk.service.TicketService;
import com.nexdesk.service.UserService;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.net.URLDecoder;

import java.nio.charset.StandardCharsets;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;


public class ApiServer {

    private final HttpServer server;

    private final UserService users = new UserService();
    private final TicketService tickets = new TicketService();
    private final TechnicianService techs = new TechnicianService();


    public ApiServer(int port) throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(port),
                0
        );

        server.createContext(
                "/",
                this::handle
        );

        server.setExecutor(
                Executors.newCachedThreadPool()
        );
    }


    public void start() {

        server.start();

        System.out.println(
                "NexDesk server started."
        );

        System.out.println(
                "Open: http://localhost:8080"
        );
    }


    /*
     * MAIN HTTP HANDLER
     */

    private void handle(
            HttpExchange e
    ) throws IOException {

        cors(e);

        /*
         * Handle browser preflight request
         */

        if ("OPTIONS".equalsIgnoreCase(
                e.getRequestMethod())) {

            e.sendResponseHeaders(
                    204,
                    -1
            );

            return;
        }


        try {

            String path =
                    e.getRequestURI()
                            .getPath();


            /*
             * FRONTEND
             */

            if ("/".equals(path)
                    || "/index.html".equals(path)) {

                file(
                        e,
                        "index.html",
                        "text/html"
                );

                return;
            }


            /*
             * CSS
             */

            if (path.startsWith("/css/")) {

                file(
                        e,
                        path.substring(1),
                        "text/css"
                );

                return;
            }


            /*
             * JAVASCRIPT
             */

            if (path.startsWith("/js/")) {

                file(
                        e,
                        path.substring(1),
                        "application/javascript"
                );

                return;
            }


            /*
             * API
             */

            if (path.startsWith("/api/")) {

                api(e, path);

                return;
            }


            /*
             * PAGE NOT FOUND
             */

            send(
                    e,
                    404,
                    "{\"error\":\"Not found\"}",
                    "application/json"
            );


        } catch (Exception ex) {

            ex.printStackTrace();

            String message =
                    ex.getMessage();

            if (message == null
                    || message.isBlank()) {

                message = "Server error";
            }

            send(
                    e,
                    500,
                    json(
                            "error",
                            message
                    ),
                    "application/json"
            );
        }
    }


    /*
     * API HANDLER
     *
     * IMPORTANT:
     * SQLException is declared here because
     * UserService and TicketService access Oracle.
     */

    private void api(
            HttpExchange e,
            String path
    ) throws IOException, SQLException {


        Map<String, String> data =
                parse(read(e));


        /*
         * =====================================
         * HEALTH CHECK
         * =====================================
         */

        if (path.equals(
                "/api/health")) {

            try {

                DatabaseManager.testConnection();

                send(
                        e,
                        200,
                        "{\"ok\":true,\"database\":true}",
                        "application/json"
                );

            } catch (SQLException ex) {

                send(
                        e,
                        500,
                        json(
                                "error",
                                ex.getMessage()
                        ),
                        "application/json"
                );
            }

            return;
        }


        /*
         * =====================================
         * LOGIN
         * =====================================
         */

        if (path.equals(
                "/api/login")
                && "POST".equalsIgnoreCase(
                        e.getRequestMethod())) {


            String email =
                    data.get("email");

            String password =
                    data.get("password");


            if (email == null
                    || password == null
                    || email.isBlank()
                    || password.isBlank()) {

                send(
                        e,
                        400,
                        "{\"error\":\"Email and password are required\"}",
                        "application/json"
                );

                return;
            }


            User user =
                    users.login(
                            email,
                            password
                    );


            if (user == null) {

                send(
                        e,
                        401,
                        "{\"error\":\"Invalid email or password\"}",
                        "application/json"
                );

                return;
            }


            send(
                    e,
                    200,
                    userJson(user),
                    "application/json"
            );

            return;
        }


        /*
         * =====================================
         * SIGNUP
         * =====================================
         */

        if (path.equals(
                "/api/signup")
                && "POST".equalsIgnoreCase(
                        e.getRequestMethod())) {


            String name =
                    data.get("name");

            String email =
                    data.get("email");

            String password =
                    data.get("password");


            if (name == null
                    || email == null
                    || password == null
                    || name.isBlank()
                    || email.isBlank()
                    || password.isBlank()) {

                send(
                        e,
                        400,
                        "{\"error\":\"Name, email and password are required\"}",
                        "application/json"
                );

                return;
            }


            User user =
                    users.signup(
                            name,
                            email,
                            password
                    );


            if (user == null) {

                send(
                        e,
                        400,
                        "{\"error\":\"Account creation failed\"}",
                        "application/json"
                );

                return;
            }


            send(
                    e,
                    201,
                    userJson(user),
                    "application/json"
            );

            return;
        }


        /*
         * =====================================
         * CREATE TICKET
         * =====================================
         */

        if (path.equals(
                "/api/tickets")
                && "POST".equalsIgnoreCase(
                        e.getRequestMethod())) {


            String userIdValue =
                    data.get("userId");

            String title =
                    data.get("title");

            String description =
                    data.get("description");

            String category =
                    data.getOrDefault(
                            "category",
                            "General"
                    );

            String priority =
                    data.getOrDefault(
                            "priority",
                            "MEDIUM"
                    );


            if (userIdValue == null
                    || title == null
                    || description == null
                    || title.isBlank()
                    || description.isBlank()) {

                send(
                        e,
                        400,
                        "{\"error\":\"User ID, title and description are required\"}",
                        "application/json"
                );

                return;
            }


            int userId =
                    Integer.parseInt(
                            userIdValue
                    );


            /*
             * TicketService now uses
             * CREATED_BY instead of USER_ID.
             */

            int ticketId =
                    tickets.create(
                            userId,
                            title,
                            description,
                            category,
                            priority
                    );


            send(
                    e,
                    201,
                    "{\"ticketId\":" +
                            ticketId +
                            "}",
                    "application/json"
            );

            return;
        }


        /*
         * =====================================
         * GET USER TICKETS
         * =====================================
         */

        if (path.equals(
                "/api/tickets")
                && "GET".equalsIgnoreCase(
                        e.getRequestMethod())) {


            int userId =
                    Integer.parseInt(
                            query(
                                    e,
                                    "userId",
                                    "0"
                            )
                    );


            if (userId <= 0) {

                send(
                        e,
                        400,
                        "{\"error\":\"Valid userId is required\"}",
                        "application/json"
                );

                return;
            }


            send(
                    e,
                    200,
                    toJson(
                            tickets.byUser(
                                    userId
                            )
                    ),
                    "application/json"
            );

            return;
        }


        /*
         * =====================================
         * ADMIN - GET ALL TICKETS
         * =====================================
         */

        if (path.equals(
                "/api/admin/tickets")
                && "GET".equalsIgnoreCase(
                        e.getRequestMethod())) {


            send(
                    e,
                    200,
                    toJson(
                            tickets.all()
                    ),
                    "application/json"
            );

            return;
        }


        /*
         * =====================================
         * ADMIN - UPDATE STATUS
         * =====================================
         */

        if (path.equals(
                "/api/admin/tickets/status")
                && "PUT".equalsIgnoreCase(
                        e.getRequestMethod())) {


            String ticketIdValue =
                    data.get("ticketId");

            String status =
                    data.get("status");


            if (ticketIdValue == null
                    || status == null
                    || status.isBlank()) {

                send(
                        e,
                        400,
                        "{\"error\":\"Ticket ID and status are required\"}",
                        "application/json"
                );

                return;
            }


            tickets.updateStatus(
                    Integer.parseInt(
                            ticketIdValue
                    ),
                    status
            );


            send(
                    e,
                    200,
                    "{\"ok\":true}",
                    "application/json"
            );

            return;
        }


        /*
         * =====================================
         * ADMIN - ASSIGN TECHNICIAN
         * =====================================
         */

        if (path.equals(
                "/api/admin/tickets/assign")
                && "PUT".equalsIgnoreCase(
                        e.getRequestMethod())) {


            String ticketIdValue =
                    data.get("ticketId");

            String technicianIdValue =
                    data.get("technicianId");


            if (ticketIdValue == null) {

                send(
                        e,
                        400,
                        "{\"error\":\"Ticket ID is required\"}",
                        "application/json"
                );

                return;
            }


            Integer technicianId = null;


            if (technicianIdValue != null
                    && !technicianIdValue.isBlank()) {

                technicianId =
                        Integer.valueOf(
                                technicianIdValue
                        );
            }


            tickets.assign(
                    Integer.parseInt(
                            ticketIdValue
                    ),
                    technicianId
            );


            send(
                    e,
                    200,
                    "{\"ok\":true}",
                    "application/json"
            );

            return;
        }


        /*
         * =====================================
         * GET TECHNICIANS
         * =====================================
         */

        if (path.equals(
                "/api/technicians")
                && "GET".equalsIgnoreCase(
                        e.getRequestMethod())) {


            send(
                    e,
                    200,
                    toJson(
                            techs.all()
                    ),
                    "application/json"
            );

            return;
        }


        /*
         * UNKNOWN API
         */

        send(
                e,
                404,
                "{\"error\":\"API endpoint not found\"}",
                "application/json"
        );
    }


    /*
     * =====================================
     * SERVE FRONTEND FILE
     * =====================================
     */

    private void file(
            HttpExchange e,
            String name,
            String type
    ) throws IOException {


        /*
         * Project structure:
         *
         * NexDesk-IT-Service-Hub
         * ├── backend
         * │   └── ...
         * └── frontend
         *
         * Server runs from backend.
         *
         * Therefore frontend is ../frontend
         */

        File f =
                new File(
                        "..",
                        "frontend/" + name
                );


        if (!f.exists()) {

            send(
                    e,
                    404,
                    "File not found: " + name,
                    "text/plain"
            );

            return;
        }


        byte[] bytes =
                java.nio.file.Files
                        .readAllBytes(
                                f.toPath()
                        );


        e.getResponseHeaders()
                .set(
                        "Content-Type",
                        type +
                                "; charset=UTF-8"
                );


        e.sendResponseHeaders(
                200,
                bytes.length
        );


        try (
                OutputStream out =
                        e.getResponseBody()
        ) {

            out.write(bytes);
        }
    }


    /*
     * =====================================
     * READ REQUEST BODY
     * =====================================
     */

    private static String read(
            HttpExchange e
    ) throws IOException {

        return new String(
                e.getRequestBody()
                        .readAllBytes(),
                StandardCharsets.UTF_8
        );
    }


    /*
     * =====================================
     * QUERY PARAMETER
     * =====================================
     */

    private static String query(
            HttpExchange e,
            String key,
            String defaultValue
    ) {


        String q =
                e.getRequestURI()
                        .getRawQuery();


        if (q == null) {
            return defaultValue;
        }


        for (String item :
                q.split("&")) {


            String[] parts =
                    item.split(
                            "=",
                            2
                    );


            if (parts.length == 2
                    && parts[0].equals(key)) {

                return URLDecoder.decode(
                        parts[1],
                        StandardCharsets.UTF_8
                );
            }
        }


        return defaultValue;
    }


    /*
     * =====================================
     * SIMPLE JSON PARSER
     * =====================================
     */

    private static Map<String, String> parse(
            String s
    ) {


        Map<String, String> map =
                new HashMap<>();


        if (s == null) {
            return map;
        }


        s = s.trim();


        if (s.startsWith("{")
                && s.endsWith("}")) {

            s =
                    s.substring(
                            1,
                            s.length() - 1
                    );
        }


        boolean insideQuotes = false;


        StringBuilder buffer =
                new StringBuilder();


        List<String> parts =
                new ArrayList<>();


        for (char c :
                s.toCharArray()) {


            if (c == '"') {

                insideQuotes =
                        !insideQuotes;
            }


            if (c == ','
                    && !insideQuotes) {

                parts.add(
                        buffer.toString()
                );

                buffer.setLength(0);

            } else {

                buffer.append(c);
            }
        }


        parts.add(
                buffer.toString()
        );


        for (String item :
                parts) {


            String[] pair =
                    item.split(
                            ":",
                            2
                    );


            if (pair.length == 2) {

                map.put(
                        clean(pair[0]),
                        clean(pair[1])
                );
            }
        }


        return map;
    }


    /*
     * =====================================
     * CLEAN JSON VALUE
     * =====================================
     */

    private static String clean(
            String s
    ) {


        s = s.trim();


        if (s.startsWith("\"")
                && s.endsWith("\"")) {

            s =
                    s.substring(
                            1,
                            s.length() - 1
                    );
        }


        return s
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", " ")
                .replace("\\r", " ");
    }


    /*
     * =====================================
     * USER -> JSON
     * =====================================
     */

    private static String userJson(
            User user
    ) {


        return "{"
                + "\"userId\":"
                + user.getUserId()

                + ",\"name\":\""
                + esc(
                        user.getFullName()
                )
                + "\""

                + ",\"email\":\""
                + esc(
                        user.getEmail()
                )
                + "\""

                + ",\"role\":\""
                + esc(
                        user.getRole()
                )
                + "\""

                + "}";
    }


    /*
     * =====================================
     * GENERIC JSON
     * =====================================
     */

    private static String json(
            String key,
            String value
    ) {


        return "{"
                + "\""
                + esc(key)
                + "\":\""
                + esc(value)
                + "\""
                + "}";
    }


    /*
     * =====================================
     * LIST -> JSON
     * =====================================
     */

    private static String toJson(
            List<Map<String, Object>> rows
    ) {


        StringBuilder json =
                new StringBuilder("[");


        boolean firstRow = true;


        for (Map<String, Object> row :
                rows) {


            if (!firstRow) {

                json.append(',');
            }


            firstRow = false;


            json.append('{');


            boolean firstColumn = true;


            for (
                    Map.Entry<String, Object> entry
                    : row.entrySet()
            ) {


                if (!firstColumn) {

                    json.append(',');
                }


                firstColumn = false;


                json.append('"')
                        .append(
                                esc(
                                        entry.getKey()
                                )
                        )
                        .append("\":");


                Object value =
                        entry.getValue();


                if (value == null) {

                    json.append("null");

                } else if (
                        value instanceof Number
                                || value instanceof Boolean
                ) {

                    json.append(value);

                } else {

                    json.append('"')
                            .append(
                                    esc(
                                            String.valueOf(
                                                    value
                                            )
                                    )
                            )
                            .append('"');
                }
            }


            json.append('}');
        }


        json.append(']');


        return json.toString();
    }


    /*
     * =====================================
     * ESCAPE JSON STRING
     * =====================================
     */

    private static String esc(
            String value
    ) {


        if (value == null) {

            return "";
        }


        return value
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                )
                .replace(
                        "\r",
                        " "
                )
                .replace(
                        "\n",
                        " "
                );
    }


    /*
     * =====================================
     * CORS
     * =====================================
     */

    private static void cors(
            HttpExchange e
    ) {


        e.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Origin",
                        "*"
                );


        e.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Headers",
                        "Content-Type"
                );


        e.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Methods",
                        "GET,POST,PUT,OPTIONS"
                );
    }


    /*
     * =====================================
     * SEND HTTP RESPONSE
     * =====================================
     */

    private static void send(
            HttpExchange e,
            int statusCode,
            String body,
            String contentType
    ) throws IOException {


        byte[] bytes =
                body.getBytes(
                        StandardCharsets.UTF_8
                );


        e.getResponseHeaders()
                .set(
                        "Content-Type",
                        contentType +
                                "; charset=UTF-8"
                );


        e.sendResponseHeaders(
                statusCode,
                bytes.length
        );


        try (
                OutputStream out =
                        e.getResponseBody()
        ) {

            out.write(bytes);
        }
    }
}