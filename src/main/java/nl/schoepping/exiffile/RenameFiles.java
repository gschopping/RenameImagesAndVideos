package nl.schoepping.exiffile;

import org.apache.logging.log4j.Logger;

import java.nio.file.FileSystems;


public class RenameFiles {
    private final Logger logger;
    private final ReadTimeLineYaml timeLineYaml;
    private final ReadConfigYaml configYaml;
    private final String startDirectory;
    private final String slash;

    public RenameFiles(Logger logger, ReadTimeLineYaml timeLineYaml, ReadConfigYaml configYaml, String startDirectory) {
        this.logger = logger;
        this.timeLineYaml = timeLineYaml;
        this.configYaml = configYaml;
        this.startDirectory = startDirectory;
        this.slash = FileSystems.getDefault().getSeparator();
    }

    private String getTitle(String title, String location, String city, String province, String country) {
        String result = country;
        String regexASCII;
        if (this.timeLineYaml.getAvoidNonAscii()) {
            regexASCII = "^([a-zA-Z0-9_\\-()@#!= \\[\\]{};,.<>]+)$";
        }
        else {
            regexASCII = "^(.+)$";
        }
        if (title.matches(regexASCII)) {
            result = title;
        }
        else if (location.matches(regexASCII)) {
            result = location;
        }
        else if (city.matches(regexASCII)) {
            result = city;
        }
        else if (province.matches(regexASCII)) {
            result = province;
        }
        return result;
    }

}
