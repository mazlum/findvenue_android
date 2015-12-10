package com.app.konumbul.app;

public class Venue {
    private String venueString;
    private String venueType;

    public void setVenueString(String venueString) {
        this.venueString = venueString;
    }

    public void setVenueType(String venueType) {
        this.venueType = venueType;
    }

    public String getVenueType() {

        return venueType;
    }

    public String getVenueString() {
        return venueString;
    }


    public Venue(String venueString, String venueType) {
        super();
        this.venueString = venueString;
        this.venueType = venueType;
    }
}
