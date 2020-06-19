package project.julie.usafe_trial2.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class GardaStation implements Parcelable {


    private String id;
    private String station;
    private long easting;
    private long northing;

    private GeoPoint geoPoint;

    public GardaStation(){}

    public GardaStation(String station, long easting, long northing) {
        this.station = station;
        this.easting = easting;
        this.northing = northing;
    }

    protected GardaStation(Parcel in) {
        id = in.readString();
        station = in.readString();
        easting = in.readLong();
        northing = in.readLong();
        geoPoint = new GeoPoint();
        geoPoint.setLat(in.readDouble());
        geoPoint.setLon(in.readDouble());
    }

    public static final Creator<GardaStation> CREATOR = new Creator<GardaStation>() {
        @Override
        public GardaStation createFromParcel(Parcel in) {
            return new GardaStation(in);
        }

        @Override
        public GardaStation[] newArray(int size) {
            return new GardaStation[size];
        }
    };

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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(station);
        dest.writeLong(easting);
        dest.writeLong(northing);

        dest.writeDouble(geoPoint.getLat());
        dest.writeDouble(geoPoint.getLon());
    }


}
