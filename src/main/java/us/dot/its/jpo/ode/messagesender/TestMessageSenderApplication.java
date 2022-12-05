package us.dot.its.jpo.ode.messagesender;

import java.util.Arrays;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class TestMessageSenderApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(TestMessageSenderApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(TestMessageSenderApplication.class, args);
	}

	// @Bean
	// public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
	// 	return args -> {

	// 		System.out.println("Let's inspect the beans provided by Spring Boot:");

	// 		String[] beanNames = ctx.getBeanDefinitionNames();
	// 		Arrays.sort(beanNames);
	// 		for (String beanName : beanNames) {
	// 			System.out.println(beanName);
	// 		}

	// 	};
	// }

	// @Bean
	// public NewTopic topic() {
	// 	return TopicBuilder.name("topic1")
	// 		.partitions(1)
	// 		.replicas(1)
	// 		.build();
	// }

	
	

}
