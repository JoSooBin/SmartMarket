package kr.co.royzero.smartmarket;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

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

import kr.co.royzero.smartmarket.model.Store;

public class BookmarkActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference bookmarkRef = database.getReference("bookmark");
    private HashMap<String, Store> listStore;

    private ListView cartList;        // 접기/펼치를 하기위한 뷰
    private BookmarkAdapter adapter;  // 즐겨찾기 어뎁터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        cartList = findViewById(R.id.cartList);

        findViewById(R.id.btnHome).setOnClickListener(this);

        listStore = new HashMap<>();
        bookmarkRef.addChildEventListener(new ChildEventListener() {
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
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnHome){
            finish();
        }
    }
    private void reloadView(){
        List<Store> stores = new ArrayList<>();
        if(listStore != null){
            for(String key:listStore.keySet()){
                Store store = listStore.get(key);
                if(store != null){
                    stores.add(store);
                }
            }
        }
        adapter = new BookmarkAdapter(bookmarkRef, stores);
        cartList.setAdapter(adapter);
    }
}
