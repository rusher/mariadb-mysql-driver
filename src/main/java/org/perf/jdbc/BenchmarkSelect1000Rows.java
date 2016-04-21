package org.perf.jdbc;

import org.openjdk.jmh.annotations.Benchmark;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BenchmarkSelect1000Rows extends BenchmarkInit {

    @Benchmark
    public void mysql(MyState state) throws Throwable {
        select1000Row(state.mysqlStatement);
    }

    @Benchmark
    public void mariadb(MyState state) throws Throwable {
        select1000Row(state.mariadbStatement);
    }

    @Benchmark
    public void drizzle(MyState state) throws Throwable {
        select1000Row(state.drizzleStatement);
    }

    private void select1000Row(Statement statement) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT * FROM PerfReadQuery");
        while (rs.next()) {
            rs.getString(1);
        }
        rs.close();
    }

}
