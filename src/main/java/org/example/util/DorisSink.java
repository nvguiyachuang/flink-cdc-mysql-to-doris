package org.example.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * ALTER TABLE `test`.`test_cdc`
 * ADD COLUMN `score` varchar(255) NULL AFTER `gender`
 */
public class DorisSink extends RichSinkFunction<String> implements Serializable {

    private Connection conn = null;

    private ObjectMapper mapper;

    private final List<String> operatorList = new ArrayList<>(Arrays.asList("r", "c", "u"));

    DorisStreamLoader dorisStreamLoader;

    @Override
    public void open(Configuration parameters) throws Exception {
        mapper = new ObjectMapper();
        dorisStreamLoader = new DorisStreamLoader();

        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection("jdbc:mysql://node:9030/test", "root", "");
    }

    @Override
//    @SuppressWarnings("unchecked")
    public void invoke(String value, Context context) throws Exception {
        JsonNode jsonNode = mapper.readTree(value);
        System.out.println();

        JsonNode historyRecord = jsonNode.get("historyRecord");
        if (null != historyRecord) {
            // deal with ddl change
            String ddl = mapper.readTree(historyRecord.asText()).get("ddl").asText();
            System.out.println(ddl);
            if (StringUtils.isNotBlank(ddl) && ddl.contains("ADD")) {
                String[] strings = ddl.split(" ");
                String dorisDdl = StringUtils.join(Arrays.copyOf(strings, 7), " ");
                System.out.println("dorisDdl:  " + dorisDdl);

                PreparedStatement ps = conn.prepareStatement(dorisDdl);
                ps.execute();
                CloseUtil.close(ps);
            }
        } else {
            // deal with data by stream load
            String op = jsonNode.get("op").asText();
            System.out.println(op);
            if (operatorList.contains(op)) {
//            System.out.println(jsonNode.get("before"));

                // here do not use .asText, return ""
                String after = jsonNode.get("after").toString();
                System.out.println("data: " + after);

                JsonNode source = jsonNode.get("source");
                String db = source.get("db").asText();
                System.out.println("db: " + db);
                String table = source.get("table").asText();
                System.out.println("ta: " + table);

                dorisStreamLoader.loadJson(after, db, table);
            }
        }
    }

    @Override
    public void close() {
        CloseUtil.close(conn);
    }
}
