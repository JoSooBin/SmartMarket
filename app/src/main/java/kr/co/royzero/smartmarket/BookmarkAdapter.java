package kr.co.royzero.smartmarket;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.List;

import kr.co.royzero.smartmarket.model.Store;

public class BookmarkAdapter extends BaseAdapter {
    private DatabaseReference bookmarkRef;
    private List<Store> storeList;

    public BookmarkAdapter(DatabaseReference bookmarkRef, List<Store> storeList){
        this.bookmarkRef = bookmarkRef;
        this.storeList = storeList;
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
            view = inflater.inflate(R.layout.list_bookmark, parent, false);
        }

        int logoId = R.mipmap.ic_launcher;
        if(store.getStoreName().contains("이마트"))    logoId = R.drawable.ic_emart;
        if(store.getStoreName().contains("홈플러스"))   logoId = R.drawable.ic_homeplus;
        if(store.getStoreName().contains("하나로"))    logoId = R.drawable.ic_hanaromart;
        if(store.getStoreName().contains("롯데마트"))   logoId = R.drawable.ic_lottemart;
        ImageView logo = view.findViewById(R.id.imgLogo);
        logo.setImageResource(logoId);

        TextView txtName =  view.findViewById(R.id.txtName);
        txtName.setText(store.getStoreName());
        txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MarketActivity.class);
                intent.putExtra("storeCode", store.getStoreCode());
                context.startActivity(intent);
            }
        });

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