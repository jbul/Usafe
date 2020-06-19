package com.usafe.entity.elasticsearch;

import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

@Document(indexName = "gardastations", type = "gardastation")
public class GardaStation {

    @Id
    private String id;
    private String station;
    private long easting;
    private long northing;

    @GeoPointField
    private GeoPoint geoPoint;

    public GardaStation() {
    }

    public GardaStation(String station, long easting, long northing) {
        this.station = station;
        this.easting = easting;
        this.northing = northing;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public long getEasting() {
        return easting;
    }

    public void setEasting(long easting) {
        this.easting = easting;
    }

    public long getNorthing() {
        return northing;
    }

    public void setNorthing(long northing) {
        this.northing = northing;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    @Override
    public boolean equals(Object obj) {
        GardaStation gardaStation = (GardaStation) obj;
        if (gardaStation.getId().equals(this.getId())
                && gardaStation.getStation().equals(this.getStation())
                && gardaStation.getEasting() == this.getEasting()
                && gardaStation.getNorthing() == this.getNorthing()) {

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
