package com.rtbasia.sscc;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool {
    private BasicDataSource basicDataSource;

    public ConnectionPool() {
        basicDataSource = new BasicDataSource();

        basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        basicDataSource.setUrl("jdbc:mysql://localhost:3306/sscc");
        basicDataSource.setUsername("admin");
        basicDataSource.setPassword("password");
    }

    public Connection getConnection() throws SQLException {
        return basicDataSource.getConnection();
    }
}
