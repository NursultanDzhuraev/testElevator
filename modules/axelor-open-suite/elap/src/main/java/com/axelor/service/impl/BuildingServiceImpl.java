package com.axelor.service.impl;


import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.erp.db.Apartment;
import com.axelor.apps.erp.db.Building;
import com.axelor.apps.erp.db.Entrance;
import com.axelor.apps.erp.db.GeoResult;
import com.axelor.apps.erp.db.repo.BuildingRepository;
import com.axelor.apps.erp.db.repo.GeoResultRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.service.BuildingService;
import com.google.gson.Gson;
import com.google.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BuildingServiceImpl implements BuildingService {
    private final MapService mapService;
    private final GeoResultRepository geoRepo;
    private final BuildingRepository buildingRepo;
    private static final Map<Long, Map<String, Object>> PENDING_GEO = new ConcurrentHashMap<>();

    @Inject
    public BuildingServiceImpl(MapService mapService, GeoResultRepository geoResultRepository, BuildingRepository buildingRepo) {
        this.mapService = mapService;
        this.geoRepo = geoResultRepository;
        this.buildingRepo = buildingRepo;
    }

    @Override
    public Map<String, Object> displayMap(Building building) {
        Map<String, Object> mapView = new HashMap<>();
        if (building != null && building.getLatitude() != null && building.getLongitude() != null) {
            mapView.put("title", "Map");
            mapView.put("resource", mapService.getMapUrl(building.getLatitude(), building.getLongitude()));
            mapView.put("viewType", "html");
        }
        return mapView;
    }

    @Override
    public void geocodeAddress(ActionRequest request, ActionResponse response) {
        try {
            Building building = request.getContext().asType(Building.class);
            String address = building.getAddress();

            if (address == null || address.trim().isEmpty()) {
                return;
            }

            String url = "https://nominatim.openstreetmap.org/search?q=" +
                    java.net.URLEncoder.encode(address, "UTF-8") +
                    "&format=json&addressdetails=1&limit=5&countrycodes=kg&bounded=1&viewbox=69.2,43.3,80.3,39.1";

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("User-Agent", "Axelor-Building-App")
                    .build();

            java.net.http.HttpResponse<String> httpResponse =
                    client.send(httpRequest, java.net.http.HttpResponse.BodyHandlers.ofString());

            Gson gson = new Gson();
            List<Map<String, Object>> results = gson.fromJson(
                    httpResponse.body(),
                    new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>() {
                    }.getType()
            );

            if (results == null || results.isEmpty()) {
                response.setAlert("Not found address");
                return;
            }
            if (results.size() > 1) {
                List<GeoResult> geoResults = new ArrayList<>();
                JPA.runInTransaction(() -> {
                    for (Map<String, Object> loc : results) {
                        Map<String, Object> addr = (Map<String, Object>) loc.get("address");
                        GeoResult geoResult = new GeoResult();
                        geoResult.setDisplayName(loc.get("display_name").toString());
                        geoResult.setRoad((String) addr.getOrDefault("road", ""));
                        geoResult.setHouseNumber((String) addr.getOrDefault("house_number", ""));
                        geoResult.setLatitude(new BigDecimal(loc.get("lat").toString()));
                        geoResult.setLongitude(new BigDecimal(loc.get("lon").toString()));
                        geoRepo.save(geoResult);
                        geoResults.add(geoResult);
                    }
                });

                response.setValue("geoResult", geoResults);
                response.setValue("$showPossible", true);
                return;
            }

            Map<String, Object> loc = results.get(0);
            BigDecimal lat = BigDecimal.valueOf(Double.parseDouble(loc.get("lat").toString())).setScale(6, RoundingMode.HALF_UP);
            BigDecimal lon = BigDecimal.valueOf(Double.parseDouble(loc.get("lon").toString())).setScale(6, RoundingMode.HALF_UP);
            Map<String, Object> addressStr = (Map<String, Object>) loc.get("address");
            String road = addressStr.get("road").toString();
            String houseNumber = addressStr.get("house_number") == null ? "" : addressStr.get("house_number").toString();
            String addressBuilding = road + " " + houseNumber;
            building.setLatitude(lat);
            building.setLongitude(lon);
            building.setAddress(addressBuilding);
            response.setValue("latitude", lat);
            response.setValue("longitude", lon);
            response.setValue("address", addressBuilding);
            response.setAttr("map", "refresh", true);

        } catch (Exception e) {
            response.setError("error geo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void applySelectedGeoResult(ActionRequest request, ActionResponse response) {
        try {
            Map<String, Object> data = request.getData();
            Map<String, Object> ctx = (Map<String, Object>) data.get("context");
            Map<String, Object> parent = (Map<String, Object>) ctx.get("_parent");

            if (ctx == null || parent == null) {
                response.setError("Context null");
                return;
            }

            BigDecimal latitude = new BigDecimal(ctx.get("latitude").toString()).setScale(6, java.math.RoundingMode.HALF_UP);
            BigDecimal longitude = new BigDecimal(ctx.get("longitude").toString()).setScale(6, java.math.RoundingMode.HALF_UP);
            String road = ctx.get("road") != null ? ctx.get("road").toString() : "";
            String houseNumber = ctx.get("houseNumber") != null ? ctx.get("houseNumber").toString() : "";
            String displayName = ctx.get("displayName") != null ? ctx.get("displayName").toString() : "";

            String fullAddr = road.trim();
            if (!houseNumber.trim().isEmpty()) {
                fullAddr += " " + houseNumber.trim();
            }
            if (fullAddr.isEmpty()) fullAddr = displayName.trim();

            Object buildingIdObj = parent.get("id");
            if (buildingIdObj != null && !buildingIdObj.toString().equals("null")) {
                Long buildingId = Long.parseLong(buildingIdObj.toString());
                String finalFullAddr = fullAddr;
                JPA.runInTransaction(() -> {
                    Building building = buildingRepo.find(buildingId);
                    building.setLatitude(latitude);
                    building.setLongitude(longitude);
                    building.setAddress(finalFullAddr);
                    buildingRepo.save(building);
                });
                response.setReload(true);
                return;
            }
            Map<String, Object> pending = new HashMap<>();
            pending.put("latitude", latitude);
            pending.put("longitude", longitude);
            pending.put("address", fullAddr);
            PENDING_GEO.put(AuthUtils.getUser().getId(), pending);
            response.setValue("_parent", Map.of(
                    "$showPossible", false,
                    "geoResult", Collections.emptyList()
            ));
        } catch (Exception e) {
            log.error("Geo selection error", e);
            response.setError("Ката: " + e.getMessage());
        }
    }

    @Override
    public void applyFromGeoId(ActionRequest request, ActionResponse response) {
        Long userId = AuthUtils.getUser().getId();
        Map<String, Object> pending = PENDING_GEO.remove(userId);
        if (pending == null) return;
        response.setValue("latitude", pending.get("latitude"));
        response.setValue("longitude", pending.get("longitude"));
        response.setValue("address", pending.get("address"));
        response.setValue("$showPossible", false);
        response.setAttr("map", "refresh", true);
    }

    @Override
    public void buildingStatistics(ActionRequest request, ActionResponse response) {

            Building building = request.getContext().asType(Building.class);

            if (building == null || building.getEntrance() == null) {
                return;
            }

            Stats stats = building.getEntrance()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(Entrance::getApartment)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .filter(Objects::nonNull)
                    .collect(Stats::new, Stats::accept, Stats::combine);

            response.setValue("$allApartments", stats.allApartments);
            response.setValue("$allResidents", stats.allResidents);
            response.setValue("$numberOfDebtors", stats.numberOfDebtors);
            response.setValue("$totalAmountOfDebt", stats.totalAmountOfDebt);

        }
    protected static class Stats {

        int allApartments = 0;
        int allResidents = 0;
        int numberOfDebtors = 0;
        int totalAmountOfDebt = 0;

        void accept(Apartment apartment) {

            allApartments++;

            if (Boolean.TRUE.equals(apartment.getDebtStatus())) {
                numberOfDebtors++;
                totalAmountOfDebt += 200;
            }

            if (apartment.getPartner() != null) {
                for (Partner partner : apartment.getPartner()) {
                    if (partner != null &&
                            "resident".equalsIgnoreCase(partner.getPartnerType())) {
                        allResidents++;
                    }
                }
            }
        }

        void combine(Stats other) {
            this.allApartments += other.allApartments;
            this.allResidents += other.allResidents;
            this.numberOfDebtors += other.numberOfDebtors;
            this.totalAmountOfDebt += other.totalAmountOfDebt;
        }
    }
}
