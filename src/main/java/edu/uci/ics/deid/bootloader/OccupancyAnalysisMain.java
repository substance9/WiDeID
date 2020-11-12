package edu.uci.ics.deid.bootloader;

import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;

@EnableAutoConfiguration
@SpringBootApplication(scanBasePackages = { "edu.uci.ics.deid.service.occupancy", "edu.uci.ics.deid.repository" })
public class OccupancyAnalysisMain {

	public static void main(String[] args) {

		ArgumentParser parser = ArgumentParsers.newFor("Main").build().defaultHelp(true)
				.description("Start Service for DeID - Occupancy");

		parser.addArgument("-s", "--service").choices("occupancy").setDefault("occupancy")
				.help("Specify service to start");
		parser.addArgument("-f", "--file").required(true).help("Specify the service discovery file to read");

		// // Tell server to look for device_filter.properties or
		// // device_filter.yml
		System.setProperty("spring.config.name", "occupancy");

		MDC.put("logFileName", "occupancy");

		SpringApplication.run(OccupancyAnalysisMain.class, args);
	}
}