package com.marteldelfer.teststore.models;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private List<Integer> indexList;
    private List<Integer> quantityList;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public List<Integer> getIndexList() {
        return indexList;
    }
    public void setIndexList(List<Integer> indexList) {
        this.indexList = indexList;
    }
    public List<Integer> getQuantityList() {
        return quantityList;
    }
    public void setQuantityList(List<Integer> quantityList) {
        this.quantityList = quantityList;
    }    
}
