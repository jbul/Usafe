package project.julie.usafe_trial2.entity;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class PathBundle {
    private Integer idx;
    private List<LatLng> latLngs;
    private List<List<Double>> doubleLatLngs;
    private Score score;

    public PathBundle() {
        doubleLatLngs = new ArrayList<>();
        this.latLngs = new ArrayList<>();
    }

    public PathBundle(Integer idx, List<LatLng> latLngs, List<List<Double>> doubleLatLngs) {
        this.idx = idx;
        this.latLngs = latLngs;
        this.doubleLatLngs = doubleLatLngs;
    }

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public List<LatLng> getLatLngs() {
        return latLngs;
    }

    public void setLatLngs(List<LatLng> latLngs) {
        this.latLngs = latLngs;
    }

    public List<List<Double>> getDoubleLatLngs() {
        return doubleLatLngs;
    }

    public void setDoubleLatLngs(List<List<Double>> doubleLatLngs) {
        this.doubleLatLngs = doubleLatLngs;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }
}
