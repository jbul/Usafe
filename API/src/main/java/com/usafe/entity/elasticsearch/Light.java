package com.usafe.entity.elasticsearch;

import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

@Document(indexName = "lights", type = "light")
public class Light {

    @Id
    private String id;
    private long easting;
    private long northing;

    @GeoPointField
    private GeoPoint geoPoint;

    public Light() {
    }

    public Light(long easting, long northing) {
        this.easting = easting;
        this.northing = northing;
        this.geoPoint = new GeoPoint();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
    public int hashCode() {
        return this.id.hashCode();
    }
}
