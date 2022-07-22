package org.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class JdbcDemo {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://node:9030/test", "root", "");

        PreparedStatement ps = conn.prepareStatement("ALTER TABLE `test`.`test_cdc` \n" +
                "ADD COLUMN `car` varchar(255)");
        ps.execute();

    }
}
