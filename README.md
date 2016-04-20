# JMH performance MariaDB/MySQL driver test

We always talk about performance, but the thing is always "Measure, don’t guess!".
This is a benchmark of [MariaDB java connector](https://github.com/MariaDB/mariadb-connector-j) versus [MySQL java connector](https://github.com/mysql/mysql-connector-j).
MariaDB and MySQL databases are using the same exchange protocol, and driver offer similar functionalities. 

This is a Driver benchmark using [JMH microbenchmark](http://openjdk.java.net/projects/code-tools/jmh/)
developed by the same guys in Oracle who implement the JIT, and is delivered as openJDK tools.

## The tests
Class BenchmarkInit initialize connections using MySQL and MariaDB drivers before tests.

test example org.perf.jdbc.BenchmarkPrepareStatementOneInsert : 
```java
public class BenchmarkPrepareStatementOneInsert extends BenchmarkInit {

    @Benchmark
    public void mysql(MyState state) throws Throwable {
        executeOneInsertPrepare(state.mysqlConnection);
    }
    
    @Benchmark
    public void mariadb(MyState state) throws Throwable {
        executeOneInsertPrepare(state.mariadbConnection);
    }
    
    private void executeOneInsertPrepare(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO PerfTextQuery (charValue) values (?)");
        preparedStatement.setString(1, "abc");
        preparedStatement.execute();
    }
}
```

The test will execute the prepareStatement "INSERT INTO PerfTextQuery (charValue) values (?)" using a connection issued from MySQL or MariaDB driver.

Tests are launched multiple times using 5 fork, 10 warmup iterations of one second followed by 20 measurement iterations of one second.


List of tests and their signification :

|Benchmark       |description |
|-----------|:----------|
| BenchmarkOneInsert* | execute query "INSERT INTO PerfTextQuery (charValue) values ('abc')"|
| BenchmarkOneInsertFailover*|same as BenchmarkOneInsert but using failover configuration|
| BenchmarkPrepareStatementOneInsert*|same as BenchmarkOneInsert but using "prepare" |
| BenchmarkPrepareStatementOneInsertFailover*|same as BenchmarkOneInsert but using "prepare" and failover configuration |
| BenchmarkSelect1Row|execute query "SELECT * FROM PerfReadQuery where id = 0";|
| BenchmarkSelect1RowFailover|same than BenchmarkSelect1Row but using failover configuration|
| BenchmarkSelect1000Rows|execute query "SELECT * FROM PerfReadQuery" (table with 1000 rows, each rows contain < 10 bytes) )|
| BenchmarkSelect1000BigRows|execute query "SELECT * FROM PerfReadQueryBig" (table with 1000 rows, each rows contain 10kb)|
| BenchmarkBatch1000InsertWithPrepare*|executing 1000 inserts using prepareStatement with "prepare" on server. (option useServerPrepStmts=true)|
| BenchmarkBatch1000InsertWithoutPrepare*|executing 1000 inserts. (option useServerPrepStmts=false)|
| BenchmarkBatch1000InsertRewrite*|executing 1000 inserts. (option rewriteBatchedStatements=true)|
| BenchmarkCallableStatementFunction|execute CallableStatement with query "{? = CALL testFunctionCall(?,?,?)}". Function created by "CREATE FUNCTION IF NOT EXISTS testFunctionCall(a float, b bigint, c int) RETURNS INT NO SQL \nBEGIN \nRETURN a; \nEND"|
| BenchmarkCallableStatementWithInParameter|execute CallableStatement with query "{call withResultSet(?)}". Procedure created with "CREATE PROCEDURE IF NOT EXISTS withResultSet(a int) begin select a; end"|
| BenchmarkCallableStatementWithOutParameter|execute CallableStatement with query "{call inOutParam(?)}". Procedure created with "CREATE PROCEDURE IF NOT EXISTS inoutParam(INOUT p1 INT) begin set p1 = p1 + 1; end"|

'* The goal is here to test the driver performance, not database, so INSERT's queries are send to a [BLACKHOLE](https://mariadb.com/kb/en/mariadb/blackhole/) engine (data are not stored). This permit to have more stable results.



## How run the tests
* install a MySQL / MariaDB database with user root without password
* create database "testj"
* install engine [BLACKHOLE](https://mariadb.com/kb/en/mariadb/blackhole/) using command "INSTALL SONAME 'ha_blackhole'" (This engine don't save data, permitting to execute INSERT queries with stable time result)
* restart database to activate the BLACKHOLE engine
* install a JRE

```script
mvn clean install
java -Xmx256m -Xms256m -Duser.country=US -Duser.language=en -jar target/benchmarks.jar > result.txt &
```
-Duser.country=US -Duser.language=en permit to avoid confusion with comma used as decimal separator / thousand separator according to countries
-Xmx256m -Xms256m is to limit java memory size so garbage time are more frequent.

## Read results 

Execution on a droplet on digitalocean.com using this parameters:
- CentOS 7.2 64bits
- 1GB memory
- 1 CPU
using default mariadb 10.1 configuration file

Results are in file "result.txt".
Complete results are the end of the file. Example of results : 



```
# Run complete. Total time: 01:17:39

Benchmark                                           Mode  Cnt      Score     Error  Units
BenchmarkBatchInsert1000Rewrite.mariadb             avgt  100      1.131 ±   0.010  ms/op
BenchmarkBatchInsert1000Rewrite.mysql               avgt  100      1.530 ±   0.069  ms/op
BenchmarkBatchInsert1000WithPrepare.mariadb         avgt  100     49.903 ±   1.318  ms/op
BenchmarkBatchInsert1000WithPrepare.mysql           avgt  100     62.583 ±   2.754  ms/op
BenchmarkBatchInsert1000WithoutPrepare.mariadb      avgt  100     62.332 ±   2.146  ms/op
BenchmarkBatchInsert1000WithoutPrepare.mysql        avgt  100     70.661 ±   0.636  ms/op
BenchmarkCallableStatementFunction.mariadb          avgt  100    106.032 ±   4.080  us/op
BenchmarkCallableStatementFunction.mysql            avgt  100    915.263 ±  73.758  us/op
BenchmarkCallableStatementWithInParameter.mariadb   avgt  100     75.878 ±   0.921  us/op
BenchmarkCallableStatementWithInParameter.mysql     avgt  100    654.043 ±  53.239  us/op
BenchmarkCallableStatementWithOutParameter.mariadb  avgt  100     68.076 ±   3.056  us/op
BenchmarkCallableStatementWithOutParameter.mysql    avgt  100    871.215 ± 108.167  us/op
BenchmarkOneInsert.mariadb                          avgt  100     60.511 ±   2.574  us/op
BenchmarkOneInsert.mysql                            avgt  100     70.912 ±   2.863  us/op
BenchmarkOneInsertFailover.mariadb                  avgt  100     64.531 ±   2.300  us/op
BenchmarkOneInsertFailover.mysql                    avgt  100     83.992 ±   1.553  us/op
BenchmarkPrepareStatementOneInsert.mariadb          avgt  100     59.480 ±   2.423  us/op
BenchmarkPrepareStatementOneInsert.mysql            avgt  100    197.476 ±   7.430  us/op
BenchmarkPrepareStatementOneInsertFailover.mariadb  avgt  100     58.344 ±   1.987  us/op
BenchmarkPrepareStatementOneInsertFailover.mysql    avgt  100    304.720 ±  20.155  us/op
BenchmarkSelect1000BigRows.mariadb                  avgt  100  31731.889 ± 434.607  us/op
BenchmarkSelect1000BigRows.mysql                    avgt  100  43211.154 ± 435.075  us/op
BenchmarkSelect1000Rows.mariadb                     avgt  100   1050.453 ±   9.372  us/op
BenchmarkSelect1000Rows.mysql                       avgt  100   1052.798 ±   7.483  us/op
BenchmarkSelect1Row.mariadb                         avgt  100    557.003 ±   4.430  us/op
BenchmarkSelect1Row.mysql                           avgt  100    568.452 ±   3.925  us/op
BenchmarkSelect1RowFailover.mariadb                 avgt  100    568.416 ±  13.683  us/op
BenchmarkSelect1RowFailover.mysql                   avgt  100    591.973 ±   4.935  us/op
```

##### How to read it :
BenchmarkBatchInsert1000Rewrite.mariadb             avgt  100      1.131 ±   0.010  ms/op
BenchmarkBatchInsert1000Rewrite.mysql               avgt  100      1.530 ±   0.069  ms/op

<div style="text-align:center"><img src ="Insert_1000_data_2.png" /></div>

BenchmarkBatchInsert1000Rewrite = executing 1000 inserts with option rewriteBatchedStatements=true.
Using mariadb driver, it take 1.131 millisecond to insert those 1000 data, and 99.9% of queries executes time are comprised between 1.121 (1.131 - 0.010) and 1.141 milliseconds (1.131 + 0.010).
Using MySQL java driver, execution time is 1.530 millisecond.   
(remember that INSERT queries are executed on BLACKHOLE engine, those number just reflect the execution time of the driver + echanges with database).

##### Other example : 
BenchmarkBatchInsert1000WithPrepare.mariadb         avgt  100     49.903 ±   1.318  ms/op
BenchmarkBatchInsert1000WithPrepare.mysql           avgt  100     62.583 ±   2.754  ms/op
BenchmarkBatchInsert1000WithoutPrepare.mariadb      avgt  100     62.332 ±   2.146  ms/op
BenchmarkBatchInsert1000WithoutPrepare.mysql        avgt  100     70.661 ±   0.636  ms/op

<div style="text-align:center"><img src ="Insert_1000_data.png" /></div>



