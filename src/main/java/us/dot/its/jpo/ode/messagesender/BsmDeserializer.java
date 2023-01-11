// package us.dot.its.jpo.ode.messagesender;

// import java.io.IOException;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import com.fasterxml.jackson.core.JacksonException;
// import com.fasterxml.jackson.core.JsonGenerator;
// import com.fasterxml.jackson.core.JsonParser;
// import com.fasterxml.jackson.databind.DeserializationContext;
// import com.fasterxml.jackson.databind.DeserializationFeature;
// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.SerializerProvider;
// import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
// import com.fasterxml.jackson.databind.node.ObjectNode;
// import com.fasterxml.jackson.databind.ser.std.StdSerializer;

// import us.dot.its.jpo.ode.model.OdeBsmData;
// import us.dot.its.jpo.ode.model.OdeBsmMetadata;
// import us.dot.its.jpo.ode.model.OdeBsmPayload;
// import us.dot.its.jpo.ode.util.JsonUtils;

// public class BsmDeserializer extends StdDeserializer<OdeBsmData> {

//     private static final Logger logger = LoggerFactory.getLogger(TestController.class);
//     private static final ObjectMapper om = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

//     public BsmDeserializer() {
//         this(null);
//     }

//     public BsmDeserializer(Class<OdeBsmData> t) {
//         super(t);
//     }

//     @Override
//     public OdeBsmData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
//         try {
//             JsonNode node = p.getCodec().readTree(p);

            

//             // Deserialize the metadata
//             JsonNode metadataNode = node.get("metadata");
//             String metadataString = metadataNode.toString();
//             var metadata = (OdeBsmMetadata) om.readValue(metadataString, OdeBsmMetadata.class);

//             // Deserialize the payload
//             ObjectNode payloadNode = (ObjectNode)node.get("payload");
//             ObjectNode dataNode = (ObjectNode)payloadNode.get("data");

//             // Remove part II extensions which aren't parsed correctly
//             if (dataNode.has("partII")) {
//                 dataNode.remove("partII");
//             }
            
            
//             String payloadString = payloadNode.toString();
//             var payload = (OdeBsmPayload) om.readValue(payloadString, OdeBsmPayload.class);

//             return new OdeBsmData(metadata, payload);
//         } catch (Exception e) {
//             logger.error("Error deserializing OdeBsmData", e);
//             return null;
//         }
//     }

// }
