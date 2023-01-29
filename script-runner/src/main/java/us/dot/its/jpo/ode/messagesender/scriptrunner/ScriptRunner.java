package us.dot.its.jpo.ode.messagesender.scriptrunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class ScriptRunner {

    private final static Logger logger = LoggerFactory.getLogger(ScriptRunner.class);

    private final static Pattern linePattern = Pattern.compile("^(?<messageType>BSM|SPAT|MAP),(?<time>\\d+),(?<message>.+)$");
    
    @Autowired
    ThreadPoolTaskScheduler scheduler;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 
     * @param scriptFile - File containing the script
     */
    public void scheduleScript(File scriptFile) throws FileNotFoundException {
        try (var scanner = new Scanner(scriptFile)) {
            scanScript(scanner);
        }
    }

    /**
     * Schedule each item in a script to be run
     * @param script - Srting containing the entire script
     */
    public void scheduleScript(String script) {
        try (var scanner = new Scanner(script)) {
            scanScript(scanner);
        } 
    }

    private void scanScript(Scanner scanner) {
        long startTime = Instant.now().toEpochMilli();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher m = linePattern.matcher(line);
            if (!m.find()) {
                logger.warn("Skipping invalid line: \n{}", line);
            } 
            try {
                String messageType = m.group("messageType");
                long timeOffset = Long.parseLong(m.group("time"));
                String message = m.group("message");
                scheduleMessage(startTime, messageType, timeOffset, message);
            } catch (Exception e) {
                logger.error("Exception {}", e);
            }
        }
    }

    private void scheduleMessage(long startTime, String messageType, long timeOffset, String message) 
    {
        final long sendTime = startTime + timeOffset;
        final Instant sendInstant = Instant.ofEpochMilli(sendTime);
        var job = new SendMessageJob();
        job.setKafkaTemplate(kafkaTemplate);
        job.setMessageType(messageType);
        job.setSendTime(sendTime);
        job.setMessage(message);
        scheduler.schedule(job, sendInstant);
        logger.info("Scheduled {} job at {}", messageType, sendTime);
    }

    
}
