package nl.schoepping.exiffile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.Yaml;

public class ReadTimeLineYaml {
    private String fileName;
    private Boolean Enabled = true;
    private Boolean AvoidNonAscii = true;
    private final List<TimeLine> timeLines;

    public static class TimeLine {
        private LocalDateTime startDate = null;
        private LocalDateTime endDate = null;
        private String title = "";
        private String countryCode = "";
        private String country = "";
        private String province = "";
        private String city = "";
        private String location = "";
        private String description = "";
        private String author = "";
        private String website = "";
        private String copyright = "";
        private String comment = "";
        private String keys = "";
        private String instructions = "";
        private Boolean override = false;

        public LocalDateTime getStartDate() { return startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public String getTitle() { return title; }
        public String getCountryCode() { return countryCode; }

        public String getCountry() {
            if (! this.countryCode.isEmpty()) {
                Locale obj = new Locale("", this.countryCode);
                return obj.getDisplayCountry();
            } else {
                return country;
            }
        }

        public String getProvince() { return province; }
        public String getCity() { return city; }
        public String getLocation() { return location; }
        public String getDescription() { return description; }
        public String getAuthor() { return author; }
        public String getWebsite() { return website; }
        public String getCopyright() { return copyright; }
        public String getComment() { return comment; }
        public String getKeys() { return keys; }
        public String getInstructions() { return instructions; }
        public Boolean getOverride() { return override; }

        public void setStartDate(String startDate) throws ParseException {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            this.startDate = LocalDateTime.parse(startDate, formatter);
        }

        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        public void setTitle(String title) { this.title = title; }

        public void setCountryCode(String countryCode) throws Exception {
            Locale obj = new Locale("", countryCode);
            try {
                String code = obj.getISO3Country();
            }
            catch (Exception e){
                throw new Exception(String.format("countrycode: %s is not valid", countryCode));
            }
            this.countryCode = obj.getCountry();
        }

        public void setCountry(String country) { this.country = country; }
        public void setProvince(String province) { this.province = province; }
        public void setCity(String city) { this.city = city; }
        public void setLocation(String location) { this.location = location; }
        public void setDescription(String description) { this.description = description; }
        public void setAuthor(String author) { this.author = author; }
        public void setWebsite(String website) { this.website = website; }
        public void setCopyRight(String copyright) { this.copyright = copyright; }
        public void setComment(String comment) { this.comment = comment; }
        public void setKeys(String keys) { this.keys = keys; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
        public void setOverride(Boolean override) { this.override = override; }
    }

    class SortByDate implements Comparator<TimeLine> {

        @Override
        public int compare(TimeLine o1, TimeLine o2) {
            return o1.getStartDate().compareTo(o2.getStartDate());
        }

    }

    public ReadTimeLineYaml(String timeLineFile) throws Exception {
        this.fileName = timeLineFile;
        this.timeLines = new ArrayList<>();
        int lineCount = 0;
        try {
            InputStream input = new FileInputStream(new File(timeLineFile));
            Yaml yaml = new Yaml();
            Map timeLine = yaml.load(input);
//            retrieve values for config
            if (timeLine.get("config") != null) {
                Map config = (Map) timeLine.get("config");
                if (config.get("enabled") != null) {
                    this.Enabled = (Boolean) config.get("enabled");
                }
                if (config.get("avoidnonascii") != null) {
                    this.AvoidNonAscii = (Boolean) config.get("avoidnonascii");
                }
            }

            if (timeLine.get("timeline") != null) {
                ArrayList<Map> timelineArray = (ArrayList<Map>) timeLine.get("timeline");
                for (Map timelineItem : timelineArray) {
                    lineCount++;
                    setTimeLine(timelineItem);
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
                throw new Exception(String.format("%s not found",timeLineFile));
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

    private void setTimeLine(Map item) throws Exception {
        TimeLine timeline = new TimeLine();
        String value;
        Boolean boolValue;
        if (item.get("countrycode") != null) {
            value = (String) item.get("countrycode");
            timeline.setCountryCode(value);
        }
        if (item.get("country") != null) {
            value = (String) item.get("country");
            timeline.setCountry(value);
        }
        if (item.get("province") != null) {
            value = (String) item.get("province");
            timeline.setProvince(value);
        }
        if (item.get("city") != null) {
            value = (String) item.get("city");
            timeline.setCity(value);
        }
        if (item.get("creator") != null) {
            value = (String) item.get("creator");
            timeline.setAuthor(value);
        }
        if (item.get("website") != null) {
            value = (String) item.get("website");
            timeline.setWebsite(value);
        }
        if (item.get("copyright") != null) {
            value = (String) item.get("copyright");
            timeline.setCopyRight(value);
        }
        if (item.get("startdate") != null) {
            value = (String) item.get("startdate");
            timeline.setStartDate(value);
        }
        if (item.get("title") != null) {
            value = (String) item.get("title");
            timeline.setTitle(value);
        }
        if (item.get("location") != null) {
            value = (String) item.get("location");
            timeline.setLocation(value);
        }
        if (item.get("description") != null) {
            value = (String) item.get("description");
            timeline.setDescription(value);
        }
        if (item.get("comment") != null) {
            value = (String) item.get("comment");
            timeline.setComment(value);
        }
        if (item.get("instructions") != null) {
            value = (String) item.get("instructions");
            timeline.setInstructions(value);
        }
        if (item.get("keys") != null) {
            value = (String) item.get("keys");
            timeline.setKeys(value);
        }
        if (timeline.startDate == null) {
            throw new Exception("startdate is not filled");
        }
        if (item.get("override") != null) {
            boolValue = (Boolean) item.get("override");
            timeline.setOverride(boolValue);
        }
        addTimeline(timeline);
    }

    private void addTimeline(TimeLine timeline) throws Exception {
        for (TimeLine element : this.timeLines) {
            if (timeline.getStartDate().equals(element.getStartDate())) {
                throw new Exception(String.format("startdate: %tF %tT already exists", timeline.getStartDate(), timeline.getStartDate()));
            }
        }
        this.timeLines.add(timeline);
    }

    private void setEndDate() {
        LocalDateTime date = null;
        for (int count = this.timeLines.size() - 1; count >= 0; count--) {
            if (count < this.timeLines.size() - 1) {
                this.timeLines.get(count).setEndDate(date);
            }
            date = this.timeLines.get(count).getStartDate();
        }
    }

    public List<TimeLine> getTimeLines() {
        this.timeLines.sort(new SortByDate());
        setEndDate();
        return this.timeLines;
    }

    public TimeLine getTimeLine(LocalDateTime date) {
        TimeLine result = null;
        for (TimeLine timeline : this.getTimeLines()) {
            if ((!date.isBefore(timeline.getStartDate())) &&
                    ((timeline.getEndDate() == null) ||
                            (date.isBefore(timeline.getEndDate())))){
                result = timeline;
                break;
            }
        }
        return result;
    }

    public Boolean getEnabled() { return this.Enabled; }
    public Boolean getAvoidNonAscii() { return this.AvoidNonAscii; }
    public String getFileName() { return this.fileName; }
}
