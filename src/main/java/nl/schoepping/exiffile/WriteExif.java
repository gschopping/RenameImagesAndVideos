package nl.schoepping.exiffile;

import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.core.util.FileUtils;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class WriteExif {
    private final String exiftool;
    private final String mediaFile;
    private final boolean isWritable;
    private final boolean removeOriginal;
    private final List<String> arguments;
    private Logger logger = null;
    private final String tempfile;

    public WriteExif(String mediaFile, boolean removeOriginal) throws IOException {
        this.mediaFile = mediaFile;
        this.removeOriginal = removeOriginal;
        String slash = FileSystems.getDefault().getSeparator();
        if (System.getProperty("os.name").contains("Windows")) {
            this.tempfile =  System.getProperty("user.dir") + "arguments.txt";
            this.exiftool = "exiftool.bat";
        } else {
            this.tempfile =  System.getProperty("user.dir") + slash + "arguments.txt";
            this.exiftool = "exiftool";
        }
        String fileType = this.getFileType();
        this.isWritable = fileType.equals("JPG") || fileType.equals("ARW") || fileType.equals("MP4") || fileType.equals("DNG") || fileType.equals("HEIF");
        this.arguments = new ArrayList<>();
        if (removeOriginal) {
            this.arguments.add("-overwrite_original");
        }
    }

    public void setLogger(Logger logger) { this.logger = logger; }

    private String getSpaceReplacedFileName() { return replaceSpaces(this.mediaFile); }
    private String replaceSpaces(String fileName) {
        String result = fileName;
        if (System.getProperty("os.name").contains("Windows")) {
            result = "\"" + fileName + "\"";
        }
        return result;
    }



    private void writeArguments() throws IOException {
        FileWriter fileWriter = new FileWriter(tempfile);
        for (String argument : this.arguments) {
            fileWriter.write(argument + "\n");
        }
        fileWriter.close();
    }

    private String ISO3Code(String countryCode) {
        Locale obj = new Locale("", countryCode);
        return obj.getISO3Country();
    }

    private String getFileType() throws IOException {
        String fileType;
        String cmdString = exiftool + " -s3 -File:FileType " + getSpaceReplacedFileName();
        Process process = Runtime.getRuntime().exec(cmdString);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        fileType = reader.readLine();
        reader.close();
        return fileType;
    }

    private void writeTag(String[] tags, String value) {
        for (String tag : tags) {
            this.arguments.add("-" + tag + "=" + value);
        }
    }

    private void writeKeys(String[] tags, String[] values) {
        for (String value : values) {
            for (String tag : tags) {
                this.arguments.add("-" + tag + "=" + value);
            }
        }
    }

    public void writeFile(String writeFile, boolean overWrite) throws IOException {
        File destinationFile = new File(writeFile);
        if (overWrite) {
            if (destinationFile.exists()) {
                if (! destinationFile.delete()) throw new IOException(String.format("%s can't be deleted", destinationFile.getPath()));
            }
        }
        else {
            if (destinationFile.exists()) throw new IOException(String.format("%s already exists", destinationFile.getPath()));
        }
        if (this.isWritable) {
            String result;
            writeArguments();
            // Before copting first delete original file

            String[] cmdString = new String[] { exiftool, "-m", "-charset", "FileName=UTF8", "-api", "largefilesupport=1", "-@", tempfile, getSpaceReplacedFileName(), "-o",  replaceSpaces(writeFile) };
            if (logger != null) {
                logger.debug("WriteEXIF: writeFile {}", Arrays.toString(cmdString));
            }
            Process process = Runtime.getRuntime().exec(cmdString);
            if (process == null) {
                throw new IOException("Can't execute " + Arrays.toString(cmdString));
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // read result lines, if any contains files is updated then continue
            boolean fileIsUpdated = false;
            result = reader.readLine();
            while (result != null) {
                if (logger != null) {
                    logger.debug("WriteEXIF: writeFile {}", result);
                }
                if (result.matches("\\s*1 image files (created|updated)")) {
                    fileIsUpdated = true;
                    break;
                }
                result = reader.readLine();
            }
            if (! fileIsUpdated) {
                throw new IOException("Update of file " + writeFile + " failed");
            }
            reader.close();
        }
        else {
            if (this.removeOriginal) {
//                FileUtils.moveFile(new File(this.mediaFile), destinationFile);
                Files.move(Paths.get(mediaFile), destinationFile.toPath());
            }
            else {
                Files.copy(Paths.get(mediaFile), destinationFile.toPath());
            }
        }
    }

    public void setAuthor(String author) {
        if (! author.isEmpty()) {
            String[] tags = new String[6];
            tags[0] = "EXIF:Artist";
            tags[1] = "EXIF:XPAuthor";
            tags[2] = "XMP:Creator";
            tags[3] = "XMP:CaptionWriter";
            tags[4] = "IPTC:By-line";
            tags[5] = "IPTC:Writer-Editor";
            writeTag(tags, author);
        }
    }

    void setCopyright(String copyright) {
        if (! copyright.isEmpty()) {
            String[] tags = new String[3];
            tags[0] = "EXIF:Copyright";
            tags[1] = "XMP:Rights";
            tags[2] = "IPTC:CopyrightNotice";
            writeTag(tags, copyright);
        }
    }

    void setComment(String comment) {
        if (! comment.isEmpty()) {
            String[] tags = new String[2];
            tags[0] = "EXIF:UserComment";
            tags[1] = "EXIF:XPComment";
            writeTag(tags, comment);
        }
    }

    public void setCountryCode(String countryCode) {
        if (! countryCode.isEmpty()) {
            String[] tags = new String[1];
            tags[0] = "XMP:CountryCode";
            writeTag(tags, countryCode);
            tags[0] = "IPTC:Country-PrimaryLocationCode";
            writeTag(tags, this.ISO3Code(countryCode));
        }
    }

    public void setCountry(String country) {
        if (! country.isEmpty()) {
            String[] tags = new String[2];
            tags[0] = "XMP:Country";
            tags[1] = "IPTC:Country-PrimaryLocationName";
            writeTag(tags, country);
        }
    }

    void setProvince(String province) {
        if (! province.isEmpty()) {
            String[] tags = new String[2];
            tags[0] = "XMP:State";
            tags[1] = "IPTC:Province-State";
            writeTag(tags, province);
        }
    }

    public void setCity(String city) {
        if (! city.isEmpty()) {
            String[] tags = new String[1];
            tags[0] = "XMP:City";
            writeTag(tags, city);
        }
    }

    void setLocation(String location) {
        if (! location.isEmpty()) {
            String[] tags = new String[2];
            tags[0] = "IPTC:Sub-location";
            tags[1] = "IPTC:ObjectName";
            writeTag(tags, location);
        }
    }

    public void setTitle(String title) {
        if (! title.isEmpty()) {
            String[] tags = new String[4];
            tags[0] = "XMP:Title";
            tags[1] = "EXIF:ImageDescription";
            tags[2] = "EXIF:XPTitle";
            tags[3] = "XMP:Description";
            writeTag(tags, title);
        }
    }

    void setURL(String url) {
        if (! url.isEmpty()) {
            String[] tags = new String[2];
            tags[0] = "XMP:BaseURL";
            tags[1] = "Photoshop:URL";
            writeTag(tags, url);
        }
    }

    void setDescription(String description) {
        if (! description.isEmpty()) {
            String[] tags = new String[3];
            tags[0] = "XMP:Headline";
            tags[1] = "EXIF:XPSubject";
            tags[2] = "IPTC:Caption-Abstract";
            writeTag(tags, description);
        }
    }

    public void setKeys(String[] keys) {
        if (keys.length > 0) {
            String[] tags = new String[4];
            tags[0] = "XMP:Subject";
            tags[1] = "IPTC:Keywords";
            tags[2] = "XMP:LastKeywordXMP";
            tags[3] = "XMP:LastKeywordIPTC";
            writeKeys(tags, keys);
            String[] tags2 = new String[1];
            String value = "";
            for (String key : keys) {
                if (value.isEmpty()) {
                    value = key;
                } else value = value.concat(", " + key);
            }
            tags2[0] = "EXIF:XPKeywords";
            writeTag(tags2, value);
        }
    }

    void setSpecialInstructions(String specialInstructions) {
        if (! specialInstructions.isEmpty()) {
            String[] tags = new String[2];
            tags[0] = "IPTC:SpecialInstructions";
            tags[1] = "XMP:Instructions";
            writeTag(tags, specialInstructions);
        }
    }

    public void setAddress(OpenStreetMapUtils.Address address) {
        setCountryCode(address.getCountrycode());
        setCountry(address.getCountry());
        setCity(address.getCity());
        setProvince(address.getProvince());
        setLocation(address.getLocation());

    }

}
