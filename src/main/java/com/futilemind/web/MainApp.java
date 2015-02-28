package com.futilemind.web;

import com.futilemind.web.config.AppConfig;
import org.apache.commons.cli.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.util.Arrays;

/**
 * This is the entry point for our application.
 * Sets up up the spring context and sets the mode.
 * Set up embedded jetty to work with AnnotationConfigWebApplicationContext
 * Relies on gradle application plugin so can be launched with ./gradlew run
 */
public class MainApp {
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);


    final private Options options;
    final private CommandLine command;
    final private HelpFormatter formatter;
    private String operationMode;
    private Integer portNumber;


    public MainApp(String[] args) throws ParseException {
        BasicParser parser = new BasicParser();
        options = new Options();
        formatter = new HelpFormatter();
        setupCli();
        command = parser.parse(options, args);
    }


    /**
     * Add menu options to the cli parser.
     */
    private void setupCli() {
        options.addOption("h", "help", false, "Show help menu");
        options.addOption("m", "mode", true, "Mode of operation. PROD or DEV");
        options.addOption("p", "port", true, "Http port number");
    }

    /**
     * Prints our help menu.
     */
    private void printHelpmenu() {
        formatter.printHelp("Web-Bootstrap", options);
    }

    //Parses the command line args and sets up the context and then runs.
    public void execute() throws Exception {

        if (command.hasOption("h")) {
            printHelpmenu();
            return;
        }
        operationMode = command.getOptionValue("m", "DEV");
        portNumber = Integer.valueOf(command.getOptionValue("p", "8080"));
        setupContext();
    }

    /**
     * Sets up a web application context and configures jetty to run.
     * @throws Exception
     */
    private void setupContext() throws Exception {
        final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();

        ConfigurableEnvironment configurableEnvironment = applicationContext.getEnvironment();
        configurableEnvironment.setActiveProfiles(operationMode);
        logger.info("Active Profile(s) >>> " + Arrays.toString(configurableEnvironment.getActiveProfiles()));

        applicationContext.register(AppConfig.class);

        final ServletHolder servletHolder = new ServletHolder(new DispatcherServlet(applicationContext));
        final ServletContextHandler context = new ServletContextHandler();
        
        context.setResourceBase(new ClassPathResource("webapp").getURI().toString());
        context.setErrorHandler(null); // use Spring exception handler(s)
        context.setContextPath("/");
        context.addServlet(servletHolder, "/");

        final Server server = new Server(portNumber);

        server.setHandler(context);

        server.start();
        server.join();
    }

    //Bootstraps our application start.
    public static void main(String[] args) throws Exception {

        try {
            MainApp app = new MainApp(args);
            app.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
