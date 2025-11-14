package nl.schoepping.exiffile;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadConfigYaml {

    private final ArrayList<FileType> fileTypes;

    public static class FileType {
        private String fileType = "";
        private String extension = "";
        private String dateTime = "";
        private String timeZone = "";
        private String gpsLatitude = "";
        private String gpsLongitude = "";
        private Boolean isWritable = true;
        private Boolean isPhotoFormat = false;

        public String getFileType() { return fileType; }
        public String getExtension() { return extension; }
        public String getDateTime() { return dateTime; }
        public String getTimeZone() { return timeZone; }
        public String getGPSLatitude() { return gpsLatitude; }
        public String getGPSLongitude() { return gpsLongitude; }
        public Boolean getIsWritable() { return isWritable; }
        public Boolean getIsPhotoFormat() { return isPhotoFormat; }

        void setFileType(String fileType) { this.fileType = fileType; }
        void setExtension(String extension) { this.extension = extension; }
        void setDateTime(String dateTime) { this.dateTime = dateTime; }
        void setTimeZone(String timeZone) { this.timeZone = timeZone; }
        void setGPSLatitude(String gpsLatitude) { this.gpsLatitude = gpsLatitude; }
        void setGPSLongitude(String gpsLongitude) { this.gpsLongitude = gpsLongitude; }
        void setIsWritable(Boolean isWritable) { this.isWritable = isWritable; }
        void setIsPhotoFormat(Boolean isPhotoFormat) { this.isPhotoFormat = isPhotoFormat; }
    }

    public ReadConfigYaml(String configFile) throws Exception {
        this.fileTypes = new ArrayList<>();
        int lineCount = 0;
        try {
            InputStream input = new FileInputStream(new File(configFile));
            Yaml yaml = new Yaml();
            Map config = yaml.load(input);
//            retrieve values for config

            if (config.get("filetypes") != null) {
                ArrayList<Map> fileTypeArray = (ArrayList<Map>) config.get("filetypes");
                for (Map fileTypeItem : fileTypeArray) {
                    lineCount++;
                    setFileType(fileTypeItem);
                }
            }
        }
        catch (Exception e) {
            String errorType = e.getClass().getName();
            String regexParser = "line (\\d+), column (\\d+):\n^(\\s*)(.+)$";
            if (errorType.equals("org.yaml.snakeyaml.parser.ParserException")) {
                Pattern pattern = Pattern.compile(regexParser, Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(e.getMessage());
                int line = 0;
                int column = 0;
                String sentence = "";
                if (matcher.find()) {
                    line = Integer.parseInt(matcher.group(1));
                    column = Integer.parseInt(matcher.group(2));
                    sentence = matcher.group(4);
                }
                throw new Exception(String.format("Error on line %d, column %d: %s", line, column, sentence));
            }
            else if (errorType.equals("java.io.FileNotFoundException")) {
                throw new Exception(String.format("%s not found",configFile));
            }
            else if (errorType.equals("java.text.ParseException")) {
                String regexDateParser = "^Unparseable date: \"(.+)\"$";
                Pattern pattern = Pattern.compile(regexDateParser);
                Matcher matcher = pattern.matcher(e.getMessage());
                String sentence = "";
                if (matcher.find()) {
                    sentence = matcher.group(1);
                }
                throw new Exception(String.format("Error in timeline %d, incorrect dateformat: %s", lineCount, sentence));
            }
            else if (errorType.equals("java.lang.Exception")) {
                throw new Exception(String.format("Error in timeline %d, %s", lineCount, e.getMessage()));
            }
            else if (errorType.contains("org.yaml.snakeyaml")) {
                Pattern pattern = Pattern.compile(regexParser, Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(e.getMessage());
                int line = 0;
                int column = 0;
                String sentence = "";
                if (matcher.find()) {
                    line = Integer.parseInt(matcher.group(1));
                    column = Integer.parseInt(matcher.group(2));
                    sentence = matcher.group(4);
                }
                throw new Exception(String.format("Error on line %d, column %d: undefined alias %s", line, column, sentence));
            }
            else {
                throw new Exception(String.format("Error in timeline %d: %s", lineCount, e.getMessage()));
            }
        }
    }

    private void setFileType(Map item) throws Exception {
        FileType fileType = new FileType();
        String value;
        Boolean boolValue;
        if (item.get("fileType") != null) {
            value = (String) item.get("fileType");
            fileType.setFileType(value);
        }
        if (item.get("extension") != null) {
            value = (String) item.get("extension");
            fileType.setExtension(value);
        }
        if (item.get("datetime") != null) {
            value = (String) item.get("datetime");
            fileType.setDateTime(value);
        }
        if (item.get("timezonde") != null) {
            value = (String) item.get("timezone");
            fileType.setTimeZone(value);
        }
        if (item.get("gpslatitude") != null) {
            value = (String) item.get("gpslatitude");
            fileType.setGPSLatitude(value);
        }
        if (item.get("gpslongitude") != null) {
            value = (String) item.get("gpslongitude");
            fileType.setGPSLongitude(value);
        }
        if (item.get("isWritable") != null) {
            boolValue = (Boolean) item.get("isWritable");
            fileType.setIsWritable(boolValue);
        }
        if (item.get("isPhotoFormat") != null) {
            boolValue = (Boolean) item.get("isPhotoFormat");
            fileType.setIsPhotoFormat(boolValue);
        }

        addFileType(fileType);
    }

    private void addFileType(FileType fileType) throws Exception {
        for (FileType element : this.fileTypes) {
            if (fileType.getFileType().equals(element.getFileType())) {
                throw new Exception(String.format("filetype: %s %s already exists", fileType.getFileType(), fileType.getFileType()));
            }
        }
        this.fileTypes.add(fileType);
    }

    public ArrayList<FileType> getFileTypes() {
        return this.fileTypes;
    }

    public FileType getFileType(String extension) {
        for (FileType filetype : this.getFileTypes()) {
            if (filetype.getExtension().equals(extension)) {
                return filetype;
            }
        }
        return null;
    }

    public String getRegexMedia(Boolean All) {
        ArrayList<FileType> items = this.getFileTypes();
        if (!All) {
            // remove all but isPhotoFormat filetypes
            items.removeIf(item -> !item.isPhotoFormat);
        }

        StringBuilder result = new StringBuilder("^(");
        for (int i = 0; i < items.size(); i++ ) {
            result.append("*\\.").append(items.get(i).extension);
            if (i < items.size()-1) {
                result.append("|");
            }
        }
        result.append(")$");
        return result.toString();
    }


}
