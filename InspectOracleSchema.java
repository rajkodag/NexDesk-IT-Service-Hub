import java.sql.*;

public class InspectOracleSchema {
    public static void main(String[] args) {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:oracle:thin:@localhost:1521/XEPDB1");
        String user = System.getenv().getOrDefault("DB_USER", "ITHELPDESK");
        String pass = System.getenv().getOrDefault("DB_PASSWORD", "ithelpdesk123");

        System.out.println("Connecting to: " + url + " as " + user);
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            DatabaseMetaData meta = conn.getMetaData();
            String[] tables = {"NX_USERS", "NX_TECHNICIANS", "NX_TICKETS", "NX_TICKET_COMMENTS", "NX_AUDIT_LOGS"};
            for (String table : tables) {
                System.out.println("\n=== TABLE: " + table + " ===");
                try (ResultSet rs = meta.getColumns(null, user.toUpperCase(), table, null)) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        String col = rs.getString("COLUMN_NAME");
                        String type = rs.getString("TYPE_NAME");
                        int size = rs.getInt("COLUMN_SIZE");
                        String nullable = rs.getString("IS_NULLABLE");
                        System.out.println("  " + col + " (" + type + (size > 0 ? "(" + size + ")" : "") + ") Nullable: " + nullable);
                    }
                    if (!found) {
                        System.out.println("  [Table not found in schema " + user + "]");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
