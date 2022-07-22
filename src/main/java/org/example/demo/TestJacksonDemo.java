package org.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.util.HashMap;

public class TestJacksonDemo {
    ObjectMapper mapper = new ObjectMapper();

    String jsonData = "{\"id\":556393583,\"number\":\"123344\",\"price\":\"23.5\",\"skuname\":\"test\",\"skudesc\":\"zhangfeng_test,test\"}";

    @Test
    public void testReadValue() throws JsonProcessingException {
        HashMap<String, Object> map = mapper.readValue(jsonData, new TypeReference<HashMap<String, Object>>() {
        });

        System.out.println(map.get("id"));
        System.out.println(map.get("number"));
        System.out.println(map.get("skuname"));
    }

    @Test
    public void testReadTree() throws JsonProcessingException {
        // can be read as generic JsonNode, if it can be Object or Array; or,
// if known to be Object, as ObjectNode, if array, ArrayNode etc:
        JsonNode root = mapper.readTree(jsonData);
        String id = root.get("name").asText();
        System.out.println(id);
        int number = root.get("age").asInt();
        System.out.println(number);

// can modify as well: this adds child Object as property 'other', set property 'type'
//        root.with("other").put("type", "student");
//        String json = mapper.writeValueAsString(root);

// with above, we end up with something like as 'json' String:
// {
//   "name" : "Bob", "age" : 13,
//   "other" : {
//      "type" : "student"
//   }
// }
    }

//    @Test
//    public void test3(){
//        ObjectMapper mapper = ...;
//// First: write simple JSON output
//        File jsonFile = new File("test.json");
//// note: method added in Jackson 2.11 (earlier would need to use
//// mapper.getFactory().createGenerator(...)
//        JsonGenerator g = f.createGenerator(jsonFile, JsonEncoding.UTF8);
//// write JSON: { "message" : "Hello world!" }
//        g.writeStartObject();
//        g.writeStringField("message", "Hello world!");
//        g.writeEndObject();
//        g.close();
//
//// Second: read file back
//        try (JsonParser p = mapper.createParser(jsonFile)) {
//            JsonToken t = p.nextToken(); // Should be JsonToken.START_OBJECT
//            t = p.nextToken(); // JsonToken.FIELD_NAME
//            if ((t != JsonToken.FIELD_NAME) || !"message".equals(p.getCurrentName())) {
//                // handle error
//            }
//            t = p.nextToken();
//            if (t != JsonToken.VALUE_STRING) {
//                // similarly
//            }
//            String msg = p.getText();
//            System.out.printf("My message to you is: %s!\n", msg);
//        }
//    }


}
