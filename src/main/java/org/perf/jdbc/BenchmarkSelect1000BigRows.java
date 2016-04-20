package org.perf.jdbc;

import org.openjdk.jmh.annotations.Benchmark;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BenchmarkSelect1000BigRows extends BenchmarkInit {

    @Benchmark
    public void mysql(MyState state) throws Throwable {
        select1000BigRow(state.mysqlStatement);
    }

    @Benchmark
    public void mariadb(MyState state) throws Throwable {
        select1000BigRow(state.mariadbStatement);
    }

    private void select1000BigRow(Statement statement) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT * FROM PerfReadQueryBig");
        while (rs.next()) {
            rs.getString(1);
            rs.getString(2);
        }
    }

}
