/**
 * 
 */
package com.synectiks.schemas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Rajesh
 */
@SpringBootApplication
//@EnableJpaRepositories
@ComponentScan(basePackages = "com.synectiks")
//@EnableAutoConfiguration
public class SynectiksSchemasApplication {

	private static final Logger logger = LoggerFactory
			.getLogger(SynectiksSchemasApplication.class);

	private static ConfigurableApplicationContext ctx;

	public static void main(String[] args) {
		ctx = SpringApplication.run(SynectiksSchemasApplication.class, args);
		for (String bean : ctx.getBeanDefinitionNames()) {
			logger.info("Bean: " + bean);
		}
	}

}
