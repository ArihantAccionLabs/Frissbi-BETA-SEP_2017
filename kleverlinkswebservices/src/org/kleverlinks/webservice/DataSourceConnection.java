package org.kleverlinks.webservice;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DataSourceConnection {

    private static DataSourceConnection     datasource;
    private static ComboPooledDataSource cpds;

    private DataSourceConnection() throws IOException, SQLException, PropertyVetoException {
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass(Constants.JDBC_DRIVER); //loads the jdbc driver
        cpds.setJdbcUrl(Constants.DB_URL);
        cpds.setUser(Constants.USER);
        cpds.setPassword(Constants.PASS);

        // the settings below are optional -- c3p0 can work with defaults
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
        cpds.setMaxStatements(180);

    }

    public static Connection getDBConnection() throws IOException, SQLException, PropertyVetoException {
        if (datasource == null) {
            datasource = new DataSourceConnection();
            return cpds.getConnection();
        } else {
            return cpds.getConnection();
        }
    }

   

}