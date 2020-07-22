package kr.co.royzero.smartmarket.model;

/**
 * 상품정보
 */
public class Product {
    private String storeCode;
    private String prdtCode;
    private String prdtName;
    private String grams;
    private int price;
    public Product(){}
    public Product(String storeCode, String prdtCode, String prdtName, String grams, int price){
        this();

        this.storeCode = storeCode;
        this.prdtCode = prdtCode;
        this.prdtName = prdtName;
        this.grams = grams;
        this.price = price;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getPrdtCode() {
        return prdtCode;
    }

    public void setPrdtCode(String prdtCode) {
        this.prdtCode = prdtCode;
    }

    public String getPrdtName() {
        return prdtName;
    }

    public void setPrdtName(String prdtName) {
        this.prdtName = prdtName;
    }

    public String getGrams() {
        return grams;
    }

    public void setGrams(String grams) {
        this.grams = grams;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
