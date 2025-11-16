package nl.schoepping.exiffile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadExif {
    private String exiftool = "";
    private String mediaFile = "";
    private EXIFInfo exifInfo = null;
    private ReadConfigYaml configYaml = null;


    public static class EXIFInfo {
        private LocalDateTime CreationDate = null;
        private Double Latitude = 0.0;
        private Double Longitude = 0.0;

        public LocalDateTime getCreationDate() { return CreationDate; }
        public void setCreationDate(LocalDateTime creationDate) { CreationDate = creationDate; }
        public Double getLatitude() { return Latitude; }
        public void setLatitude(Double latitude) { Latitude = latitude; }
        public Double getLongitude() { return Longitude; }
        public void setLongitude(Double longitude) { Longitude = longitude; }
    }

    public ReadExif(ReadConfigYaml configYaml, String filePath) throws IOException, ParseException {
        this.mediaFile = filePath;
        this.configYaml = configYaml;
        if (System.getProperty("os.name").contains("Windows")) {
            exiftool = "exiftool.bat";
        } else {
            exiftool = "exiftool";
        }
        this.exifInfo = this.getEXIFInfo();
    }

    private String getSpaceReplacedFileName() {
        return replaceSpaces(this.mediaFile);
    }

    private String replaceSpaces(String fileName) {
        String result = fileName;
        if (System.getProperty("os.name").contains("Windows")) {
            result = "\"" +  fileName + "\"";
        }
        return result;
    }

    private EXIFInfo getEXIFInfo() throws IOException {
        EXIFInfo result = new EXIFInfo();
        Map<String, String> map = new HashMap<>();
        BufferedReader reader;

        // create list of options for exiftool to be executed
        ArrayList<String> items = new ArrayList<>();
        items.add(exiftool);
        items.add("-s1");
        items.add("-api");
        items.add("largefilesupport=1");
        items.add("-c");
        items.add("%.6f");
        items.add("-FileType");
        for (String tag : configYaml.getTags()) {
            items.add("-" + tag);
        }
        items.add(getSpaceReplacedFileName());

        String[] cmdString = new String[items.size()];
        items.toArray(cmdString);
        Process process = Runtime.getRuntime().exec(cmdString);
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        while (line != null) {
            String regexEXIF = "^(\\S+)(\\s*): (.+)$";
            Pattern pattern = Pattern.compile(regexEXIF);
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                map.put(matcher.group(1),matcher.group(3));
            }
            line = reader.readLine();
        }
        reader.close();

        // Read creationdate
        String dateString = null;
        String timeZone = "+00:00";
        String filetype = map.get("FileType");
//        ReadConfigYaml.FileType fileType = configYaml.getFileType(map.get("FileType"));
        ReadConfigYaml.FileType fileType = configYaml.getFileType(filetype);
        dateString = map.get(fileType.getDateTime());
        boolean includeTimeZone = false;
        if (fileType.getTimeZone() != null) {
            if (fileType.getTimeZone().equalsIgnoreCase(fileType.getDateTime())) {
                includeTimeZone = true;
            }
            if (map.get(fileType.getTimeZone()) != null) {
                timeZone = map.get(fileType.getTimeZone());
            };
        }
        // if dateString is still empty then try another tag
        if (dateString == null) {
            dateString = map.get("FileModifyDate");
        }

        if (dateString != null) {
            LocalDateTime date;
            if (includeTimeZone) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ssXXXXX");
                date = LocalDateTime.parse(dateString, formatter);
            }
            else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
                date = LocalDateTime.parse(dateString, formatter);
            }
            String regexTimeZone = "([+-])(\\d{2}):(\\d{2})$";
            Pattern pattern = Pattern.compile(regexTimeZone);
            Matcher matcher = pattern.matcher(timeZone);
            int hours = 0;
            int minutes = 0;
            if (matcher.find()) {
                String sign = matcher.group(1);
                hours = Integer.parseInt(matcher.group(2));
                if (sign.equals("-")) {
                    hours = -1 * hours;
                }
                minutes = Integer.parseInt(matcher.group(3));
            }
            date = date.plusHours(hours);
            result.setCreationDate(date.plusMinutes(minutes));
        }

        // read GPS data

        String latitudeString = map.get(fileType.getGPSLatitude());
        String longitudeString = map.get(fileType.getGPSLongitude());
        if ((latitudeString != null) && (longitudeString != null)) {
            double latitude;
            double longitude;
            String regexGPS = "^([\\d.]+) ([NESW])$";
            Pattern pattern = Pattern.compile(regexGPS);
            Matcher matcher = pattern.matcher(latitudeString);
            if (matcher.matches()) {
                latitude = Double.parseDouble(matcher.group(1));
                if (matcher.group(2).equals("S")) {
                    latitude = latitude * -1;
                }
                result.setLatitude(latitude);
            }
            matcher = pattern.matcher(longitudeString);
            if (matcher.matches()) {
                longitude = Double.parseDouble(matcher.group(1));
                if (matcher.group(2).equals("S")) {
                    longitude = longitude * -1;
                }
                result.setLongitude(longitude);
            }
        }
        return result;

    }

    public String getTag(String tag) throws IOException {
        String result = "";
        BufferedReader reader;
        String line;
        String[] cmdString = new String[] { exiftool,  "-charset", "IPTC=UTF8", "-s3", "-" + tag, getSpaceReplacedFileName() };
        Process process = Runtime.getRuntime().exec(cmdString);
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        line = reader.readLine();
        if (line != null) {
            result = line;
        }
        reader.close();
        return result;
    }

    public LocalDateTime getCreateDateTime() { return this.exifInfo.getCreationDate(); }
    public Double getGPSLatitude() { return this.exifInfo.getLatitude(); }
    public Double getGPSLongitude() { return this.exifInfo.getLongitude(); }
    public String getCreateDateTimeString() throws Exception {
        if (this.getCreateDateTime() == null) {
            throw new Exception("No creation date is found");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return this.getCreateDateTime().format(formatter);
    }

    public String getCreateDateString() throws Exception {
        if (this.getCreateDateTime() == null) {
            throw new Exception("No creation date is found");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return this.getCreateDateTime().format(formatter);
    }

}
