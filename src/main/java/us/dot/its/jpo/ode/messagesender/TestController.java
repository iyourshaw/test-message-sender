package us.dot.its.jpo.ode.messagesender;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.ode.model.OdeBsmData;
import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmPayload;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;
import us.dot.its.jpo.ode.model.OdeMsgPayload;
import us.dot.its.jpo.ode.model.OdeSpatData;
import us.dot.its.jpo.ode.model.OdeSpatMetadata;
import us.dot.its.jpo.ode.model.OdeSpatPayload;
import us.dot.its.jpo.ode.plugin.j2735.J2735Bsm;
import us.dot.its.jpo.ode.plugin.j2735.J2735BsmCoreData;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;

@RestController
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    
    ObjectMapper objectMapper = new ObjectMapper();

    
    
    
    @PostMapping(value = "/spat", consumes = "application/json", produces = "*/*")
    public @ResponseBody ResponseEntity<String> spat(@RequestBody String json) {

        var om = new ObjectMapper();
        om.setSerializationInclusion(Include.NON_ABSENT);
        JsonNode node;
        try {
            node = om.readTree(json);

            // Deserialize the metadata
            JsonNode metadataNode = node.get("metadata");
            String metadataString = metadataNode.toString();
            OdeSpatMetadata metadataObject = om.readValue(metadataString, OdeSpatMetadata.class);

            // Deserialize the payload
            JsonNode payloadNode = node.get("payload");
            String payloadString = payloadNode.toString();
            OdeSpatPayload mapPayload = om.readValue(payloadString, OdeSpatPayload.class);

            OdeSpatData spatData = new OdeSpatData(metadataObject, mapPayload);

            // String serialized = om.writeValueAsString(spatData);

            String serialized = spatData.toXml();

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_XML).body(serialized);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN)
                    .body(ExceptionUtils.getStackTrace(e));
        }

    }

    @Autowired
    KafkaTemplate<String, String> template;

    @PostMapping(value = "/kafka/{topic}", consumes = "*/*", produces = "*/*")
    public @ResponseBody ResponseEntity<String> kafka(@RequestBody String message, @PathVariable String topic) {
        try {
            var result = template.send(topic, message);
            SendResult<String, String> sendResult = result.completable().join();
            String strResult = sendResult.toString();
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(strResult);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN)
                    .body(ExceptionUtils.getStackTrace(e));
        }

    }

    @PostMapping(value = "/createBsmMessages", consumes = "application/json", produces = "*/*")
    public @ResponseBody ResponseEntity<String> createBsms(@RequestBody BsmPostData bsmPostData) {
        final String ODE_JSON_TOPIC = "topic.OdeBsmJson";
        logger.info("Received BSMs: {}", bsmPostData);
        var results = new ArrayList<CompletableFuture<SendResult<String, String>>>();
        try {
            var bsmCoords = bsmPostData.bsmList;
            var bsmTemplate = bsmPostData.bsmTemplate;
            var bsmDataList = createBsmList(bsmCoords, bsmTemplate);
            
            for (var bsmData : bsmDataList) {
                var result = template.send(ODE_JSON_TOPIC, bsmData.toJson(false));
                results.add(result.completable());
            }

            var strResults = new StringBuilder();
            // Wait for all the futures to return to see the result
            // Don't do this if this method needs to be fast
            for (CompletableFuture<SendResult<String, String>> result : results) {
                SendResult<String, String> sendResult = result.join();
                logger.info("Sent: {}", sendResult);
                strResults.append(String.format("%s%n", sendResult.toString()));
            }
            
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(strResults.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN)
                    .body(ExceptionUtils.getStackTrace(e));
        }
    }

    public List<OdeBsmData> createBsmList(TimestampedCoordinateList bsmCoords, OdeBsmData bsmTemplate) {
        var bsmDataList = new ArrayList<OdeBsmData>();
        int msgCount = 0;
        for (TimestampedCoordinate tsCoord : bsmCoords) {
            bsmDataList.add(createBsm(tsCoord, msgCount, bsmTemplate));
            msgCount = (msgCount + 1) % 128;
        }
        return bsmDataList;
    }

    public OdeBsmData createBsm(TimestampedCoordinate bsmCoord, int msgCnt, OdeBsmData bsmTemplate) {
        var metadata = new OdeBsmMetadata();
        if (bsmTemplate != null) {
            metadata = (OdeBsmMetadata)bsmTemplate.getMetadata();
        } else {
            metadata.setOriginIp("127.0.0.1");
        }
        
        
        
        var coreData = new J2735BsmCoreData();
        if (bsmTemplate != null) {
            var templatePayload = (OdeBsmPayload)bsmTemplate.getPayload();
            coreData = templatePayload.getBsm().getCoreData();
        }
        coreData.setMsgCnt(msgCnt);
        
        var instant = Instant.ofEpochMilli(bsmCoord.getTimestamp());
        var ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        var secondOfMinute = ldt.getSecond();
        var milliOfSecond = ldt.getNano() / (int)1e6;
        var milliOfMinute = secondOfMinute * 1000 + milliOfSecond;
        coreData.setSecMark(milliOfMinute);

        var position = new OdePosition3D();
        var mc = new MathContext(11, RoundingMode.DOWN);
        position.setLongitude(new BigDecimal(bsmCoord.getCoords()[0]).round(mc));
        position.setLatitude(new BigDecimal(bsmCoord.getCoords()[1]).round(mc));
        position.setElevation(BigDecimal.ZERO);
        coreData.setPosition(position);
        var bsm = new J2735Bsm();
        bsm.setCoreData(coreData);
        var payload = new OdeBsmPayload();
        payload.setBsm(bsm);

        return new OdeBsmData((OdeMsgMetadata) metadata, (OdeMsgPayload) payload);
    }
}
