package kr.co.royzero.smartmarket;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
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

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.royzero.smartmarket.model.Store;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener, MapView.POIItemEventListener {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference storeRef = database.getReference("store");
    private List<MapPOIItem> listPOIItems;
    private HashMap<String, Store> stores;
    private MapPOIItem curPoint;
    private double curLat = 37.2410448;    // 현재위도[TEST. 용인시청]
    private double curLon = 127.1779315;   // 현재경도[TEST. 용인시청]
    private Store curStore;     // 현재 선택된 마켓 정보
    private MapView mapView;    // 카카오지도API
    private RelativeLayout lyMarket;    // 현재 선택된 마켓 정보를 표시하기 위한 레이아웃
    private EditText searchTerm;
    private ListView storeList;
    private StoreAdapter adapter;
    /*
    private LocationManager lm;
    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            curLat = location.getLatitude();
            curLon = location.getLongitude();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        lyMarket = findViewById(R.id.lyMarket);

        // 카카오 맵뷰 설정
        if(mapView == null)    mapView = new MapView(this);
        ViewGroup mapViewContainer = findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setPOIItemEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);

        this.checkPermission();
        /*
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null){
            curLat = location.getLatitude();
            curLon = location.getLongitude();
        }else{
            Toast.makeText(this, "아직 현재 위치를 잡지 못했습니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show();
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener);
        */
        MapPoint curpoint = MapPoint.mapPointWithGeoCoord(curLat, curLon);
        mapView.setMapCenterPoint(curpoint, true);

        findViewById(R.id.btnSearch).setOnClickListener(this);
        findViewById(R.id.btnMarket).setOnClickListener(this);
        findViewById(R.id.lySearch).setOnClickListener(this);

        searchTerm = findViewById(R.id.searchTerm);
        searchTerm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String keyword = searchTerm.getText().toString();
                List<Store> list = new ArrayList<>();
                if(stores != null){
                    for(String storeCode:stores.keySet()){
                        Store store = stores.get(storeCode);
                        if(store.getStoreName().contains(keyword)){
                            list.add(store);
                        }
                    }
                }
                adapter = new StoreAdapter(list);
                storeList.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        storeList = findViewById(R.id.storeList);
        storeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(adapter != null){
                    Store store = (Store)adapter.getItem(position);
                    mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(store.getLatitude(), store.getLongitude()), true);

                    for(MapPOIItem item:listPOIItems){
                        if(item.getUserObject() != null && item.getUserObject() instanceof Store){
                            Store target = (Store)item.getUserObject();
                            if(target.getStoreCode().equals(store.getStoreCode())){
                                mapView.selectPOIItem(item, true);

                                curStore = store;
                                showCurStore();

                                break;
                            }
                        }
                    }

                    findViewById(R.id.lySearch).setVisibility(View.GONE);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        // 마켓정보 로드
        stores = new HashMap<>();
        storeRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Store store = dataSnapshot.getValue(Store.class);
                if(store != null){
                    stores.put(dataSnapshot.getKey(), store);

                    reloadStore();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Store store = dataSnapshot.getValue(Store.class);
                if(store != null){
                    stores.put(dataSnapshot.getKey(), store);

                    reloadStore();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                stores.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * 스토어 정보 리로드
     */
    private void reloadStore(){
        mapView.removeAllPOIItems();
        listPOIItems = new ArrayList<>();

        for(String storeCode:stores.keySet()){
            Store store = stores.get(storeCode);

            if(store != null){
                // 지도 내 마커 추가
                MapPOIItem item = this.getMarker(store);

                mapView.addPOIItem(item);
                listPOIItems.add(item);
            }
        }

        // 현재위치
        MapPoint curpoint = MapPoint.mapPointWithGeoCoord(curLat, curLon);
        mapView.addPOIItem(this.getCurrentMarker(curpoint));    // 임의의 위치로 변경
    }

    /**
     * 지도내에 핀(마커) 추가
     * @param store 스토어 정보
     */
    private MapPOIItem getMarker(Store store){
        // 마커
        MapPOIItem mapPOIItem = new MapPOIItem();
        mapPOIItem.setItemName(store.getStoreName());
        mapPOIItem.setTag(0);
        mapPOIItem.setMapPoint(MapPoint.mapPointWithGeoCoord(store.getLatitude(), store.getLongitude()));
        mapPOIItem.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        mapPOIItem.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin);
        mapPOIItem.setDraggable(false);// true = 핀을 꾸욱 눌러서 이동시킬 수 있습니다. default : false
        mapPOIItem.setUserObject(store);

        return mapPOIItem;
    }

    /**
     * 지도내 현재 위치 핀으로 표시
     * @param mapPoint 현재위치
     */
    private MapPOIItem getCurrentMarker(MapPoint mapPoint){
        // 마커
        MapPOIItem mapPOIItem = new MapPOIItem();
        mapPOIItem.setItemName("현재위치");
        mapPOIItem.setTag(0);
        mapPOIItem.setMapPoint(mapPoint);
        mapPOIItem.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 BluePin 마커 모양.
        mapPOIItem.setDraggable(true);// true = 핀을 꾸욱 눌러서 이동시킬 수 있습니다. default : false

        return mapPOIItem;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnMarket){
            if(curStore != null){
                Intent intent = new Intent(SearchActivity.this, MarketActivity.class);
                intent.putExtra("storeCode", curStore.getStoreCode());
                startActivity(intent);
            }else{
                Toast.makeText(this, "선택된 마켓이 없습니다.", Toast.LENGTH_LONG).show();
            }
        }else if(view.getId() == R.id.btnSearch){
            findViewById(R.id.lySearch).setVisibility(View.VISIBLE);
        }else if(view.getId() == R.id.lySearch){
            findViewById(R.id.lySearch).setVisibility(View.GONE);
        }
    }

    private void showCurStore(){
        // 거리계산[Start]
        Location cpoint = new Location("Current");
        cpoint.setLatitude(curLat);
        cpoint.setLongitude(curLon);

        Location spoint = new Location("Store");
        spoint.setLatitude(curStore.getLatitude());
        spoint.setLongitude(curStore.getLongitude());

        String distance = Math.round(cpoint.distanceTo(spoint) / 100f) / 10f + "km";   // 소수점 1의자리까지만 표기(km)
        // 거리계산[End]

        ((TextView)lyMarket.findViewById(R.id.txtName)).setText(curStore.getStoreName());   // 매장명
        ((TextView)lyMarket.findViewById(R.id.txtGbn)).setText(curStore.getStoreType());    // 매장구분
        ((TextView)lyMarket.findViewById(R.id.txtDist)).setText(distance);   // 매장과의 거리
        ((TextView)lyMarket.findViewById(R.id.txtAddr)).setText(curStore.getStoreAddr());   // 매장주소
        ((TextView)lyMarket.findViewById(R.id.txtTime)).setText(curStore.getStoreTime());   // 영업시간

        lyMarket.setVisibility(View.VISIBLE);
    }
    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        if(mapPOIItem.getUserObject() != null && mapPOIItem.getUserObject() instanceof Store){
            curStore = (Store)mapPOIItem.getUserObject();
            // Toast.makeText(this, curStore.getStoreName(), Toast.LENGTH_LONG).show();

            this.showCurStore();
        }
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    /**
     * 권한관리
     */
    @TargetApi(value = Build.VERSION_CODES.JELLY_BEAN)
    private void checkPermission(){
        String[] permission_list = {
                Manifest.permission.INTERNET
                , Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.ACCESS_COARSE_LOCATION
        };
        //현재 안드로이드 버전이 6.0미만이면 메서드를 종료한다.
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)    return;

        for(String permission : permission_list){
            //권한 허용 여부를 확인한다.
            int chk = checkCallingOrSelfPermission(permission);

            if(chk == PackageManager.PERMISSION_DENIED){
                //권한 허용을여부를 확인하는 창을 띄운다
                requestPermissions(permission_list,0);
            }
        }
    }
}
