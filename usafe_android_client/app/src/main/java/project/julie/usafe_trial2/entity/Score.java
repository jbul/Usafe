package project.julie.usafe_trial2.entity;

public class Score {

    private int gardaStationsCount;
    private long lightsCount;
    private double pathLength;

    public Score() {
    }

    public Score(int gardaStationsCount, int lightsCount, double pathLength) {
        this.gardaStationsCount = gardaStationsCount;
        this.lightsCount = lightsCount;
        this.pathLength = pathLength;
    }

    public int getGardaStationsCount() {
        return gardaStationsCount;
    }

    public void setGardaStationsCount(int gardaStationsCount) {
        this.gardaStationsCount = gardaStationsCount;
    }

    public long getLightsCount() {
        return lightsCount;
    }

    public void setLightsCount(long lightsCount) {
        this.lightsCount = lightsCount;
    }

    public double getPathLength() {
        return pathLength;
    }

    public void setPathLength(double pathLength) {
        this.pathLength = pathLength;
    }
}
