
package org.perf.jdbc;

import org.openjdk.jmh.annotations.*;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

public class BenchmarkOneInsert extends BenchmarkInit {

    @Benchmark
    public void mysql(MyState state) throws SQLException {
        executeOneInsert(state.mysqlStatement);
    }

    @Benchmark
    public void mariadb(MyState state) throws SQLException {
        executeOneInsert(state.mariadbStatement);
    }

    private void executeOneInsert(Statement stmt) throws SQLException {
        stmt.execute("INSERT INTO PerfTextQuery (charValue) values ('abc')");
    }

}
