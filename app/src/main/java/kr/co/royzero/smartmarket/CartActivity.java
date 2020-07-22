package kr.co.royzero.smartmarket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import kr.co.royzero.smartmarket.model.CartProduct;

public class CartActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference cartRef = database.getReference("cart");

    private ListView cartList;    // 접기/펼치를 하기위한 뷰
    private CartAdapter adapter;  // 장바구니 어뎁터


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartList = findViewById(R.id.cartList);
        adapter = new CartAdapter();
        cartList.setAdapter(adapter);

        cartRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                CartProduct product = dataSnapshot.getValue(CartProduct.class);
                if(product != null){
                    product.setChecked(false);
                    adapter.setItem(new CartProduct(product.getStoreCode(), product.getPrdtCode(), product.getPrdtName(), product.getGrams(), product.getPrice(), product.getQty()));
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                CartProduct product = dataSnapshot.getValue(CartProduct.class);
                if(product != null){
                    adapter.setItem(new CartProduct(product.getStoreCode(), product.getPrdtCode(), product.getPrdtName(), product.getGrams(), product.getPrice(), product.getQty()));
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                CartProduct product = dataSnapshot.getValue(CartProduct.class);
                if(product != null){
                    adapter.removeItem(new CartProduct(product.getStoreCode(), product.getPrdtCode(), product.getPrdtName(), product.getGrams(), product.getPrice(), product.getQty()));
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        findViewById(R.id.btnChkAll).setOnClickListener(this);
        findViewById(R.id.btnChkRemove).setOnClickListener(this);
        findViewById(R.id.btnCalAll).setOnClickListener(this);
        findViewById(R.id.btnCompare).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnCompare){
            Intent intent = new Intent(CartActivity.this, CompareActivity.class);
            startActivity(intent);
        }else if(view.getId() == R.id.btnCalAll){
            if(adapter != null){
                List<CartProduct> carts = adapter.getWholeCartProduct();
                int totprice = 0;
                for(CartProduct product:carts){
                    totprice += (product.getPrice() * product.getQty());
                }
                ((TextView)findViewById(R.id.txtTotal)).setText(String.valueOf(totprice) + "원");
            }else{
                Toast.makeText(this, "장바구니에 상품이 없습니다.", Toast.LENGTH_LONG).show();
            }
        }else if(view.getId() == R.id.btnChkAll){
            adapter.setAllCheck();
            adapter.notifyDataSetChanged();
            // cartList.setAdapter(adapter);
        }else if(view.getId() == R.id.btnChkRemove){
            cartRef.setValue(adapter.removeCheckedItem());
            adapter.notifyDataSetChanged();
        }
    }
}
