package kr.co.royzero.smartmarket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.royzero.smartmarket.model.CartProduct;
import kr.co.royzero.smartmarket.model.Store;

public class CompareActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference storeRef = database.getReference("store");
    private DatabaseReference cartRef = database.getReference("cart");
    private DatabaseReference bookmarkRef = database.getReference("bookmark");
    private HashMap<String, Store> listStore;
    private HashMap<String, CartProduct> listCart;

    private ListView cartList;    // 접기/펼치를 하기위한 뷰
    private CompareAdapter adapter;  // 장바구니 어뎁터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        cartList = findViewById(R.id.cartList);

        findViewById(R.id.btnHome).setOnClickListener(this);

        listCart = new HashMap<>();
        cartRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                CartProduct product = dataSnapshot.getValue(CartProduct.class);
                if(product != null){
                    product.setChecked(false);
                    listCart.put(product.getPrdtCode(), product);
                }

                reloadView();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                CartProduct product = dataSnapshot.getValue(CartProduct.class);
                if(product != null)    listCart.put(product.getPrdtCode(), product);

                reloadView();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                CartProduct product = dataSnapshot.getValue(CartProduct.class);
                if(product != null)    listCart.remove(product.getPrdtCode());

                reloadView();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listStore = new HashMap<>();
        storeRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Store store = dataSnapshot.getValue(Store.class);
                if(store != null){
                    listStore.put(dataSnapshot.getKey(), store);

                    reloadView();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Store store = dataSnapshot.getValue(Store.class);
                if(store != null){
                    listStore.put(dataSnapshot.getKey(), store);

                    reloadView();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                listStore.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Toast.makeText(this, "꼭 상세 목록을 확인하십시오", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnHome){
            startActivity(new Intent(CompareActivity.this, MainActivity.class));
        }
    }

    private void reloadView(){
        List<CartProduct> carts = new ArrayList<>();
        if(listCart != null){
            for(String key:listCart.keySet()){
                CartProduct cart = listCart.get(key);
                if(cart != null){
                    carts.add(cart);
                }
            }
        }
        List<Store> stores = new ArrayList<>();
        if(listStore != null){
            for(String key:listStore.keySet()){
                Store store = listStore.get(key);
                if(store != null){
                    stores.add(store);
                }
            }
        }
        adapter = new CompareAdapter(bookmarkRef, carts, stores);
        cartList.setAdapter(adapter);
    }
}
