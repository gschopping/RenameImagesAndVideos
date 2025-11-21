package nl.schoepping.exiffile;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;


import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;


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
    private void RenameFile(OpenStreetMapUtils.Address address, ReadTimeLineYaml.TimeLine timeline, ReadConfigYaml configYaml, String dateString, String directory, int counter, File file, boolean fromTimeline) throws Exception {
        char postfix=' ';
        String title;
        String country;
        String city;
        String location;
        String province;
        // write all tags
        this.logger.debug("RenameFiles: RenameFile {}", file.getPath());
        WriteExif writeEXIF = new WriteExif(file.getPath(), true);
        writeEXIF.setLogger(logger);
        this.logger.debug("RenameFiles: RenameFile writeEXIF.setAddress");
        if ((address != null) && address.getIsSet() && (timeline.getOverride() == false)) {
            writeEXIF.setAddress(address);
            title = address.getAddress();
            country = address.getCountry();
            city = address.getCity();
            location = address.getLocation();
            province = address.getProvince();
        }
        else {
            writeEXIF.setCountryCode(timeline.getCountryCode());
            writeEXIF.setCountry(timeline.getCountry());
            writeEXIF.setProvince(timeline.getProvince());
            writeEXIF.setCity(timeline.getCity());
            writeEXIF.setLocation(timeline.getLocation());
            title = timeline.getTitle();
            country = timeline.getCountry();
            city = timeline.getCity();
            location = timeline.getLocation();
            province = timeline.getProvince();
        }
        writeEXIF.setCopyright(timeline.getCopyright());
        writeEXIF.setAuthor(timeline.getAuthor());
        writeEXIF.setURL(timeline.getWebsite());
        writeEXIF.setTitle(title);
        if (fromTimeline) {
            writeEXIF.setDescription(timeline.getDescription());
            writeEXIF.setComment(timeline.getComment());
            writeEXIF.setSpecialInstructions(timeline.getInstructions());
            String[] keys = timeline.getKeys().split(",");
            writeEXIF.setKeys(keys);
        }
        boolean noError = false;
        // to avoid non ASCII characters in the filename
        title = getTitle(title, location, city, province, country);
        String newFileName;
        while (! noError) {
            if (postfix == ' ') {
                if (counter > 0) {
                    newFileName = String.format("%s-%04d %s.%s",
                            dateString,
                            counter,
                            title,
                            FilenameUtils.getExtension(file.getName()));
                }
                else {
                    newFileName = String.format("%s %s.%s",
                            dateString,
                            title,
                            FilenameUtils.getExtension(file.getName()));
                }
            }
            else {
                if (counter > 0) {
                    newFileName = String.format("%s%c-%04d %s.%s",
                            dateString,
                            postfix,
                            counter,
                            title,
                            FilenameUtils.getExtension(file.getName()));
                }
                else {
                    newFileName = String.format("%s%c %s.%s",
                            dateString,
                            postfix,
                            title,
                            FilenameUtils.getExtension(file.getName()));
                }
            }
            logger.info("{} =>\t{}\t({}, {}, {})", file.getName(), newFileName, country, city, location);
            try {
                writeEXIF.writeFile(directory + slash + "results" + slash + newFileName, false);
                noError = true;
            } catch (Exception e) {
                if (e.getMessage().matches("^(.* already exists)$")) {
                    if (postfix == ' ') {
                        postfix = 'a';
                    } else {
                        postfix += 1;
                    }
                }
                else {
                    throw new Exception(e.getMessage());
                }
            }
        }

    }

    public void RenameRootFiles() throws Exception {
        this.logger.debug("RenameRootFiles: ReadYaml {}", timeLineYaml.getFileName());
        this.logger.debug("RenameRootFiles: ReadFiles {}", startDirectory);
        if (timeLineYaml.getEnabled()) {
            ReadFiles readFiles = new ReadFiles(configYaml, this.startDirectory);
            for (File file : readFiles.getFilesFromDirectory()) {
                this.logger.debug("RenameRootFiles: ReadEXIF {}", file.getPath());
                ReadExif readEXIF = new ReadExif(configYaml, file.getPath());
// timeline is found, now you can set all information in mediafile
// create new name for mediafile
                this.logger.debug("RenameRootFiles: ReadEXIF");
                this.logger.debug("RenameRootFiles: ReadYaml.getTimeLine {}", readEXIF.getCreateDateTimeString());
                ReadTimeLineYaml.TimeLine timeline = timeLineYaml.getTimeLine(readEXIF.getCreateDateTime());
                if (timeline != null) {
                    String dateString = readEXIF.getCreateDateTimeString();
                    Double latitude = readEXIF.getGPSLatitude();
                    Double longitude = readEXIF.getGPSLongitude();
                    OpenStreetMapUtils.Address address = OpenStreetMapUtils.getInstance().getAddress(latitude, longitude);
                    try {
                        RenameFile(address, timeline, configYaml, dateString, this.startDirectory, 0, file, true);
                    } catch (Exception e) {
                        this.logger.info("RenameRootFiles {}", e.getMessage());
                    }
                } else {
                    this.logger.info(String.format("%s with %tF %tT has a creationdate before all timelines",
                            file.getName(), readEXIF.getCreateDateTime(), readEXIF.getCreateDateTime()));
                }
            }
        }
        this.logger.debug("RenameRootFiles: end");
    }

    public void RenameTimelapsFiles(boolean counting) throws Exception {
        this.logger.debug("RenameTimelapsFiles: ReadYaml {}", timeLineYaml.getFileName());
        // read timelaps subdirectories
        if (counting) {
            this.logger.debug("RenameTimelapsFiles: (counting=true) ReadFiles {}", startDirectory);
        } else {
            this.logger.debug("RenameTimelapsFiles: (counting=false) ReadFiles {}", startDirectory);
        }
        if (timeLineYaml.getEnabled()) {
            ReadFiles readFiles = new ReadFiles(configYaml, this.startDirectory);
            int counter = 1;
            String referenceDate = "";
            List<File> dirs;
            if (counting) {
                dirs = readFiles.getTimelapsDirectories();
            } else {
                dirs = readFiles.getGPSDirectories();
            }
            for (File dir : dirs) {
                this.logger.debug("RenameTimelapsFiles: readFiles.getTimelapsFiles {}", dir.getPath());
                List<File> timelapsFiles = readFiles.getTimelapsFiles(dir);
                if (!timelapsFiles.isEmpty()) {
                    // take the last file where GPS data is to be expected and fix the date
                    File file = timelapsFiles.get(timelapsFiles.size() - 1);
                    this.logger.debug("RenameTimelapsFiles: ReadEXIF {}", file.getPath());
                    ReadExif readEXIF = new ReadExif(configYaml, file.getPath());
                    this.logger.debug("RenameTimelapsFiles: ReadEXIF {}", readEXIF.getCreateDateTimeString());
                    // start counter for each new date, keep all filenames unique
                    if (referenceDate.isEmpty()) {
                        counter = 1;
                    } else {
                        if (!referenceDate.equals(readEXIF.getCreateDateString())) {
                            counter = 1;
                        }
                    }
                    referenceDate = readEXIF.getCreateDateString();
                    this.logger.debug("RenameTimelapsFiles: ReadEXIF.getCreateDateString {}", referenceDate);
                    // read from Yaml file in order to retrieve some default information
                    ReadTimeLineYaml.TimeLine timeline = timeLineYaml.getTimeLine(readEXIF.getCreateDateTime());
                    if (timeline != null) {
                        this.logger.debug("RenameTimelapsFiles: ReadEXIF.getGPSLatitude, getGPSLongitude");
                        // retrieve if possible GPS coordinates
                        Double latitude = readEXIF.getGPSLatitude();
                        Double longitude = readEXIF.getGPSLongitude();
                        OpenStreetMapUtils.Address address = OpenStreetMapUtils.getInstance().getAddress(latitude, longitude);
                        this.logger.debug(String.format("RenameTimelapsFiles: OpenStreetMapUtils latitude: %.6f, longitude: %.6f ", latitude, longitude));
                        for (File timelapsFile : timelapsFiles) {
                            if (!counting) {
                                // not counting means no counter will be added instead the time will be used
                                readEXIF = new ReadExif(configYaml, timelapsFile.getPath());
                                referenceDate = readEXIF.getCreateDateTimeString();
                                counter = 0;
                            }
                            RenameFile(address, timeline, configYaml, referenceDate, dir.getPath(), counter, timelapsFile, false);
//                        RenameTimelapsFile(address, timeline, referenceDate, dir.getPath(), counter, timelapsFile);
                            counter++;
                        }
                    } else {
                        this.logger.info(String.format("%s with %tF %tT has a creationdate before all timelines",
                                file.getName(), readEXIF.getCreateDateTime(), readEXIF.getCreateDateTime()));
                    }
                } else {
                    this.logger.info("No files in {}", dir.getPath());
                }
            }
        }
        this.logger.debug("RenameTimelapsFiles: end");
    }

}
