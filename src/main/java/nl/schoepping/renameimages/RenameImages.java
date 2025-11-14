package nl.schoepping.renameimages;

import nl.schoepping.exiffile.*;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class RenameImages {

    private static final Logger log = LogManager.getLogger("RenameImages");

    static void main(String[] args) {
        // handle arguments
        Options options = new Options();
        options.addOption("c", "config", true, "configuration yaml file");
        options.addOption("t", "timeline", true, "timeline yaml file");
        options.addOption("d", "directory", true, "start directory");
        options.addOption("l", "log", true, "log file");

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            String configFile = cmd.getOptionValue("config");
            String timelineFile = cmd.getOptionValue("timeline");
            String startDirectory = cmd.getOptionValue("directory");
            String logFile = cmd.getOptionValue("log");
            ReadTimeLineYaml readTimeLine = new ReadTimeLineYaml(timelineFile);
            ReadConfigYaml readConfigYaml = new ReadConfigYaml(configFile);

            if (readTimeLine.getEnabled()) {
                log.info("Configuration file:\t{}", configFile);
                log.info("Timeline file:\t{}", timelineFile);
                log.info("Start directory:\t{}", startDirectory);
                log.info("Home directory:\t{}", System.getProperty("user.dir"));

                System.out.println("regexMedia: "+ readConfigYaml.getRegexMedia(true));
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            System.exit(1);
        }

    }
}
