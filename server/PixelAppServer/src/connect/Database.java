package connect;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.*;
import org.apache.commons.dbutils.handlers.*;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

public class Database {
    private Connection connection;
    private QueryRunner runner;
    private static Database instance;

    private Database() throws SQLException {
        connection = ConnectionPool.getConnection();
        /*
        if (connection.isClosed()) {
            connection = ConnectionPool.getConnection();
        }
        */
        runner = new QueryRunner();
    }

    public static Database getInstance() throws SQLException {
        if (instance == null) {
            instance = new Database();
        }

        return instance;
    }

    public List<Map<String, Object>> select(String query, Object... params) throws SQLException {
        ColumnAliasRowProcessor aliasRowProcessor = new ColumnAliasRowProcessor();
        return runner.query(connection, query, new MapListHandler(aliasRowProcessor), params);
    }

    public boolean exist(String query, Object... params) throws SQLException {
        return (count(query, params) > 0);
    }

    public int count(String query, Object... params) throws SQLException {
        Object oCount = runner.query(connection, query, new ScalarHandler(1), params);

        if (oCount == null) {
            return -1;
        } else {
            if ( oCount instanceof Long ) {
                return ((Long)oCount).intValue();
            } else {
                return (Integer)oCount;
            }
        }
    }

    public Object value(String query, String columnName, Object... params) throws SQLException {
        return runner.query(connection, query, new ScalarHandler(columnName), params);
    }

    public int update(String query, Object... params) throws SQLException {
        return runner.update(connection, query, params);
    }

    public void transaction_start() throws SQLException {
        connection.setAutoCommit(false);
    }

    public void transaction_finish() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }

    public void rollback() throws SQLException {
        connection.rollback();
        connection.setAutoCommit(true);
    }

    public void close() {
        if (instance != null) {
            if (connection != null) {
                try {
                    connection.close(); // 물리적 connection 이 close 되는 것이 아니라 pool 에 반환된다.
                } catch (SQLException e) {
                    System.out.println(e.getClass().getSimpleName());
                    System.out.println(Arrays.toString(e.getStackTrace()));
                }
            }
            instance = null;
        }
    }

    private class ColumnAliasRowProcessor extends BasicRowProcessor {
        @Override
        public Map<String, Object> toMap(ResultSet rs) throws SQLException {
            CaseInsensitiveMap<String, Object> result = new CaseInsensitiveMap<>();
            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();

            for (int i = 1; i <= cols; i++) {
                result.put(rsmd.getColumnLabel(i), rs.getObject(i));
            }

            return result;
        }
    }
}

