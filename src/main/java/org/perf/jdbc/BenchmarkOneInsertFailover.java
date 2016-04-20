
package org.perf.jdbc;

import org.openjdk.jmh.annotations.Benchmark;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.SQLException;
import java.sql.Statement;

public class BenchmarkOneInsertFailover extends BenchmarkInit {

    @Benchmark
    public void mysql(MyState state) throws SQLException {
        executeOneInsert(state.mysqlStatementFailover);
    }

    @Benchmark
    public void mariadb(MyState state) throws SQLException {
        executeOneInsert(state.mariadbStatementFailover);
    }

    private void executeOneInsert(Statement stmt) throws SQLException {
        stmt.execute("INSERT INTO PerfTextQuery (charValue) values ('abc')");
    }

}
