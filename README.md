# JMH performance MariaDB/MySQL driver test

We always talk about performance, but the thing is always "Measure, don’t guess!".
This is a benchmark of [MariaDB java connector](https://github.com/MariaDB/mariadb-connector-j) versus [MySQL java connector](https://github.com/mysql/mysql-connector-j).
MariaDB and MySQL databases are using the same exchange protocol, and driver offer similar functionalities. 

This is a Driver benchmark using [JMH microbenchmark](http://openjdk.java.net/projects/code-tools/jmh/)
developed by the same guys in Oracle who implement the JIT, and is delivered as openJDK tools.

## Understand the tests
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

Tests are launched multiple times using only 1 fork (we want JIT optimization !) : 60 warmup iterations of 1s followed by 600 measurement iterations of 1s


## How run the tests
* install a MySQL / MariaDB database with user root without password
* create database "testj"
* install engine [BLACKHOLE](https://mariadb.com/kb/en/mariadb/blackhole/) using command "INSTALL SONAME 'ha_blackhole'" (This engine don't save data, permitting to execute INSERT queries with stable time result)
* restart database to activate the BLACKHOLE engine
* install a JRE

```script
mvn clean install
java -Duser.country=US -Duser.language=en -jar target/benchmarks.jar > result.txt &
```
(-Duser.country=US -Duser.language=en permit to avoid confusion with comma that can be use as thousand separator, and as decimal according to default java country)

## Understand results 

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
| BenchmarkBatchInsertWithPrepare*|executing 1000 inserts using prepareStatement with "prepare" on server. (option useServerPrepStmts=true)|
| BenchmarkBatchInsertWithoutPrepare*|executing 1000 inserts. (option useServerPrepStmts=false)|
| BenchmarkBatchInsertRewrite*|executing 1000 inserts. (option rewriteBatchedStatements=true)|
| BenchmarkCallableStatementFunction|execute CallableStatement with query "{? = CALL testFunctionCall(?,?,?)}". Function created by "CREATE FUNCTION IF NOT EXISTS testFunctionCall(a float, b bigint, c int) RETURNS INT NO SQL \nBEGIN \nRETURN a; \nEND"|
| BenchmarkCallableStatementWithInParameter|execute CallableStatement with query "{call withResultSet(?)}". Procedure created with "CREATE PROCEDURE IF NOT EXISTS withResultSet(a int) begin select a; end"|
| BenchmarkCallableStatementWithOutParameter|execute CallableStatement with query "{call inOutParam(?)}". Procedure created with "CREATE PROCEDURE IF NOT EXISTS inoutParam(INOUT p1 INT) begin set p1 = p1 + 1; end"|

'* The goal is here to test the driver performance, not database, so INSERT's queries are send to a [BLACKHOLE](https://mariadb.com/kb/en/mariadb/blackhole/) engine (data are not stored). This permit to have more stable results.



Results are in file "result.txt".
Complete results are the end of the file. Example of results 


# Example of results

execution run on a droplet on digitalocean.com using this parameters:
- CentOS 7.2 64bits
- 1GB memory
- 1 CPU
using default mariadb 10.1 configuration file
