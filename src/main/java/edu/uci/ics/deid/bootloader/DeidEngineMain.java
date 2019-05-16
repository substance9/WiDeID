package edu.uci.ics.deid.bootloader;

import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;

@EnableAutoConfiguration
@SpringBootApplication(scanBasePackages = { "edu.uci.ics.deid.service.deidengine", "edu.uci.ics.deid.repository" })
public class DeidEngineMain {

	public static void main(String[] args) {

		ArgumentParser parser = ArgumentParsers.newFor("Main").build().defaultHelp(true)
				.description("Start Service for DeID - DeID Engine");

		parser.addArgument("-s", "--service").choices("deid_engine").setDefault("deid_engine")
				.help("Specify service to start");
		parser.addArgument("-f", "--file").required(true).help("Specify the service discovery file to read");

		// // Tell server to look for device_filter.properties or
		// // device_filter.yml
		System.setProperty("spring.config.name", "deid_engine");

		MDC.put("logFileName", "deid_engine");

		SpringApplication.run(DeidEngineMain.class, args);
	}
}