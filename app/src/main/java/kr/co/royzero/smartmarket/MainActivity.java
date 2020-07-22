package kr.co.royzero.smartmarket;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 메인화면
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnSearch).setOnClickListener(this);
        findViewById(R.id.btnBookmark).setOnClickListener(this);
        findViewById(R.id.btnCart).setOnClickListener(this);
        findViewById(R.id.btnInfo).setOnClickListener(this);

        this.checkPermission();

        // this.initDb();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnSearch:this.moveToActivity(SearchActivity.class);break;        // 매장검색
            case R.id.btnBookmark:this.moveToActivity(BookmarkActivity.class);break;    // 즐겨찾기
            case R.id.btnCart:this.moveToActivity(CartActivity.class);break;            // 장바구니
            case R.id.btnInfo:this.moveToActivity(InfoActivity.class);break;            // 개발자정보
        }
    }

    /**
     * 화면이동
     * @param target 이동할 Activity
     */
    private void moveToActivity(Class target){
        startActivity(new Intent(MainActivity.this, target));
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

    /**
     * RealtimeDatabase 초기화
     */
    private void initDb(){
        initStore();
    }

    /**
     * 마켓정보 초기화
     */
    private void initStore(){
        Date cDate = new Date();
        String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);

        SharedPreferences ref = this.getSharedPreferences("MYPREFRENCE", Context.MODE_PRIVATE);
        String syncDt = ref.getString("SYNC_DT", null);
        if(true || syncDt == null || !syncDt.equals(fDate)){
            // 하루가 지난 경우에만 갱신
            new Thread() {
                public void run() {
                    MarketClient client = new MarketClient();
                    client.loadStore02();

                    Date cDate = new Date();
                    String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
                    SharedPreferences ref = getSharedPreferences("MYPREFRENCE", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = ref.edit();
                    editor.putString("SYNC_DT", fDate);
                    editor.commit();
                }
            }.start();
        }
    }
}
