
package org.perf.jdbc;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchmarkBatchInsertRewrite extends BenchmarkInit {

    @Benchmark
    public void mysql(MyState state) throws Throwable {
        executeBatch(state.mysqlConnectionRewrite);
    }

    @Benchmark
    public void mariadb(MyState state) throws Throwable {
        executeBatch(state.mariadbConnectionRewrite);
    }

    private void executeBatch(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO PerfTextQuery (charValue) values (?)");
        for (int i = 0; i < 1000; i++) {
            preparedStatement.setString(1, "abc");
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
    }

}
