package org.perf.jdbc;

import org.openjdk.jmh.annotations.Benchmark;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BenchmarkSelect1RowFailover extends BenchmarkInit {
    private String request = "SELECT * FROM PerfReadQuery where id = 0";

    @Benchmark
    public String mysql(MyState state) throws Throwable {
        return select1Row(state.mysqlStatementFailover);
    }

    @Benchmark
    public String mariadb(MyState state) throws Throwable {
        return select1Row(state.mariadbStatementFailover);
    }

    private String select1Row(Statement statement) throws SQLException {
        ResultSet rs = statement.executeQuery(request);
        rs.next();
        String result = rs.getString(1);
        rs.close();
        return result;
    }

}
