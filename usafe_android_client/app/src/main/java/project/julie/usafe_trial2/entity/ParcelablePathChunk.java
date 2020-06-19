package project.julie.usafe_trial2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParcelablePathChunk implements Parcelable {

    private List<Double> chunk;

    public ParcelablePathChunk(List<Double> chunk) {
        this.chunk = chunk;
    }

    protected ParcelablePathChunk(Parcel in) {
        double[] values = new double[2];
        in.readDoubleArray(values);
        chunk = Arrays.asList(values[0], values[1]);
    }

    public static final Creator<ParcelablePathChunk> CREATOR = new Creator<ParcelablePathChunk>() {
        @Override
        public ParcelablePathChunk createFromParcel(Parcel in) {
            return new ParcelablePathChunk(in);
        }

        @Override
        public ParcelablePathChunk[] newArray(int size) {
            return new ParcelablePathChunk[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDoubleArray(new double[] { chunk.get(0), chunk.get(1) });
    }

    public List<Double> getChunk() {
        return chunk;
    }
}
