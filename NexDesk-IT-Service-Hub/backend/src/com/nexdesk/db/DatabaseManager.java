package com.nexdesk.db;
import java.sql.*;
public final class DatabaseManager {
 private static final String URL=System.getenv().getOrDefault("DB_URL","jdbc:oracle:thin:@localhost:1521/XEPDB1");
 private static final String USER=System.getenv().getOrDefault("DB_USER","");
 private static final String PASSWORD=System.getenv().getOrDefault("DB_PASSWORD","");
 private DatabaseManager(){}
 public static Connection getConnection() throws SQLException{return DriverManager.getConnection(URL,USER,PASSWORD);}
 public static String getUrl(){return URL;}
 public static String getUser(){return USER;}
 public static void testConnection() throws SQLException{try(Connection c=getConnection()){System.out.println("Oracle connection successful.");System.out.println("URL  : "+URL);System.out.println("USER : "+USER);}}
}
