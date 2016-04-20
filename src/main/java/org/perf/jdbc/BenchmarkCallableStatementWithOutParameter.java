
package org.perf.jdbc;

import org.openjdk.jmh.annotations.Benchmark;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.*;

public class BenchmarkCallableStatementWithOutParameter extends BenchmarkInit {

    @Benchmark
    public void mysql(MyState state) throws Throwable {
        callableStatementWithOutParameter(state.mysqlConnection);
    }

    @Benchmark
    public void mariadb(MyState state) throws Throwable {
        callableStatementWithOutParameter(state.mariadbConnection);
    }

    private void callableStatementWithOutParameter(Connection connection) throws SQLException {
        CallableStatement storedProc = connection.prepareCall("{call inOutParam(?)}");
        storedProc.setInt(1, 1);
        storedProc.registerOutParameter(1, Types.INTEGER);
        storedProc.execute();
        storedProc.getString(1);
    }

}
