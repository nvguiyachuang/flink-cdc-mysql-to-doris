package org.example.demo;

import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.lang.reflect.Field;

public class CdcDemo {
    public static void main(String[] args) throws Exception {
        MySqlSource<String> mySqlSource = MySqlSource
                .<String>builder()
                .hostname("localhost")
                .port(3306)
                .databaseList("test") // set captured database
                .tableList("test.test_cdc") // set captured table
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
                .print()
                .name("print demo")
                .uid("print-demo")
                .setParallelism(1); // use parallelism 1 for sink to keep message ordering

        env.execute("Print MySQL Snapshot + Binlog");
    }
}
