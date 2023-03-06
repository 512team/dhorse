package org.dhorse.rest;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 引导服务入口。
 * 
 * @author Dahai
 *
 */
@ServletComponentScan
@EnableScheduling
@PropertySource(value = {"classpath:dhorse.yml",
		"classpath:application-private.yml"})
@SpringBootApplication(scanBasePackages = {
		"org.dhorse.infrastructure",
		"org.dhorse.application",
		"org.dhorse.web",
		"org.dhorse.rest"})
public class DHorseBootstrap {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(DHorseBootstrap.class);
		springApplication.setDefaultProperties(Collections.singletonMap("server.port", "8100"));
		springApplication.run(args);
	}
}