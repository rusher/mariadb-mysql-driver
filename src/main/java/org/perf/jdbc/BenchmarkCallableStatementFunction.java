
package org.perf.jdbc;

import org.openjdk.jmh.annotations.*;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.*;
import java.util.concurrent.TimeUnit;

public class BenchmarkCallableStatementFunction extends BenchmarkInit {

    @Benchmark
    public void mysql(MyState state) throws Throwable {
        callableStatementFunction(state.mysqlConnection);
    }

    @Benchmark
    public void mariadb(MyState state) throws Throwable {
        callableStatementFunction(state.mariadbConnection);
    }

    private void callableStatementFunction(Connection connection) throws SQLException {
        CallableStatement callableStatement = connection.prepareCall("{? = CALL testFunctionCall(?,?,?)}");
        callableStatement.registerOutParameter(1, Types.INTEGER);
        callableStatement.setFloat(2, 2);
        callableStatement.setInt(3, 1);
        callableStatement.setInt(4, 1);
        callableStatement.execute();
        callableStatement.close();
    }


}
