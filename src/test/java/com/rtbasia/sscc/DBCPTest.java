package com.rtbasia.sscc;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SpringBootTest
public class DBCPTest {

    @Test
    public void testConnection() throws SQLException {
        BasicDataSource basicDataSource = new BasicDataSource();

        basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        basicDataSource.setUrl("jdbc:mysql://localhost:3306/sscc");
        basicDataSource.setUsername("admin");
        basicDataSource.setPassword("password");

        Connection conn = basicDataSource.getConnection();
        String sql = "select * from cache";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            System.out.println(rs.getString("value"));
        }

        conn.close();
    }
}
