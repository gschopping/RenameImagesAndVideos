package nl.schoepping.renameimages;

import nl.schoepping.exiffile.*;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.time.LocalDateTime;


public class RenameImages {

    private static final Logger log = LogManager.getLogger(RenameImages.class);

    static void main(String[] args) {
        // handle arguments
        Options options = new Options();
        options.addOption("c", "config", true, "configuration yaml file");
        options.addOption("t", "timeline", true, "timeline yaml file");
        options.addOption("d", "directory", true, "start directory");
        options.addOption("l", "logconfig", true, "configuration of the log file");

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            String configFile = cmd.getOptionValue("config");
            String timelineFile = cmd.getOptionValue("timeline");
            String startDirectory = cmd.getOptionValue("directory");
            String logConfigFile = cmd.getOptionValue("logconfig");
            ReadConfigYaml readConfigYaml = new ReadConfigYaml(configFile);

            System.out.println("Home directory: " + System.getProperty("user.dir"));
            System.out.println("Log4j.properties: " + System.getenv("log4j_properties"));
            System.out.println("Max Aliases for collections: " + System.getenv("MAX_MAX_ALIASES_FOR_COLLECTIONS"));
            System.out.println("Configuration file: " + configFile);
            System.out.println("Timeline file: " + timelineFile);
            System.out.println("Start directory: " + startDirectory);
            System.out.println("Log Config file: " + logConfigFile);

            System.out.println("regexMedia: " + readConfigYaml.getRegexMedia(true));
            System.out.println("results: " + readConfigYaml.getPathForResults());
            System.out.println("Tags: " + readConfigYaml.getTags());
            ReadConfigYaml.FileType filetype = readConfigYaml.getFileType("JPEG");
            if (filetype != null) {
                System.out.println("FileType: " + filetype.getFileType());
                System.out.println("DateTime: " + filetype.getDateTime());
                System.out.println("TimeZone: " + filetype.getTimeZone());
                System.out.println("isWritable: " + filetype.getIsWritable());
                System.out.println("isPhotoFormat: " + filetype.getIsPhotoFormat());
            }
            else {
                System.out.println("Filetype not found");
            }

            ReadTimeLineYaml readTimeLine = new ReadTimeLineYaml(timelineFile);
            if (readTimeLine.getEnabled()) {
                System.out.println("timeline is enabled");
                ReadTimeLineYaml.TimeLine timeline = readTimeLine.getTimeLine(LocalDateTime.of(2019, 8, 11, 18, 36, 31));
                System.out.println("StartDate: " + timeline.getStartDate());
                System.out.println("EndDate: " + timeline.getEndDate());
                System.out.println("Title: " + timeline.getTitle());
                System.out.println("Creator: " + timeline.getAuthor());
                System.out.println("Website: " + timeline.getWebsite());
                System.out.println("Copyright: " + timeline.getCopyright());
                System.out.println("Countrycode: " + timeline.getCountryCode());
                System.out.println("Country: " + timeline.getCountry());
                System.out.println("Province: " + timeline.getProvince());
                System.out.println("City: " + timeline.getCity());
                System.out.println("Location: " + timeline.getLocation());
                System.out.println("Description: " + timeline.getDescription());
                System.out.println("Override: " + timeline.getOverride());
                System.out.println("Keys: " + timeline.getKeys());

                ReadFiles readFiles = new ReadFiles(readConfigYaml, startDirectory);
                for (File file : readFiles.getFilesFromDirectory()) {
                    ReadExif readExif = new ReadExif(readConfigYaml, file.getPath());
                    System.out.println("==== " + file.getName() + " ====");
                    System.out.println("CreateDateTime: " + readExif.getCreateDateTime());
                    timeline = readTimeLine.getTimeLine(readExif.getCreateDateTime());
                    if (timeline != null) {
                        System.out.println("Description: " + timeline.getDescription());
                    }
                }
                log.info("Configuration file:\t{}", configFile);
                log.info("Timeline file:\t{}", timelineFile);
                log.info("Start directory:\t{}", startDirectory);
                log.info("Home directory:\t{}", System.getProperty("user.dir"));
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            System.exit(1);
        }

    }
}
