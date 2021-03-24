
package com.acme.eshop;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.exit;



        import com.thedeanda.lorem.Lorem;
        import com.thedeanda.lorem.LoremIpsum;
        import com.zaxxer.hikari.HikariConfig;
        import com.zaxxer.hikari.HikariDataSource;
        import org.h2.tools.Server;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;

        import java.io.IOException;
        import java.io.InputStream;
        import java.sql.PreparedStatement;
        import java.sql.ResultSet;
        import java.sql.SQLException;
        import java.sql.Statement;
        import java.util.Arrays;
        import java.util.Properties;
        import java.util.concurrent.ThreadLocalRandom;
        import java.util.ArrayList;
        import java.util.List;

        import static java.lang.System.exit;

public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);
    private static final String DB_URL = "jdbc:h2:mem:sample";
    private static final String DB_USERNAME = "sa";
    private static final String DB_PASSWORD = "";
    private final Properties sqlCommands = new Properties();
    private final Lorem generator = LoremIpsum.getInstance();
    private HikariDataSource hikariDatasource;

    private Server h2Server, webServer;

    public static void main(String[] args) throws InterruptedException {
        Database eshop = new Database();
        // Load SQL commands
        eshop.loadSqlCommands();

        // Start H2 database server
        eshop.startH2Server();

        eshop.initiateConnectionPooling();


        // Create table
        logger.info("CREATING TABLES");
        eshop.createTables();

        // Insert data
        logger.info("INSERT PRODUCTS");
            eshop.insertProducts();
        // Insert data in batch mode
            //eshop.batchInsertData();
            //eshop.selectData();

        // Update data
            //eshop.updateData();
            //eshop.selectData();

        // Delete data
            //eshop.deleteData();
            //eshop.selectData();

            //eshop.selectData();

        Thread.sleep(30000);

        eshop.stopH2Server();

        // Stop H2 database server via shutdown hook
            //Runtime.getRuntime().addShutdownHook(new Thread(() -> eshop.stopH2Server()));

            //while (true) {
            //}
    }


    private void loadSqlCommands() {
        try (InputStream inputStream = Database.class.getClassLoader().getResourceAsStream("sql.properties")) {
            if (inputStream == null) {
                logger.error("Unable to load SQL commands.");
                exit(-1);
            }
            sqlCommands.load(inputStream);
        } catch (IOException e) {
            logger.error("Error while loading SQL commands.", e);
        }
    }

    private void startH2Server() {
        try {
            h2Server = Server.createTcpServer("-tcpAllowOthers", "-tcpDaemon");
            h2Server.start();
            webServer = Server.createWebServer("-webAllowOthers", "-webDaemon");
            webServer.start();
            logger.info("H2 Database server is now accepting connections.");
        } catch (SQLException throwables) {
            logger.error("Unable to start H2 database server.", throwables);
            exit(-1);
        }
    }

    private void initiateConnectionPooling() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USERNAME);
        config.setPassword(DB_PASSWORD);

        config.setConnectionTimeout(10000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(1800000);
        config.setMinimumIdle(1);
        config.setMaxLifetime(5);
        config.setAutoCommit(true);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtsCacheSize", "500");
        hikariDatasource = new HikariDataSource(config);
    }

    private void createTables() {
        try (Statement statement = hikariDatasource.getConnection().createStatement()) {
            int resultRows;
            resultRows = statement.executeUpdate(sqlCommands.getProperty("create.table.001"));
            logger.info("CUSTOMER TABLE CREATED.");
            resultRows = statement.executeUpdate(sqlCommands.getProperty("create.table.002"));
            logger.info("PRODUCT TABLE CREATED.");
            resultRows = statement.executeUpdate(sqlCommands.getProperty("create.table.003"));
            logger.info("ORDER TABLE CREATED.");
            resultRows = statement.executeUpdate(sqlCommands.getProperty("create.table.004"));
            logger.info("ORDER ID TABLE CREATED.");

            //logger.debug("Statement returned {}.", resultRows);
        } catch (SQLException throwables) {
            logger.error("Unable to create target database table.", throwables);
        }
    }

    private void insertProducts() {
        try (Statement statement = hikariDatasource.getConnection().createStatement()) {
            int resultRows = statement.executeUpdate(sqlCommands.getProperty("insert.table.006"));
            logger.debug("Statement returned {}.", resultRows);
            logger.info("Statement returned {}.", resultRows);
            resultRows = statement.executeUpdate(sqlCommands.getProperty("insert.table.007"));
            logger.debug("Statement returned {}.", resultRows);
            resultRows = statement.executeUpdate(sqlCommands.getProperty("insert.table.008"));
            logger.debug("Statement returned {}.", resultRows);
            resultRows = statement.executeUpdate(sqlCommands.getProperty("insert.table.009"));
            logger.debug("Statement returned {}.", resultRows);
            resultRows = statement.executeUpdate(sqlCommands.getProperty("insert.table.010"));
            logger.debug("Statement returned {}.", resultRows);

        } catch (SQLException throwables) {
            logger.error("Error occurred while inserting data.", throwables);
        }
    }

    private void batchInsertData() {
        try (PreparedStatement preparedStatement = hikariDatasource.getConnection().prepareStatement(
                sqlCommands.getProperty("insert.table.000"))) {
            generateData(preparedStatement, 10);

            int[] affectedRows = preparedStatement.executeBatch();
            logger.debug("Rows inserted {}.", Arrays.stream(affectedRows).sum());

        } catch (SQLException throwables) {
            logger.error("Error occurred while batch inserting data.", throwables);
        }
    }

    private void selectData() {
        try (Statement statement = hikariDatasource.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(sqlCommands.getProperty("select.table.001"))) {

            while (resultSet.next()) {
                //@formatter:off
                logger.info("id:{}, firstName:{}, lastName:{}, age:{}.",
                        resultSet.getLong("ID"),
                        resultSet.getString("FIRSTNAME"),
                        resultSet.getString("LASTNAME"),
                        resultSet.getInt("AGE"));
                //@formatter:on
            }
        } catch (SQLException throwables) {
            logger.error("Error occurred while retrieving data", throwables);
        }
    }

    private void updateData() {
        try (Statement statement = hikariDatasource.getConnection().createStatement()) {
            int resultRows = statement.executeUpdate(sqlCommands.getProperty("update.table.001"));

            logger.debug("Rows updated {}.", resultRows);

        } catch (SQLException throwables) {
            logger.error("Error occurred while updating data.", throwables);
        }
    }

    private void deleteData() {
        try (Statement statement = hikariDatasource.getConnection().createStatement()) {
            int resultRows = statement.executeUpdate(sqlCommands.getProperty("delete.table.001"));

            logger.debug("Rows updated {}.", resultRows);

        } catch (SQLException throwables) {
            logger.error("Error occurred while updating data.", throwables);
        }
    }

    private void stopH2Server() {
        if (h2Server == null || webServer == null) {
            return;
        }

        if (h2Server.isRunning(true)) {
            h2Server.stop();
            h2Server.shutdown();
        }
        if (webServer.isRunning(true)) {
            webServer.stop();
            webServer.shutdown();
        }
        logger.info("H2 Database server has been shutdown.");
    }

    private void generateData(PreparedStatement preparedStatement, int howMany) throws SQLException {
        for (int i = 1; i <= howMany; i++) {
            preparedStatement.clearParameters();

            preparedStatement.setLong(1, 1005 + i);
            preparedStatement.setString(2, generator.getFirstName());
            preparedStatement.setString(3, generator.getLastName());
            preparedStatement.setInt(4, ThreadLocalRandom.current().nextInt(18, 70));
            preparedStatement.addBatch();
        }
    }
}
