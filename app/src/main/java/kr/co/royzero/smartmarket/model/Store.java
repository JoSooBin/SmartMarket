package kr.co.royzero.smartmarket.model;

import java.util.HashMap;

public class Store implements Comparable<Store>{
    private String storeCode;
    private String storeName;
    private String storeType;
    private String storeAddr;
    private String storeTime;
    private double latitude;
    private double longitude;
    private float distance;
    private int totPrice;
    private boolean bookmarkYn;
    private String logoId;
    private HashMap<String, Product> productList;
    public Store(){}
    public Store(String storeCode, String storeName, String storeType, String storeAddr, String storeTime, double latitude, double longitude, String logoId){
        this();

        this.storeCode = storeCode;
        this.storeName = storeName;
        this.storeType = storeType;
        this.storeAddr = storeAddr;
        this.storeTime = storeTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.logoId = logoId;
    }
    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getStoreAddr() {
        return storeAddr;
    }

    public void setStoreAddr(String storeAddr) {
        this.storeAddr = storeAddr;
    }

    public String getStoreTime() {
        return storeTime;
    }

    public void setStoreTime(String storeTime) {
        this.storeTime = storeTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public HashMap<String, Product> getProductList() { return productList; }

    public void setProductList(HashMap<String, Product> productList) { this.productList = productList; }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getTotPrice() {
        return totPrice;
    }

    public void setTotPrice(int totPrice) {
        this.totPrice = totPrice;
    }

    public boolean isBookmarkYn() {
        return bookmarkYn;
    }

    public void setBookmarkYn(boolean bookmarkYn) {
        this.bookmarkYn = bookmarkYn;
    }

    public String getLogoId() {
        return logoId;
    }

    public void setLogoId(String logoId) {
        this.logoId = logoId;
    }

    @Override
    public int compareTo(Store s) {
        if (this.totPrice < s.getTotPrice()) {
            return -1;
        } else if (this.totPrice > s.getTotPrice()) {
            return 1;
        }
        return 0;
    }
}
