package com.example.springproject.crawl;

import com.alibaba.druid.pool.DruidDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Druid {
    public static DruidDataSource dataSource;

    static {
        dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/repo");
        dataSource.setUsername("root");
        dataSource.setPassword("1234");
        dataSource.setInitialSize(10);
        dataSource.setMaxActive(50);
        dataSource.setMinIdle(10);
        dataSource.setMaxWait(5000);
        }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    public static void closeAll(Connection connection) throws SQLException {
        if (connection != null)
            connection.close();
    }
}
