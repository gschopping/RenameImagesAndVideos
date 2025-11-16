package nl.schoepping.exiffile;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ReadFiles {
    private String regexMedia = "";
    private String regexTimelaps = "";
    private String regexGPS = "";
    private String regexTimelapsFile = "";
    private String path;
    private ReadConfigYaml configYaml;

    public ReadFiles(ReadConfigYaml configYaml, String path) {
        this.path = path;
        this.configYaml = configYaml;
        this.regexMedia = configYaml.getRegexMedia(true);
        this.regexTimelapsFile = configYaml.getRegexMedia(false);
        this.regexTimelaps = configYaml.getPathForTimelaps();
        this.regexGPS = configYaml.getPathForGPS();
    }

    public static class EXIFFile {
        private File file;
        private File path;
        private Double latitude = 0.0;
        private Double longitude = 0.0;
        private LocalDateTime creationDate;

        public File getFile() { return file; }
        public void setFile(File file) { this.file = file; }
        public File getPath() { return path; }
        public void setPath(File path) { this.path = path; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public LocalDateTime getCreationDate() { return creationDate; }
        public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
    }

    public List<File> getFilesFromDirectory() {
        File dir = new File(this.path);
        File [] files = dir.listFiles();
        List<File> result = new ArrayList<>();

        if (files != null) {
            Arrays.sort( files, (Comparator) Comparator.comparing(o -> ((File) o).getName()));
            for (File child : files) {
                if (child.getName().toUpperCase().matches(regexMedia)) {
                    result.add(child);
                }
            }
        }

        return result;
    }

    public List<File> getTimelapsDirectories() {
        File dir = new File(this.path);
        File[] files = dir.listFiles(File::isDirectory);
        List<File> result = new ArrayList<>();

        if (files != null) {
            for (File child : files) {
                if (child.getName().matches(regexTimelaps)) {
                    result.add(child);
                }
            }
        }
        return result;
    }

    List<File> getGPSDirectories() {
        File dir = new File(this.path);
        File[] files = dir.listFiles(File::isDirectory);
        List<File> result = new ArrayList<>();

        if (files != null) {
            for (File child : files) {
                if (child.getName().matches(regexGPS)) {
                    result.add(child);
                }
            }
        }
        return result;
    }

    List<File> getTimelapsFiles(File dirTimelaps) {
        File dir = new File(dirTimelaps.getPath());
        File[] files = dir.listFiles();
        List<File> result = new ArrayList<>();

        if (files != null) {
            Arrays.sort(files, (Comparator) Comparator.comparing(o -> ((File) o).getName()));

            for (File child : files) {
                if (child.getName().toUpperCase().matches(regexTimelapsFile)) {
                    result.add(child);
                }
            }
        }
        return result;
    }

    public List<EXIFFile> getFiles() throws IOException, ParseException {
        List<EXIFFile> result = new ArrayList<>();

        Arrays.sort( result.toArray(), (Comparator) Comparator.comparing(o -> ((EXIFFile) o).getCreationDate()));


        // read Root files
        File dir = new File(this.path);
        File [] files = dir.listFiles();

        if (files != null) {
            for (File child : files) {
                if (child.getName().toUpperCase().matches(regexMedia)) {
                    EXIFFile exifFile = new EXIFFile();
                    exifFile.setFile(child);
                    exifFile.setPath(dir);
                    ReadExif readEXIF = new ReadExif(configYaml, child.getPath());
                    exifFile.setCreationDate(readEXIF.getCreateDateTime());
                    exifFile.setLatitude(readEXIF.getGPSLatitude());
                    exifFile.setLongitude(readEXIF.getGPSLongitude());
                    result.add(exifFile);
                }
            }
        }

        // read Timelaps and GPS files
        File[] dirs = dir.listFiles(File::isDirectory);

        if (dirs != null) {
            for (File child : dirs) {
                if (child.getName().matches(regexTimelaps) || child.getName().matches(regexGPS)) {
                    File subdir = new File(child.getPath());
                    File[] subfiles = subdir.listFiles();
                    if (subfiles != null) {
                        for (File subchild : subfiles) {
                            if (subchild.getName().toUpperCase().matches(regexTimelapsFile)) {
                                EXIFFile exifFile = new EXIFFile();
                                exifFile.setFile(subchild);
                                exifFile.setPath(subdir);
                                ReadExif readEXIF = new ReadExif(configYaml, subchild.getPath());
                                exifFile.setCreationDate(readEXIF.getCreateDateTime());
                                exifFile.setLatitude(readEXIF.getGPSLatitude());
                                exifFile.setLongitude(readEXIF.getGPSLongitude());
                                result.add(exifFile);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

}
