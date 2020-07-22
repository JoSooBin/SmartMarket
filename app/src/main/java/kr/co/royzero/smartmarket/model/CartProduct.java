package kr.co.royzero.smartmarket.model;

/**
 * 상품정보
 */
public class CartProduct extends Product {
    private boolean checked;
    private int qty;
    public CartProduct(){};
    public CartProduct(String storeCode, String prdtCode, String prdtName, String grams, int price, int qty){
        super(storeCode, prdtCode, prdtName, grams, price);
        this.qty = qty;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }
}
