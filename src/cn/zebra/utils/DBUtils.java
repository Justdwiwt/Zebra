package cn.zebra.utils;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBUtils {
    private static ComboPooledDataSource source = new ComboPooledDataSource();

    private DBUtils() {
    }

    public static DataSource getSource() {
        return source;
    }

    @SuppressWarnings("unused")
    public static Connection getConn() {
        try {
            return source.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
