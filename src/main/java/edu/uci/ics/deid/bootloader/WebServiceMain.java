package edu.uci.ics.deid.bootloader;

import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;

@EnableAutoConfiguration
@SpringBootApplication(scanBasePackages = { "edu.uci.ics.deid.controller", "edu.uci.ics.deid.repository" })
public class WebServiceMain {

	public static void main(String[] args) {

		ArgumentParser parser = ArgumentParsers.newFor("Main").build().defaultHelp(true)
				.description("Start Service for DeID - Web Service");
		parser.addArgument("-s", "--service").choices("web_service").setDefault("web_service")
				.help("Specify service to start");
		parser.addArgument("-f", "--file").required(true).help("Specify the service discovery file to read");

		// // Tell server to look for webs_service.properties or
		// // web_service.yml
		System.setProperty("spring.config.name", "web_service");

		MDC.put("logFileName", "web_service");

		SpringApplication.run(WebServiceMain.class, args);
	}
}