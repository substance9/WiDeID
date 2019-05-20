package edu.uci.ics.deid.bootloader;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {

	public static void main(String[] args) {
		ArgumentParser parser = ArgumentParsers.newFor("Main").build().defaultHelp(true)
				.description("Start Service for DeID");
		parser.addArgument("-s", "--service").choices("device_filter", "deid_engine", "web_service","dispatcher")
				.setDefault("device_filter").required(true).help("Specify service to start");
		parser.addArgument("-f", "--file").required(true).help("Specify the config file to read");
		Namespace ns = null;

		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

		String serverName = ns.get("service");

		if (serverName.equals("device_filter") || serverName.equals("dev_filter")) {
			DeviceFilterMain.main(args);
		} else if (serverName.equals("deid_engine")) {
			DeidEngineMain.main(args);
		} else if (serverName.equals("web_service")) {
			WebServiceMain.main(args);
		} else if (serverName.equals("dispatcher")) {
			 DispatcherMain.main(args);
		} else {
			System.out.println("Unknown server type: " + serverName);
			parser.printHelp();
		}
	}

}
