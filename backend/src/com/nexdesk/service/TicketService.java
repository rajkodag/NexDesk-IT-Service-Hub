package com.nexdesk.service;

import com.nexdesk.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class TicketService {

    /*
     * ============================================
     * CREATE TICKET
     * ============================================
     *
     * Database table:
     *
     * NX_TICKETS
     *
     * Important:
     * The user column is CREATED_BY.
     * There is NO USER_ID column in NX_TICKETS.
     */

    public int create(
            int userId,
            String title,
            String description,
            String category,
            String priority
    ) throws SQLException {

        String sql =
                "INSERT INTO NX_TICKETS " +
                "(TICKET_ID, TITLE, DESCRIPTION, CATEGORY, " +
                "PRIORITY, STATUS, CREATED_BY, CREATED_AT, UPDATED_AT) " +
                "VALUES " +
                "(NX_TICKET_SEQ.NEXTVAL, ?, ?, ?, ?, " +
                "'OPEN', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";


        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(
                                sql,
                                new String[]{"TICKET_ID"}
                        )
        ) {

            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, category);
            ps.setString(4, priority);
            ps.setInt(5, userId);


            ps.executeUpdate();


            /*
             * Get generated TICKET_ID
             */

            try (ResultSet rs =
                         ps.getGeneratedKeys()) {

                if (rs.next()) {

                    return rs.getInt(1);
                }
            }


            /*
             * Fallback:
             * Get the latest ticket ID.
             */

            String fallbackSql =
                    "SELECT MAX(TICKET_ID) " +
                    "FROM NX_TICKETS " +
                    "WHERE CREATED_BY = ?";


            try (
                    PreparedStatement fallback =
                            connection.prepareStatement(
                                    fallbackSql
                            )
            ) {

                fallback.setInt(1, userId);


                try (
                        ResultSet rs =
                                fallback.executeQuery()
                ) {

                    if (rs.next()) {

                        return rs.getInt(1);
                    }
                }
            }
        }


        throw new SQLException(
                "Ticket created but ticket ID could not be retrieved."
        );
    }


    /*
     * ============================================
     * GET TICKETS OF ONE USER
     * ============================================
     */

    public List<Map<String, Object>> byUser(
            int userId
    ) throws SQLException {


        String sql =
                "SELECT " +
                "TICKET_ID, " +
                "TITLE, " +
                "DESCRIPTION, " +
                "CATEGORY, " +
                "PRIORITY, " +
                "STATUS, " +
                "CREATED_BY, " +
                "ASSIGNED_TO, " +
                "CREATED_AT, " +
                "UPDATED_AT " +
                "FROM NX_TICKETS " +
                "WHERE CREATED_BY = ? " +
                "ORDER BY CREATED_AT DESC";


        return executeTicketQuery(
                sql,
                userId
        );
    }


    /*
     * ============================================
     * GET ALL TICKETS
     * ============================================
     */

    public List<Map<String, Object>> all()
            throws SQLException {


        String sql =
                "SELECT " +
                "TICKET_ID, " +
                "TITLE, " +
                "DESCRIPTION, " +
                "CATEGORY, " +
                "PRIORITY, " +
                "STATUS, " +
                "CREATED_BY, " +
                "ASSIGNED_TO, " +
                "CREATED_AT, " +
                "UPDATED_AT " +
                "FROM NX_TICKETS " +
                "ORDER BY CREATED_AT DESC";


        List<Map<String, Object>> result =
                new ArrayList<>();


        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {


            while (rs.next()) {

                result.add(
                        mapTicket(rs)
                );
            }
        }


        return result;
    }


    /*
     * ============================================
     * UPDATE TICKET STATUS
     * ============================================
     */

    public void updateStatus(
            int ticketId,
            String status
    ) throws SQLException {


        String sql =
                "UPDATE NX_TICKETS " +
                "SET STATUS = ?, " +
                "UPDATED_AT = CURRENT_TIMESTAMP " +
                "WHERE TICKET_ID = ?";


        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {

            ps.setString(1, status);
            ps.setInt(2, ticketId);


            int updated =
                    ps.executeUpdate();


            if (updated == 0) {

                throw new SQLException(
                        "Ticket not found: " + ticketId
                );
            }
        }
    }


    /*
     * ============================================
     * ASSIGN TECHNICIAN
     * ============================================
     */

    public void assign(
            int ticketId,
            Integer technicianId
    ) throws SQLException {


        String sql =
                "UPDATE NX_TICKETS " +
                "SET ASSIGNED_TO = ?, " +
                "UPDATED_AT = CURRENT_TIMESTAMP " +
                "WHERE TICKET_ID = ?";


        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {


            if (technicianId == null) {

                ps.setNull(
                        1,
                        java.sql.Types.NUMERIC
                );

            } else {

                ps.setInt(
                        1,
                        technicianId
                );
            }


            ps.setInt(
                    2,
                    ticketId
            );


            int updated =
                    ps.executeUpdate();


            if (updated == 0) {

                throw new SQLException(
                        "Ticket not found: " + ticketId
                );
            }
        }
    }


    /*
     * ============================================
     * INTERNAL QUERY METHOD
     * ============================================
     */

    private List<Map<String, Object>> executeTicketQuery(
            String sql,
            int userId
    ) throws SQLException {


        List<Map<String, Object>> result =
                new ArrayList<>();


        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {


            ps.setInt(
                    1,
                    userId
            );


            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {


                while (rs.next()) {

                    result.add(
                            mapTicket(rs)
                    );
                }
            }
        }


        return result;
    }


    /*
     * ============================================
     * RESULTSET -> MAP
     * ============================================
     */

    private Map<String, Object> mapTicket(
            ResultSet rs
    ) throws SQLException {


        Map<String, Object> ticket =
                new LinkedHashMap<>();


        ticket.put(
                "ticketId",
                rs.getInt("TICKET_ID")
        );


        ticket.put(
                "title",
                rs.getString("TITLE")
        );


        ticket.put(
                "description",
                rs.getString("DESCRIPTION")
        );


        ticket.put(
                "category",
                rs.getString("CATEGORY")
        );


        ticket.put(
                "priority",
                rs.getString("PRIORITY")
        );


        ticket.put(
                "status",
                rs.getString("STATUS")
        );


        ticket.put(
                "createdBy",
                rs.getInt("CREATED_BY")
        );


        /*
         * ASSIGNED_TO can be NULL.
         */

        int assignedTo =
                rs.getInt("ASSIGNED_TO");


        if (rs.wasNull()) {

            ticket.put(
                    "assignedTo",
                    null
            );

        } else {

            ticket.put(
                    "assignedTo",
                    assignedTo
            );
        }


        /*
         * Oracle TIMESTAMP values.
         */

        if (rs.getTimestamp("CREATED_AT") != null) {

            ticket.put(
                    "createdAt",
                    rs.getTimestamp(
                            "CREATED_AT"
                    ).toString()
            );

        } else {

            ticket.put(
                    "createdAt",
                    null
            );
        }


        if (rs.getTimestamp("UPDATED_AT") != null) {

            ticket.put(
                    "updatedAt",
                    rs.getTimestamp(
                            "UPDATED_AT"
                    ).toString()
            );

        } else {

            ticket.put(
                    "updatedAt",
                    null
            );
        }


        return ticket;
    }
}