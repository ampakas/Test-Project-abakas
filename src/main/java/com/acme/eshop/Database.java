
package com.acme.eshop;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import static com.acme.eshop.Customer.CustomerType.*;
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

    public enum CustomerType {
        B2C, B2B, B2G;
    }
    public enum PaymentType {
        CASH, CREDIT;
    }

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
        logger.info("INSERT CUSTOMERS");
            eshop.insertCustomers();

        logger.info("1ST EXAMPLE");
        ////////////////////////////////////////////////
        Customer NewCustomer1 = new Customer();

        logger.info("SELECT CUSTOMER");

        eshop.getCustomer(NewCustomer1, 1001);

        OrderServiceImpl NewOrderService = new OrderServiceImpl();

        Item firstItem = new Item();
        eshop.getProduct(firstItem,101);
        firstItem.setQuantity(1);
        logger.info("firstItem name:{}.", firstItem.getName());

        Item secondItem = new Item();
        eshop.getProduct(secondItem,102);
        secondItem.setQuantity(2);

        NewOrderService.setCustomer(NewCustomer1);
        NewOrderService.setPaymentType(PaymentType.CASH);

        logger.info("SET CUSTOMER");
        logger.info("firstItem name:{}.", firstItem.getName());
        NewOrderService.addItem(firstItem);
        logger.info("SET firstItem");
        NewOrderService.addItem(secondItem);
        logger.info("SET secondItem");

        logger.info("TOTAL AMOUNT:{}.", NewOrderService.getTotalAmount());





        logger.info("CUSTOMER id:{}, name:{}, type:{}.",
                NewCustomer1.getId(),
                NewCustomer1.getName(),
                NewCustomer1.getType());

        logger.info("INSERT ORDER TO DATABASE");
        long currentOrder = eshop.setOrder(NewCustomer1,NewOrderService.getTotalAmount(),NewOrderService.getTotalQuantity());

        logger.info("INSERT ORDER ITEMS TO DATABASE");
        eshop.batchInsertOrders(NewOrderService.getOrder(),currentOrder  );

        NewOrderService.checkout();


/*
        logger.info("1ST EXAMPLE");
        ////////////////////////////////////////////////

        Customer NewCustomer2 = new Customer();

        logger.info("SELECT CUSTOMER");

        eshop.getCustomer(NewCustomer2, 1003);

        Item firstItem2 = new Item();
        eshop.getProduct(firstItem2,100);
        firstItem2.setQuantity(3);
        logger.info("firstItem name:{}.", firstItem2.getName());

        Item secondItem2 = new Item();
        eshop.getProduct(secondItem2,103);
        secondItem2.setQuantity(2);

        NewOrderService.setCustomer(NewCustomer2);
        NewOrderService.setPaymentType(PaymentType.CASH);

        logger.info("SET CUSTOMER");
        logger.info("firstItem name:{}.", firstItem2.getName());
        NewOrderService.addItem(firstItem2);
        logger.info("SET firstItem");
        NewOrderService.addItem(secondItem2);
        logger.info("SET secondItem");

        logger.info("TOTAL AMOUNT:{}.", NewOrderService.getTotalAmount());





        logger.info("CUSTOMER id:{}, name:{}, type:{}.",
                NewCustomer2.getId(),
                NewCustomer2.getName(),
                NewCustomer2.getType());

        logger.info("INSERT ORDER TO DATABASE");
        long currentOrder2 = eshop.setOrder(NewCustomer2,NewOrderService.getTotalAmount(),NewOrderService.getTotalQuantity());

        logger.info("INSERT ORDER ITEMS TO DATABASE");
        eshop.batchInsertOrders(NewOrderService.getOrder(),currentOrder2  );


*/



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

    private void insertCustomers() {
        try (Statement statement = hikariDatasource.getConnection().createStatement()) {
            int resultRows = statement.executeUpdate(sqlCommands.getProperty("insert.table.011"));
            logger.debug("Statement returned {}.", resultRows);
            logger.info("Statement returned {}.", resultRows);
            resultRows = statement.executeUpdate(sqlCommands.getProperty("insert.table.012"));
            logger.debug("Statement returned {}.", resultRows);
            resultRows = statement.executeUpdate(sqlCommands.getProperty("insert.table.013"));
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

      private void getCustomer(Customer myCustomer, long customerID) /*throws SQLException*/ {
        try (PreparedStatement  preparedStatement = hikariDatasource.getConnection().prepareStatement(
                sqlCommands.getProperty("select.table.002"))) {
            //try {
            preparedStatement.setLong(1,customerID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                //@formatter:off
                logger.info("CUSTOMER id:{}, name:{}, type:{}.",
                        resultSet.getLong("ID"),
                        resultSet.getString("NAME"),
                        resultSet.getInt("TYPE"));

                myCustomer.setId(resultSet.getLong("ID"));
                myCustomer.setName(resultSet.getString("NAME"));
                int customerType = (resultSet.getInt("TYPE"));
                if (customerType == 1) {myCustomer.setType(B2C); break;}
                else if (customerType == 2) {myCustomer.setType(B2B); break;}
                else if (customerType == 3) myCustomer.setType(B2G);
                //@formatter:on
           // } catch (SQLException ) {
          //          logger.error("Error occurred while retrieving data");
                }
        } catch (SQLException throwables) {
            logger.error("Error occurred while retrieving data", throwables);
        }
    }


    private void getProduct(Item myProduct, long ProductID) /*throws SQLException*/ {
        try (PreparedStatement  preparedStatement = hikariDatasource.getConnection().prepareStatement(
                sqlCommands.getProperty("select.table.003"))) {
            //try {
            preparedStatement.setLong(1,ProductID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                //@formatter:off
                logger.info("PRODUCT id:{}, name:{}, price:{}.",
                        resultSet.getLong("ID"),
                        resultSet.getString("NAME"),
                        resultSet.getBigDecimal("PRICE"));

                myProduct.setId(resultSet.getLong("ID"));
                myProduct.setName(resultSet.getString("NAME"));
                myProduct.setPrice(resultSet.getBigDecimal("PRICE"));

            }
        } catch (SQLException throwables) {
            logger.error("Error occurred while retrieving data", throwables);
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

    private long setOrder(Customer myCustomer, BigDecimal TotalAmount,  Integer TotalQuantity) /*throws SQLException*/ {
        try (PreparedStatement  preparedStatement = hikariDatasource.getConnection().prepareStatement(
                sqlCommands.getProperty("insert.table.020"))) {
            long orderId = ThreadLocalRandom.current().nextLong(2000, 3000);
            preparedStatement.setLong(1,orderId);
            preparedStatement.setLong(2,myCustomer.getId());
            preparedStatement.setLong(3,TotalQuantity);
            preparedStatement.setBigDecimal(4,TotalAmount);
            //ResultSet resultSet = preparedStatement.executeQuery();
            int resultRows = preparedStatement.executeUpdate();

            return orderId;
        } catch (SQLException throwables) {
            logger.error("Error occurred while retrieving data", throwables);
            return 1;
        }
    }

    private void batchInsertOrders(List orders, long orderId) {
        try (PreparedStatement preparedStatement = hikariDatasource.getConnection().prepareStatement(
                sqlCommands.getProperty("insert.table.030"))) {
            generateOrdersData(preparedStatement, orders, orderId);

            int[] affectedRows = preparedStatement.executeBatch();
            logger.debug("Rows inserted {}.", Arrays.stream(affectedRows).sum());

        } catch (SQLException throwables) {
            logger.error("Error occurred while batch inserting data.", throwables);
        }
    }

    private void generateOrdersData(PreparedStatement preparedStatement, List<Item> Orders, long orderId) throws SQLException {

            for (Item order : Orders) {
            preparedStatement.clearParameters();

            preparedStatement.setLong(1, orderId);
            preparedStatement.setLong(2, order.getId());
            preparedStatement.setLong(3, order.getQuantity());
            preparedStatement.addBatch();
        }
    }

    private void averageOrdersCost() {
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


}
