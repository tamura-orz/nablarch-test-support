package nablarch.test.support.db.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import oracle.ucp.jdbc.JDBCConnectionPoolStatistics;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import nablarch.core.repository.di.ComponentFactory;

public class DataSourceFactory implements ComponentFactory<DataSource> {

    private static final String POOL_NAME = "test";

    private static DataSource dataSource = null;

    private String user;

    private String password;

    private String url;

    private String dbName;

    private String serverName;

    private int portNumber;

    private String connectionFactoryClassName;

    private String driverType;

    private Map<String, String> additional = new HashMap<String, String>();

    @Override
    public synchronized DataSource createObject() {

        if (dataSource != null) {
            if (dataSource instanceof PoolDataSource) {
                final JDBCConnectionPoolStatistics statistics = ((PoolDataSource) dataSource).getStatistics();
                System.err.println(
                        "Universal Connection Pool Statistics: " + statistics);


                if (statistics != null) {
                    final int i = statistics.getAvailableConnectionsCount() - statistics.getBorrowedConnectionsCount();
                    List<Connection> connectionList = new ArrayList<Connection>();
                    try {
                        for (int i1 = 0; i1 < i; i1++) {
                            connectionList.add(dataSource.getConnection());
                        }
                        for (Connection connection : connectionList) {
                            connection.setAutoCommit(true);
                            connection.close();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return dataSource;
        }

        try {
            PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
            pds.setConnectionPoolName(POOL_NAME);
            pds.setConnectionFactoryClassName(connectionFactoryClassName);
            pds.setServerName(serverName);
            pds.setPortNumber(portNumber);
            pds.setDatabaseName(dbName);
            pds.setURL(url);
            pds.setUser(user);
            pds.setPassword(password);
            pds.setMaxPoolSize(20);
            pds.setInitialPoolSize(2);
            pds.setMaxStatements(20);
            if (driverType != null) {
                pds.setConnectionFactoryProperty("driverType", driverType);
            }
            for (Entry<String, String> prop : additional.entrySet()) {
                pds.setConnectionFactoryProperty(prop.getKey(), prop.getValue());
            }
            dataSource = pds;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataSource;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public void setConnectionFactoryClassName(String connectionFactoryClassName) {
        this.connectionFactoryClassName = connectionFactoryClassName;
    }

    public void setDriverType(String driverType) {
        this.driverType = driverType;
    }

    public void setAdditionalProperties(Map<String, String> additional) {
        this.additional = additional;
    }
}

