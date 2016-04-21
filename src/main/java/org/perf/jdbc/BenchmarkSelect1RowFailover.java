package org.perf.jdbc;

import org.openjdk.jmh.annotations.Benchmark;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BenchmarkSelect1RowFailover extends BenchmarkInit {


    @Benchmark
    public void mysql(MyState state) throws Throwable {
        select1Row(state.mysqlStatementFailover);
    }

    @Benchmark
    public void mariadb(MyState state) throws Throwable {
        select1Row(state.mariadbStatementFailover);
    }

    private void select1Row(Statement statement) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT * FROM PerfReadQuery where id = 0");
        rs.next();
        rs.getString(1);
        rs.close();
    }


}
