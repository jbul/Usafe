package com.usafe.controller;

import com.usafe.entity.elasticsearch.GardaStation;
import com.usafe.repository.elasticsearch.GardaRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/garda")
public class GardaController {

    private GardaRepository gardaRepository;
    private Client client;

    @Autowired
    public GardaController(Client client, GardaRepository gardaRepository) {
        this.client = client;
        this.gardaRepository = gardaRepository;
    }

    // See https://blog.mafr.de/2017/01/06/empty-inputstream-with-spring-mvc/
    @PostMapping("/upload")
    public void uploadGardaStationsToES(InputStream dataStream) throws Exception {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(dataStream));
            CSVParser parser = new CSVParser(br, CSVFormat.EXCEL.withFirstRecordAsHeader().withQuote('"'));

            List<CSVRecord> records = parser.getRecords();

            for (CSVRecord record : records) {

                String gardaStationName = record.get("Station");
                String easting = record.get("x");
                String northing = record.get("y");

                System.out.println(record.getRecordNumber() + ": " + easting + ", " + northing);

                CoordinateReferenceSystem stdCrs = CRS.decode("EPSG:4326");
                CoordinateReferenceSystem crs = CRS.decode("EPSG:29902");

                Coordinate c = new Coordinate(Double.valueOf(easting), Double.valueOf(northing));

                MathTransform t = CRS.findMathTransform(crs, stdCrs);
                Coordinate target = JTS.transform(c, null, t);

                try {
                    GardaStation garda = new GardaStation(gardaStationName, Long.valueOf(easting), Long.valueOf(northing));
                    garda.setGeoPoint(new GeoPoint(target.x, target.y));
                    gardaRepository.save(garda);
                } catch (NumberFormatException e) {
                    System.out.println("Could not parse: " + easting + ", " + northing);
                }
            }

        } catch (IOException ioe) {
            System.out.println("Exception while reading input " + ioe);
        } finally {
            // close the streams using close method
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
    }

    @PostMapping("/getGardaStations")
    public List<GardaStation> getGardaStations(@RequestParam("latlngs") List<String> latLngList) {

        System.out.println("Checking garda stations");
        List<GeoPoint> allPoints = new ArrayList<>();
        for (String l : latLngList) {
            Double[] r = convert(l);
            allPoints.add(new GeoPoint(r[0], r[1]));

        }

        //implement compare to method to avoid duplicates
        Set<GardaStation> gardaStations = new HashSet<>();
        for (GeoPoint p : allPoints) {

            SearchResponse resp = client.prepareSearch("gardastations")
                    .setTypes("gardastation")
                    .setQuery(
                            QueryBuilders
                                    .geoDistanceQuery("geoPoint")
                                    .point(p.getLat(), p.getLon())
                                    .distance(100, DistanceUnit.METERS)
                    ).execute().actionGet();

            System.out.println(resp.getHits().getTotalHits());

            SearchHit[] shs = resp.getHits().getHits();

            for (SearchHit sh : shs) {
                System.out.println("GS" + sh);
                GardaStation gs = gardaRepository.findById(sh.getId()).get();
                if (!gardaStations.contains(gs)) {
                    gardaStations.add(gs);
                }
            }
        }

        return new ArrayList<>(gardaStations);
    }

    private Double[] convert(String s) {
        Double[] res = new Double[2];
        String[] arr = s.substring(1, s.length() - 1).split(",");
        res[0] = Double.valueOf(arr[0]);
        res[1] = Double.valueOf(arr[1]);
        return res;
    }

}
