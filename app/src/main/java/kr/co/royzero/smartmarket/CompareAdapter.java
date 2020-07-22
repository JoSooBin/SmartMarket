package kr.co.royzero.smartmarket;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.co.royzero.smartmarket.model.CartProduct;
import kr.co.royzero.smartmarket.model.Product;
import kr.co.royzero.smartmarket.model.Store;

public class CompareAdapter extends BaseAdapter {
    private DatabaseReference bookmarkRef;
    private Store curStore;
    private List<CartProduct> cartList = new ArrayList<>();
    private List<Store> storeList = new ArrayList<>();

    public CompareAdapter(DatabaseReference bookmarkRef, List<CartProduct> cartList, List<Store> storeList){
        this.bookmarkRef = bookmarkRef;
        this.cartList = cartList;

        // 현재 장바구니의 스토어 코드 추출
        String storeCode = null;
        if(cartList != null){
            for(CartProduct product:cartList){
                storeCode = product.getStoreCode();
                break;
            }
        }

        // 현재 장바구니의 스토어 정보 제외
        List<Store> list = new ArrayList<>();
        if(storeCode != null && storeList != null){
            for(Store store:storeList){
                if(storeCode.equals(store.getStoreCode())){
                    this.curStore = store;
                    break;
                }
            }
            if(curStore != null){
                for(Store store:storeList){
                    if(!storeCode.equals(store.getStoreCode())){
                        Location cpoint = new Location("Current");
                        cpoint.setLatitude(curStore.getLatitude());
                        cpoint.setLongitude(curStore.getLongitude());

                        Location spoint = new Location("Store");
                        spoint.setLatitude(store.getLatitude());
                        spoint.setLongitude(store.getLongitude());

                        float distance = Math.round(cpoint.distanceTo(spoint) / 100f) / 10f;   // 소수점 1의자리까지만 표기(km)
                        if(distance <= 10f){
                            int price = 0;
                            if(this.cartList != null){
                                for(CartProduct cart:this.cartList){
                                    Product product = store.getProductList().get(cart.getPrdtCode());
                                    if(product != null){
                                        price += (product.getPrice() * cart.getQty());
                                    }
                                }
                            }
                            store.setDistance(distance);
                            store.setTotPrice(price);
                            list.add(store);
                        }
                    }
                }

                Collections.sort(list);
                this.storeList = list;
            }
        }
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return this.storeList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Context context = parent.getContext();
        final Store store = storeList.get(position);

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_compare, parent, false);
        }

        NumberFormat formatter01 = new DecimalFormat("#,###");
        String totPrice = formatter01.format(store.getTotPrice());
        NumberFormat formatter02 = new DecimalFormat("#,###.#");
        String distance = formatter02.format(store.getDistance());

        int logoId = R.mipmap.ic_launcher;
        if(store.getStoreName().contains("이마트"))    logoId = R.drawable.ic_emart;
        if(store.getStoreName().contains("홈플러스"))   logoId = R.drawable.ic_homeplus;
        if(store.getStoreName().contains("하나로"))    logoId = R.drawable.ic_hanaromart;
        if(store.getStoreName().contains("롯데마트"))   logoId = R.drawable.ic_lottemart;
        ImageView logo = view.findViewById(R.id.imgLogo);
        logo.setImageResource(logoId);

        ((TextView)view.findViewById(R.id.txtName)).setText(store.getStoreName());
        ((TextView)view.findViewById(R.id.txtPrice)).setText(totPrice + "원");
        ((TextView)view.findViewById(R.id.txtDist)).setText(distance + "km");

        final ImageButton btnBookmark = view.findViewById(R.id.btnBookmark);
        bookmarkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                store.setBookmarkYn(snapshot.hasChild(store.getStoreCode()));
                if (store.isBookmarkYn()) {
                    btnBookmark.setImageResource(R.drawable.ic_star_on);
                }else{
                    btnBookmark.setImageResource(R.drawable.ic_star_off);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(bookmarkRef.child(store.getStoreCode()).getRef() != null){
            btnBookmark.setImageResource(R.drawable.ic_star_on);
        }
        btnBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(store != null){
                    store.setBookmarkYn(!store.isBookmarkYn());
                    if(store.isBookmarkYn()){
                        bookmarkRef.child(store.getStoreCode()).setValue(store);
                        btnBookmark.setImageResource(R.drawable.ic_star_on);
                    }else{
                        bookmarkRef.child(store.getStoreCode()).removeValue();
                        btnBookmark.setImageResource(R.drawable.ic_star_off);
                    }
                }
            }
        });

        return view;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return this.storeList.get(position) ;
    }

    // Get Resource
    public int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}