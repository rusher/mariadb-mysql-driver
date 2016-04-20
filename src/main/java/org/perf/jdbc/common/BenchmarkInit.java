package org.perf.jdbc.common;

import org.openjdk.jmh.annotations.*;

import java.sql.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 60)
@Measurement(iterations = 600)
@Fork(value = 1)
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

        private Connection createConnection(String className, String url) throws Exception {
            return ((Driver) Class.forName(className).newInstance()).connect(url, new Properties());
        }

        @Setup(Level.Trial)
        public void doSetup() throws Exception {
            String mysqlDriverClass = "com.mysql.jdbc.Driver";
            String mariaDriverClass = "org.mariadb.jdbc.Driver";
            // add &useSSL=false&serverTimezone=UTC for testing 6.0.2 mysql driver
            String urlPrepare = "jdbc:mysql://localhost:3306/testj?user=root&useServerPrepStmts=true";
            String urlWithoutPrepare = "jdbc:mysql://localhost:3306/testj?user=root&useServerPrepStmts=false";
            String urlRewrite = "jdbc:mysql://localhost:3306/testj?user=root&rewriteBatchedStatements=true";
            String urlFailover = "jdbc:mysql:replication://localhost:3306,localhost:3306/testj?user=root&useServerPrepStmts=true&validConnectionTimeout=0";

            //create different kind of connection
            mysqlConnection = createConnection(mysqlDriverClass, urlPrepare);
            mariadbConnection = createConnection(mariaDriverClass, urlPrepare);

            mysqlConnectionText =  createConnection(mysqlDriverClass, urlWithoutPrepare);
            mariadbConnectionText =  createConnection(mariaDriverClass, urlWithoutPrepare);

            mysqlConnectionRewrite = createConnection(mysqlDriverClass, urlRewrite);
            mariadbConnectionRewrite = createConnection(mariaDriverClass, urlRewrite);

            mysqlFailoverConnection = createConnection(mysqlDriverClass, urlFailover);
            mariadbFailoverConnection = createConnection(mariaDriverClass, urlFailover);

            mysqlStatementFailover = mysqlFailoverConnection.createStatement();
            mariadbStatementFailover = mariadbFailoverConnection.createStatement();

            mysqlStatement = mysqlConnection.createStatement();
            mariadbStatement = mariadbConnection.createStatement();

            mysqlStatementRewrite = mysqlConnectionRewrite.createStatement();
            mariadbStatementRewrite = mariadbConnectionRewrite.createStatement();

            //use black hole engine. so test are not stored and to avoid server disk access permetting more stable result
            //if "java.sql.SQLSyntaxErrorException: Unknown storage engine 'BLACKHOLE'". restart database
            mysqlStatement.execute("INSTALL SONAME 'ha_blackhole'");

            mysqlStatement.execute("CREATE TABLE IF NOT EXISTS PerfTextQuery(charValue VARCHAR(100) NOT NULL) ENGINE = BLACKHOLE");
            mysqlStatement.execute("CREATE TABLE IF NOT EXISTS PerfTextQueryBlob(blobValue LONGBLOB NOT NULL) ENGINE = BLACKHOLE");
            mysqlStatement.execute("CREATE TABLE IF NOT EXISTS PerfReadQuery(id int, charValue VARCHAR(100) NOT NULL )");
            mysqlStatement.execute("CREATE TABLE IF NOT EXISTS PerfReadQueryBig(charValue VARCHAR(5000), charValue2 VARCHAR(5000) NOT NULL)");
            mysqlStatement.execute("CREATE PROCEDURE IF NOT EXISTS withResultSet(a int) begin select a; end");
            mysqlStatement.execute("CREATE PROCEDURE IF NOT EXISTS inoutParam(INOUT p1 INT) begin set p1 = p1 + 1; end");
            mysqlStatement.execute("CREATE FUNCTION IF NOT EXISTS testFunctionCall(a float, b bigint, c int) RETURNS INT NO SQL \n"
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

        }

        @TearDown(Level.Trial)
        public void doTearDown() throws SQLException {
            mysqlConnection.close();
            mysqlConnectionRewrite.close();
            mysqlConnectionText.close();
            mysqlFailoverConnection.close();

            mariadbConnection.close();
            mariadbConnectionRewrite.close();
            mariadbConnectionText.close();
            mariadbFailoverConnection.close();
        }
    }

}
