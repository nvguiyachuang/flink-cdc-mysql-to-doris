package org.example;

import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.example.util.DorisSink;

import java.lang.reflect.Field;

/**
 * {
 * "before":null,
 * "after":{"id":29,"name":"d","gender":null},
 * "source":
 * {"version":"1.5.4.Final","connector":"mysql","name":"mysql_binlog_source","ts_ms":0,"snapshot":"false",
 * "db":"test","sequence":null,"table":"test_cdc","server_id":0,"gtid":null,"file":"","pos":0,"row":0,
 * "thread":null,"query":null},
 * "op":"r",
 * "ts_ms":1651470068042,
 * "transaction":null
 * }
 */
public class MysqlToDoris {
    public static void main(String[] args) throws Exception {
        MySqlSource<String> mySqlSource = MySqlSource
                .<String>builder()
                .hostname("localhost")
                .port(3306)
                .databaseList("test") // set captured database
                .tableList("test.test_cdc", "test.test_cdc2") // set captured table
                .username("root")
                .password("root")
                .deserializer(new JsonDebeziumDeserializationSchema())
                .includeSchemaChanges(true)
                .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // http port
        Field field = StreamExecutionEnvironment.class.getDeclaredField("configuration");
        field.setAccessible(true);
        Configuration conf = (Configuration) field.get(env);
        conf.setString("rest.bind-port", "8081");

        // enable checkpoint
        env.enableCheckpointing(3000);

        env
                .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Cdc Source")
                .uid("mysql-cdc-source")
                // set 4 parallel source tasks
                .setParallelism(4)
                .addSink(new DorisSink())
                .name("doris-sink")
                .uid("doris-sink")
                .setParallelism(1); // use parallelism 1 for sink to keep message ordering

        env.execute("Print MySQL Snapshot + Binlog");
    }
}
