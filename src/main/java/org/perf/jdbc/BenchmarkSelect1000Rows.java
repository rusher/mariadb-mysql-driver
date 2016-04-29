package org.perf.jdbc;

import org.openjdk.jmh.annotations.Benchmark;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BenchmarkSelect1000Rows extends BenchmarkInit {
    private String request = "SELECT * FROM PerfReadQuery";

    @Benchmark
    public ResultSet mysql(MyState state) throws Throwable {
        return select1000Row(state.mysqlStatement);
    }

    @Benchmark
    public ResultSet mariadb(MyState state) throws Throwable {
        return select1000Row(state.mariadbStatement);
    }

    @Benchmark
    public ResultSet drizzle(MyState state) throws Throwable {
        return select1000Row(state.drizzleStatement);
    }

    private ResultSet select1000Row(Statement statement) throws SQLException {
        ResultSet rs = statement.executeQuery(request);
        while (rs.next()) {
            rs.getString(1);
        }
        rs.close();
        return rs;
    }

}
