
package org.perf.jdbc;

import org.openjdk.jmh.annotations.Benchmark;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BenchmarkPrepareStatementOneInsertFailover extends BenchmarkInit {

    @Benchmark
    public void mysql(MyState state) throws Throwable {
        executeOneInsertPrepare(state.mysqlFailoverConnection);
    }

    @Benchmark
    public void mariadb(MyState state) throws Throwable {
        executeOneInsertPrepare(state.mariadbFailoverConnection);
    }

    private void executeOneInsertPrepare(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO PerfTextQuery (charValue) values (?)");
        preparedStatement.setString(1, "abc");
        preparedStatement.execute();
        preparedStatement.close();
    }

}
