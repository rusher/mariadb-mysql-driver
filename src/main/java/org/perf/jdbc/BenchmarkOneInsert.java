
package org.perf.jdbc;

import org.openjdk.jmh.annotations.*;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

public class BenchmarkOneInsert extends BenchmarkInit {
    private String request = "INSERT INTO PerfTextQuery (charValue) values ('abc')";

    @Benchmark
    public boolean mysql(MyState state) throws SQLException {
        return executeOneInsert(state.mysqlStatement);
    }

    @Benchmark
    public boolean mariadb(MyState state) throws SQLException {
        return executeOneInsert(state.mariadbStatement);
    }

    @Benchmark
    public boolean drizzle(MyState state) throws SQLException {
        return executeOneInsert(state.drizzleStatement);
    }

    private boolean executeOneInsert(Statement stmt) throws SQLException {
        return stmt.execute(request);
    }

}
