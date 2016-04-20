
package org.perf.jdbc;

import org.openjdk.jmh.annotations.Benchmark;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.*;

public class BenchmarkCallableStatementWithInParameter extends BenchmarkInit {

    @Benchmark
    public void mysql(MyState state) throws Throwable {
        callableStatementWithInParameter(state.mysqlConnection);
    }

    @Benchmark
    public void mariadb(MyState state) throws Throwable {
        callableStatementWithInParameter(state.mariadbConnection);
    }

    private void callableStatementWithInParameter(Connection connection) throws SQLException {
        CallableStatement stmt = connection.prepareCall("{call withResultSet(?)}");
        stmt.setInt(1, 1);
        ResultSet rs = stmt.executeQuery();
    }

}
