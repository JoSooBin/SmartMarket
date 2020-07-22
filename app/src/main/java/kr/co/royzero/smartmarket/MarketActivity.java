package kr.co.royzero.smartmarket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import kr.co.royzero.smartmarket.model.CartProduct;
import kr.co.royzero.smartmarket.model.Product;
import kr.co.royzero.smartmarket.model.Store;

public class MarketActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference storeRef = database.getReference("store");
    private DatabaseReference cartRef = database.getReference("cart");
    private Store store;

    private ListView cartList;    // 접기/펼치를 하기위한 뷰
    private ProductAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        cartList = findViewById(R.id.cartList);

        String storeCode = getIntent().getStringExtra("storeCode");
        if(storeCode != null){
            storeRef.child(storeCode).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    store = dataSnapshot.getValue(Store.class);

                    reloadView();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

        findViewById(R.id.btnCart).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnCart){
            if(adapter == null){
                Toast.makeText(this, "장바구니에 담을 상품이 없습니다.", Toast.LENGTH_LONG).show();
            }else{
                List<CartProduct> list = adapter.getWholeCartProduct();
                List<CartProduct> carts = new ArrayList<>();
                for(CartProduct product:list){
                    if(product.getQty() > 0){
                        carts.add(product);
                    }
                }

                if(carts.size() > 0){
                    cartRef.setValue(carts);
                    Toast.makeText(this, "장바구니에 담겼습니다.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(MarketActivity.this, CartActivity.class);
                    intent.putExtra("storeCode", store.getStoreCode());
                    startActivity(intent);
                }else{
                    Toast.makeText(this, "선택된 상품이 없습니다.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void reloadView(){
        ((TextView)findViewById(R.id.txtName)).setText(store.getStoreName());    // 마켓명

        List<CartProduct> data = new ArrayList<>();
        if(store.getProductList() != null){
            for(String key:store.getProductList().keySet()){
                Product product = store.getProductList().get(key);
                if(product != null){
                    data.add(new CartProduct(product.getStoreCode(), product.getPrdtCode(), product.getPrdtName(), product.getGrams(), product.getPrice(), 1));
                }
            }
        }
        adapter = new ProductAdapter(data);
        cartList.setAdapter(adapter);
    }
}
