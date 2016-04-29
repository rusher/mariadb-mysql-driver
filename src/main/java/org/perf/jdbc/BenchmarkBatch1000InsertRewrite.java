
package org.perf.jdbc;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.perf.jdbc.common.BenchmarkInit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchmarkBatch1000InsertRewrite extends BenchmarkInit {
    private String request = "INSERT INTO PerfTextQuery (charValue) values (?)";

    @Benchmark
    public int[] mysql(MyState state) throws Throwable {
        return executeBatch(state.mysqlConnectionRewrite, state.insertData);
    }

    @Benchmark
    public int[] mariadb(MyState state) throws Throwable {
        return executeBatch(state.mariadbConnectionRewrite, state.insertData);
    }

    private int[] executeBatch(Connection connection, String[] data) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(request);
        for (int i = 0; i < 1000; i++) {
            preparedStatement.setString(1, data[i]);
            preparedStatement.addBatch();
        }
        int[] updateCounts = preparedStatement.executeBatch();
        preparedStatement.close();
        return updateCounts;
    }

}
