package org.perf.jdbc.common;

import org.openjdk.jmh.annotations.*;

import java.sql.*;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 20)
@Measurement(iterations = 20)
@Fork(value = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class BenchmarkInit {

    @State(Scope.Thread)
    public static class MyState {

        public Connection mysqlConnectionRewrite;
        public Connection mysqlConnection;
        public Connection mysqlConnectionText;
        public Connection mysqlFailoverConnection;

        public Statement mysqlStatementRewrite;
        public Statement mysqlStatement;
        public Statement mysqlStatementFailover;

        public Connection mariadbConnectionRewrite;
        public Connection mariadbConnection;
        public Connection mariadbConnectionText;
        public Connection mariadbFailoverConnection;

        public Statement mariadbStatementRewrite;
        public Statement mariadbStatement;
        public Statement mariadbStatementFailover;

        public Connection drizzleConnectionText;

        public Statement drizzleStatement;

        public String[] insertData = new String[1000];
        private static final Random rand = new Random();

        private Connection createConnection(String className, String url, Properties props) throws Exception {
            return ((Driver) Class.forName(className).newInstance()).connect(url, props);
        }

        private Connection createConnection(String className, String url) throws Exception {
            return ((Driver) Class.forName(className).newInstance()).connect(url, new Properties());
        }

        @Setup(Level.Trial)
        public void doSetup() throws Exception {
            String mysqlDriverClass = "com.mysql.jdbc.Driver";
            String mariaDriverClass = "org.mariadb.jdbc.Driver";
            String drizzleDriverClass = "org.drizzle.jdbc.DrizzleDriver";

            String baseUrl = "jdbc:mysql://localhost:3306/testj";
            String baseDrizzle = "jdbc:drizzle://localhost:3306/testj";

            Properties prepareProperties = new Properties();
            prepareProperties.setProperty("user", "perf");
            prepareProperties.setProperty("password", "!Password0");
            prepareProperties.setProperty("useServerPrepStmts", "true");
            prepareProperties.setProperty("cachePrepStmts", "true");
            prepareProperties.setProperty("useSSL", "false");

            Properties textProperties = new Properties();
            textProperties.setProperty("user", "perf");
            textProperties.setProperty("password", "!Password0");
            textProperties.setProperty("useServerPrepStmts", "false");
            textProperties.setProperty("useSSL", "false");

            Properties textPropertiesDrizzle = new Properties();
            textPropertiesDrizzle.setProperty("user", "perf");
            textPropertiesDrizzle.setProperty("password", "!Password0");

            String urlRewrite = "jdbc:mysql://localhost:3306/testj?user=perf&rewriteBatchedStatements=true&useSSL=false&password=!Password0";
            String urlFailover = "jdbc:mysql:replication://localhost:3306,localhost:3306/testj?"
                    + "user=perf&useServerPrepStmts=true&validConnectionTimeout=0&cachePrepStmts=true&useSSL=false&password=!Password0";

            //create different kind of connection
            mysqlConnection = createConnection(mysqlDriverClass, baseUrl, prepareProperties);
            mariadbConnection = createConnection(mariaDriverClass, baseUrl, prepareProperties);

            mysqlConnectionText =  createConnection(mysqlDriverClass, baseUrl, textProperties);
            mariadbConnectionText =  createConnection(mariaDriverClass, baseUrl, textProperties);
            drizzleConnectionText = createConnection(drizzleDriverClass, baseDrizzle, textPropertiesDrizzle);

            mysqlConnectionRewrite = createConnection(mysqlDriverClass, urlRewrite);
            mariadbConnectionRewrite = createConnection(mariaDriverClass, urlRewrite);

            mysqlFailoverConnection = createConnection(mysqlDriverClass, urlFailover);
            mariadbFailoverConnection = createConnection(mariaDriverClass, urlFailover);

            mysqlStatementFailover = mysqlFailoverConnection.createStatement();
            mariadbStatementFailover = mariadbFailoverConnection.createStatement();

            mysqlStatement = mysqlConnection.createStatement();
            mariadbStatement = mariadbConnection.createStatement();
            drizzleStatement = drizzleConnectionText.createStatement();

            mysqlStatementRewrite = mysqlConnectionRewrite.createStatement();
            mariadbStatementRewrite = mariadbConnectionRewrite.createStatement();

            //use black hole engine. so test are not stored and to avoid server disk access permitting more stable result
            //if "java.sql.SQLSyntaxErrorException: Unknown storage engine 'BLACKHOLE'". restart database
            try {
                mysqlStatement.execute("INSTALL SONAME 'ha_blackhole'");
            } catch (Exception e) { }

            mysqlStatement.execute("CREATE TABLE IF NOT EXISTS PerfTextQuery(charValue VARCHAR(100) NOT NULL) ENGINE = BLACKHOLE");
            mysqlStatement.execute("CREATE TABLE IF NOT EXISTS PerfTextQueryBlob(blobValue LONGBLOB NOT NULL) ENGINE = BLACKHOLE");
            mysqlStatement.execute("CREATE TABLE IF NOT EXISTS PerfReadQuery(id int, charValue VARCHAR(100) NOT NULL )");
            mysqlStatement.execute("CREATE TABLE IF NOT EXISTS PerfReadQueryBig(charValue VARCHAR(5000), charValue2 VARCHAR(5000) NOT NULL)");
            mysqlStatement.execute("DROP PROCEDURE IF EXISTS withResultSet");
            mysqlStatement.execute("DROP PROCEDURE IF EXISTS inoutParam");
            mysqlStatement.execute("DROP FUNCTION IF EXISTS testFunctionCall");
            mysqlStatement.execute("CREATE PROCEDURE withResultSet(a int) begin select a; end");
            mysqlStatement.execute("CREATE PROCEDURE inoutParam(INOUT p1 INT) begin set p1 = p1 + 1; end");
            mysqlStatement.execute("CREATE FUNCTION testFunctionCall(a float, b bigint, c int) RETURNS INT NO SQL \n"
                    + "BEGIN \n"
                    + "RETURN a; \n"
                    + "END");
            mysqlStatement.execute("TRUNCATE PerfTextQuery");
            mysqlStatement.execute("TRUNCATE PerfTextQueryBlob");
            mysqlStatement.execute("TRUNCATE PerfReadQuery");
            mysqlStatement.execute("TRUNCATE PerfReadQueryBig");


            //Insert DATA to permit test read perf
            PreparedStatement preparedStatement = mysqlConnectionRewrite.prepareStatement("INSERT INTO PerfReadQuery (id, charValue) values (?, ?)");
            for (int i = 0; i < 1000; i++) {
                preparedStatement.setInt(1, i);
                preparedStatement.setString(2, "abc" + i + "'");
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();

            byte[] arr = new byte[5000];
            for (int i = 0; i < 5000; i++) {
                arr[i] = (byte)(i % 128);
            }
            String data = new String(arr);
            PreparedStatement preparedStatement2 = mysqlConnectionRewrite.prepareStatement("INSERT INTO PerfReadQueryBig (charValue, charValue2) values (?, ?)");
            for (int i = 0; i < 1000; i++) {
                preparedStatement2.setString(1, data);
                preparedStatement2.setString(2, data);
                preparedStatement2.addBatch();
            }
            preparedStatement2.executeBatch();

            //populate data
            for (int i = 0; i < 1000; i++) {
                insertData[i] = randomAscii(20);
            }

        }

        /**
         * Generate a random ASCII string of a given length.
         */
        public static String randomAscii(int length) {
            int interval='~'-' '+1;

            byte []buf = new byte[length];
            rand.nextBytes(buf);
            for (int i = 0; i < length; i++) {
                if (buf[i] < 0) {
                    buf[i] = (byte)((-buf[i] % interval) + ' ');
                } else {
                    buf[i] = (byte)((buf[i] % interval) + ' ');
                }
            }
            return new String(buf);
        }

        @TearDown(Level.Trial)
        public void doTearDown() throws SQLException {
            mysqlStatementRewrite.close();
            mysqlStatement.close();
            mysqlStatementFailover.close();

            mariadbStatementRewrite.close();
            mariadbStatement.close();
            mariadbStatementFailover.close();

            drizzleStatement.close();

            mysqlConnection.close();
            mysqlConnectionRewrite.close();
            mysqlConnectionText.close();
            mysqlFailoverConnection.close();

            mariadbConnection.close();
            mariadbConnectionRewrite.close();
            mariadbConnectionText.close();
            mariadbFailoverConnection.close();

            drizzleConnectionText.close();
        }
    }

}
