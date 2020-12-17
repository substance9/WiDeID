package edu.uci.ics.deid.model;

public class Space {
    int space_id;
    String name;
    String space_type;
    int building_id;
    int floor_id;
    int region_id;//for semantic location

    public int getSpace_id() {
        return this.space_id;
    }

    public int getRegion_id() {
        return this.region_id;
    }

    public void setSpace_id(int space_id) {
        this.space_id = space_id;
    }

    public void setRegion_id(int region_id) {
        this.region_id = region_id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpace_type() {
        return this.space_type;
    }

    public void setSpace_type(String space_type) {
        this.space_type = space_type;
    }

    public int getBuilding_id() {
        return this.building_id;
    }

    public void setBuilding_id(int building_id) {
        this.building_id = building_id;
    }

    public int getFloor_id() {
        return this.floor_id;
    }

    public void setFloor_id(int floor_id) {
        this.floor_id = floor_id;
    }

}