package com.usafe.controller;

import com.google.gson.Gson;
import com.usafe.entity.elasticsearch.Light;
import com.usafe.repository.elasticsearch.LightRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.QueryBuilders;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ElasticSearchController {

    private Gson gson = new Gson();

    LightRepository lightRepository;
    Client client;

    @Autowired
    public ElasticSearchController(Client client, LightRepository lightRepository) {
        this.client = client;
        this.lightRepository = lightRepository;
    }

    @PostMapping("/upload")
    public void uploadToEs(InputStream dataStream) throws Exception {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(dataStream));

            CSVParser parser = new CSVParser(br, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreEmptyLines(true));

            List<CSVRecord> records = parser.getRecords();

            for (CSVRecord record : records) {

                String easting = record.get("EASTING");
                String northing = record.get("NORTHING");

                CoordinateReferenceSystem stdCrs = CRS.decode("EPSG:4326");
                CoordinateReferenceSystem crs = CRS.decode("EPSG:29902");

                Coordinate c = new Coordinate(Double.valueOf(easting), Double.valueOf(northing));

                MathTransform t = CRS.findMathTransform(crs, stdCrs);
                Coordinate target = JTS.transform(c, null, t);

                Light light = new Light(Long.valueOf(easting), Long.valueOf(northing));
                light.setGeoPoint(new GeoPoint(target.x, target.y));

                lightRepository.save(light);
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

    @GetMapping("display")
    public List<Light> getLights() {
        Pageable p = PageRequest.of(1, 20);
        return lightRepository.findAll(p).getContent();
    }

    @PostMapping("/lightsOnPath")
    public Long getLightOnPath(@RequestParam("latlngs") List<String> latLngList) {

        List<GeoPoint> allPoints = new ArrayList<>();
        for (String l : latLngList) {
            Double[] r = convert(l);
            allPoints.add(new GeoPoint(r[0], r[1]));
        }

        SearchResponse resp = client.prepareSearch("lights")
                .setTypes("light")
                .setQuery(QueryBuilders.geoPolygonQuery("geoPoint", allPoints))
                .execute()
                .actionGet();

        Long result = resp.getHits().getTotalHits();

        return result;
    }

    private Double[] convert(String s) {
        Double[] res = new Double[2];
        String[] arr = s.substring(1, s.length() - 1).split(",");
        res[0] = Double.valueOf(arr[0]);
        res[1] = Double.valueOf(arr[1]);
        return res;
    }
}
