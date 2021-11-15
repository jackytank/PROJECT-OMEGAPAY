/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;

import java.sql.*;

/**
 *
 * @author balis
 */
public class JDBCHelper {

    public static String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=BankAppDB";
    public static String USERNAME = "sa";
    public static String PASSWORD = "1221";

    public static PreparedStatement preparedStatement(String SQL, Object... args) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            PreparedStatement stmt = null;
            if (SQL.trim().startsWith("{")) {
                stmt = conn.prepareCall(SQL);
            } else {
                stmt = conn.prepareStatement(SQL);
            }
            for (int i = 0; i < args.length; i++) {
                stmt.setObject(i + 1, args[i]);
            }
            return stmt;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ResultSet executeQuery(String SQL, Object... args) {
        try {
            PreparedStatement stmt = preparedStatement(SQL, args);
            try {
                return stmt.executeQuery();
            } finally {

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void executeUpdate(String SQL, Object... args) {
        try {
            PreparedStatement stmt = preparedStatement(SQL, args);
            try {
                stmt.executeUpdate();
            } finally {
                stmt.getConnection().close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
