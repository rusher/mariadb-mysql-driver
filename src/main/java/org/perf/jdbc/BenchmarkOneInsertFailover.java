
package org.perf.jdbc;

import org.openjdk.jmh.annotations.Benchmark;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.SQLException;
import java.sql.Statement;

public class BenchmarkOneInsertFailover extends BenchmarkInit {
    private String request = "INSERT INTO PerfTextQuery (charValue) values ('abc')";

    @Benchmark
    public boolean mysql(MyState state) throws SQLException {
        return executeOneInsert(state.mysqlStatementFailover);
    }

    @Benchmark
    public boolean mariadb(MyState state) throws SQLException {
        return executeOneInsert(state.mariadbStatementFailover);
    }

    private boolean executeOneInsert(Statement stmt) throws SQLException {
        return stmt.execute(request);
    }

}
