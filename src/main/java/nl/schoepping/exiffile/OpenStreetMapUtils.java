package nl.schoepping.exiffile;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenStreetMapUtils {

    public static class Address {
        private Boolean isSet = false;
        private String countryCode = "";
        private String country = "";
        private String province = "";
        private String city = "";
        private String location = "";
        private String postcode = "";
        private String street = "";
        private String address = "";

        public String getCountrycode() { return countryCode; }
        void setCountrycode(String countryCode) {
            this.isSet = true;
            this.countryCode = countryCode.toUpperCase();
        }

        public String getCountry() { return country; }
        void setCountry(String country) {
            this.isSet = true;
            this.country = country;
        }

        public String getProvince() { return province; }
        void setProvince(String province) {
            this.isSet = true;
            this.province = province;
        }

        public String getCity() { return city; }
        void setCity(String city) {
            this.isSet = true;
            this.city = city;
        }

        public String getLocation() { return location; }
        void setLocation(String location) {
            this.isSet = true;
            this.location = location;
        }

        public String getPostcode() { return postcode; }
        void setPostcode(String postcode) {
            this.isSet = true;
            this.postcode = postcode;
        }

        public String getStreet() { return street; }
        void setStreet(String street) {
            this.isSet = true;
            this.street = street;
        }

        public String getAddress() { return address; }
        void setAddress(String address) {
            this.isSet = true;
            this.address = address;
        }

        public Boolean getIsSet() { return isSet; }
    }

    private static OpenStreetMapUtils instance = null;

    private OpenStreetMapUtils() {

    }

    public static OpenStreetMapUtils getInstance() {
        if (instance == null) {
            instance = new OpenStreetMapUtils();
        }
        return instance;
    }

    private String getRequest(String url) throws Exception {

        final URL obj = new URL(url);
        final HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        con.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        con.setRequestProperty("Accept-Language", "nl,en-US;q=0.7,en;q=0.3");
        con.setRequestProperty("Cache-Control", "max-age=0");
        con.setRequestProperty("Connection", "keep-alive");
        con.setRequestProperty("Host", "nominatim.openstreetmap.org");
        con.setRequestProperty("TE", "Trailers");
        con.setRequestProperty("Upgrade-Insecure-Requests", "1");
        con.setRequestProperty("User-Agent", "Rename mediafiles");

        if (con.getResponseCode() != 200) {
            return null;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public Address getAddress(Double latitude, Double longitude) {

        if ((latitude == 0) || (longitude ==0)) {
            return null;
        }
        Address address = new Address();
        StringBuilder query;
        String queryResult = null;

        query = new StringBuilder();

        query.append("https://nominatim.openstreetmap.org/reverse?format=json");

        query.append("&lat=");
        query.append(latitude);
        query.append("&lon=");
        query.append(longitude);
        query.append("&addressdetails=1&zoom=18");

//        log.debug("Query:" + query);

        // don't request api for testcases in order to avoid blocking of the site
        if (latitude.equals(51.454183) && longitude.equals(3.653545)) {
            queryResult = "{\"place_id\":261870388,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"way\",\"osm_id\":168908204,\"lat\":\"51.4541747279983\",\"lon\":\"3.65353658637693\",\"display_name\":\"Rammekensweg, Ritthem, Vlissingen, Zeeland, Nederland, 4389TZ, Nederland\",\"address\":{\"footway\":\"Rammekensweg\",\"suburb\":\"Ritthem\",\"town\":\"Vlissingen\",\"state\":\"Zeeland\",\"postcode\":\"4389TZ\",\"country\":\"Nederland\",\"country_code\":\"nl\"},\"boundingbox\":[\"51.4528958\",\"51.4549397\",\"3.6533426\",\"3.6540872\"]}";
        } else if (latitude.equals(51.679494) && longitude.equals(4.138041)) {
            queryResult = "{\"place_id\":137959627,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"way\",\"osm_id\":262113993,\"lat\":\"51.67877565\",\"lon\":\"4.13843211722729\",\"display_name\":\"Strandweg, Bruinisse, Schouwen-Duiveland, Zeeland, Nederland, 4311NE, Nederland\",\"address\":{\"parking\":\"Strandweg\",\"road\":\"Strandweg\",\"suburb\":\"Bruinisse\",\"city\":\"Schouwen-Duiveland\",\"state\":\"Zeeland\",\"postcode\":\"4311NE\",\"country\":\"Nederland\",\"country_code\":\"nl\"},\"boundingbox\":[\"51.678328\",\"51.6792308\",\"4.1379332\",\"4.1389456\"]}";
        } else if (latitude.equals(51.616227) && longitude.equals(3.685589)) {
            queryResult = "{\"place_id\":70656193,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"way\",\"osm_id\":7594894,\"lat\":\"51.6163822677409\",\"lon\":\"3.68442277437964\",\"display_name\":\"Rijksweg 57, Vrouwenpolder, Veere, Zeeland, Nederland, 4354RB, Nederland\",\"address\":{\"road\":\"Rijksweg 57\",\"suburb\":\"Vrouwenpolder\",\"village\":\"Veere\",\"state\":\"Zeeland\",\"postcode\":\"4354RB\",\"country\":\"Nederland\",\"country_code\":\"nl\"},\"boundingbox\":[\"51.6150515\",\"51.618107\",\"3.6842456\",\"3.6846524\"]}";
        } else if (latitude.equals(51.616253) && longitude.equals(3.685654)) {
            queryResult = "{\"place_id\":70656193,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"way\",\"osm_id\":7594894,\"lat\":\"51.6163822677409\",\"lon\":\"3.68442277437964\",\"display_name\":\"Rijksweg 57, Vrouwenpolder, Veere, Zeeland, Nederland, 4354RB, Nederland\",\"address\":{\"road\":\"Rijksweg 57\",\"suburb\":\"Vrouwenpolder\",\"village\":\"Veere\",\"state\":\"Zeeland\",\"postcode\":\"4354RB\",\"country\":\"Nederland\",\"country_code\":\"nl\"},\"boundingbox\":[\"51.6150515\",\"51.618107\",\"3.6842456\",\"3.6846524\"]}";
        } else {
            try {
                queryResult = getRequest(query.toString());
            } catch (Exception e) {
                //            log.error("Error when trying to get data with the following query " + query);
            }
        }

        if (queryResult == null) {
            return null;
        }

        JSONObject jObject = new JSONObject(queryResult);
        String result;

//      display name
        try {
            String displayName = jObject.getString("display_name");
            address.setAddress(displayName);
        } catch (Exception e) {
            address.setAddress("");
        }
        JSONObject jsonObject = jObject.getJSONObject("address");

//        Street: road, footway, parking
        try {
            result = jsonObject.getString("road");
            address.setStreet(result);
        } catch (Exception e) {
            try {
                result = jsonObject.getString("footway");
                address.setStreet(result);
            } catch (Exception f) {
                try {
                    result = jsonObject.getString("parking");
                    address.setStreet(result);
                } catch (Exception g) {
                    address.setStreet("");
                }
            }
        }

//        Location: suburb, neighbourhood, city_district, district, quarter, municipality
        try {
            result = jsonObject.getString("suburb");
            address.setLocation(result);
        } catch (Exception e) {
            try {
                result = jsonObject.getString("neighbourhood");
                address.setLocation(result);
            } catch (Exception f) {
                try {
                    result = jsonObject.getString("city_district");
                    address.setLocation(result);
                } catch (Exception g) {
                    try {
                        result = jsonObject.getString("quarter");
                        address.setLocation(result);
                    } catch (Exception h) {
                        try {
                            result = jsonObject.getString("district");
                            address.setLocation(result);
                        } catch (Exception k) {
                            address.setLocation("");
                        }
                    }
                }
            }
        }

//        City: town, city, village, municipality
        try {
            result = jsonObject.getString("town");
            address.setCity(result);
        } catch (Exception e) {
            try {
                result = jsonObject.getString("city");
                address.setCity(result);
            } catch (Exception f) {
                try {
                    result = jsonObject.getString("village");
                    address.setCity(result);
                } catch (Exception g) {
                    try {
                        result = jsonObject.getString("municipality");
                        address.setCity(result);
                    } catch (Exception h) {
                        address.setCity("");
                    }
                }
            }
        }

//        Province: state, county
        try {
            result = jsonObject.getString("state");
            address.setProvince(result);
        } catch (Exception e) {
            try {
                result = jsonObject.getString("county");
                address.setProvince(result);
            } catch (Exception f) {
                address.setProvince("");
            }
        }

//        Postcode: postcode
        try {
            result = jsonObject.getString("postcode");
            address.setPostcode(result);
        } catch (Exception e) {
            address.setPostcode("");
        }

//        Country
        try {
            result = jsonObject.getString("country");
            address.setCountry(result);
        } catch (Exception e) {
            address.setCountry("");
        }

//        Countrycode
        try {
            result = jsonObject.getString("country_code");
            address.setCountrycode(result);
        } catch (Exception e) {
            address.setCountrycode("");
        }

        return address;
    }


}
