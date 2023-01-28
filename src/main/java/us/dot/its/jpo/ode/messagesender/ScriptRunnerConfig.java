package us.dot.its.jpo.ode.messagesender;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ScriptRunnerConfig  {


    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(3);
        scheduler.setThreadNamePrefix("ScriptPool");
        return scheduler;
    }
    
}
