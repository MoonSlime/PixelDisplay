package connect;

import java.sql.*;
import java.util.Arrays;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

class ConnectionPool {
    static Connection getConnection() {
        Connection connection;
        try {
            Context context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc/pixeldisplay");

            connection = dataSource.getConnection();
        } catch (Exception e) {
            connection = null;

            System.out.println(e.getClass().getSimpleName());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return connection;
    }
}