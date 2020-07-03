package com.projects.padie.shiloah.holders;

/**
 * Created by Padie on 2/16/2018.
 */

public class Models {

    private String id, name, modelno, manufacturer_id, category_id;

    public Models(String id, String name, String modelno, String manufacturer_id, String category_id) {
        this.id = id;
        this.name = name;
        this.modelno = modelno;
        this.manufacturer_id = manufacturer_id;
        this.category_id = category_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelno() {
        return modelno;
    }

    public void setModelno(String modelno) {
        this.modelno = modelno;
    }

    public String getManufacturer_id() {
        return manufacturer_id;
    }

    public void setManufacturer_id(String manufacturer_id) {
        this.manufacturer_id = manufacturer_id;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }
}
