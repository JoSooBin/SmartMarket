package kr.co.royzero.smartmarket;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import kr.co.royzero.smartmarket.model.Product;
import kr.co.royzero.smartmarket.model.Store;

public class MarketClient {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference storeRef = database.getReference("store");

    private final static String TAG_NAME = "MarketClient";
    private final static String apiMarket = "http://openapi.seoul.go.kr:8088/4e66474658776f723130306a6e707770/json/Mgismulgainfo/%d/%d/";

    /**
     * 서울시데이터
     */
    public void loadStore() {
        String urlString = String.format(apiMarket, 1, 1);
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            in = new BufferedInputStream(conn.getInputStream());
            JSONObject json = new JSONObject(getStringFromInputStream(in));

            if(json.has("Mgismulgainfo")){
                int total = json.getJSONObject("Mgismulgainfo").getInt("list_total_count");

                // 전채로 재호출
                in.close();
                conn.disconnect();

                urlString = String.format(apiMarket, 1, total);
                url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                in = new BufferedInputStream(conn.getInputStream());
                json = new JSONObject(getStringFromInputStream(in));

                if(json.has("Mgismulgainfo")) {
                    JSONObject info = json.getJSONObject("Mgismulgainfo");
                    JSONArray list = info.getJSONArray("row");

                    storeRef.removeValue();
                    ArrayList<Store> stores = new ArrayList<>();
                    for(int i = 0; i < list.length(); i++){
                        JSONObject obj = list.getJSONObject(i);

                        String logoId = "ic_market";
                        if(obj.getString("COT_CONTS_NAME").contains("이마트"))    logoId = "ic_emart";
                        if(obj.getString("COT_CONTS_NAME").contains("홈플러스"))   logoId = "ic_homeplus";
                        if(obj.getString("COT_CONTS_NAME").contains("하나로"))    logoId = "ic_hanaromart";
                        if(obj.getString("COT_CONTS_NAME").contains("롯데마트"))   logoId = "ic_lottemart";

                        Store store = new Store(
                                obj.getString("COT_CONTS_ID")         // storeCode
                                , obj.getString("COT_CONTS_NAME")       // storeName
                                , obj.getString("COT_KW")               // storeType
                                , obj.getString("COT_ADDR_FULL_OLD")    // storeAddr
                                , obj.getString("COT_TEL_NO")           // storeTime
                                , obj.getDouble("COT_COORD_Y")          // latitude
                                , obj.getDouble("COT_COORD_X")          // longtitude
                                , logoId);

                        HashMap<String, Product> map = new HashMap<>();
                        for(int j = 0; j < 20; j++){
                            String idx = String.valueOf((j + 1));
                            if(j < 9)    idx = "0" + idx;

                            String name = obj.getString("COT_NAME_" + idx);
                            String value = obj.getString("COT_VALUE_" + idx);

                            if(name != null && !name.equals("") && value != null && !value.equals("")){
                                int begin = value.indexOf("가격 : ") + 5;
                                int end = value.indexOf("원", begin);

                                String price = value.substring(begin, end);
                                String grams = value.substring(value.indexOf("(", end) + 1, value.indexOf(")", end));
                                Product product = new Product(
                                        store.getStoreCode()      // storeCode
                                        , name                      // prdtCode
                                        , name                      // prdtName
                                        , grams                     // grams
                                        , Integer.parseInt(price));
                                map.put(product.getPrdtCode().replace(".", "_"), product);
                            }
                        }
                        store.setProductList(map);

                        storeRef.child(store.getStoreCode()).setValue(store);
                        stores.add(store);
                    }
                }else{
                    Log.e(TAG_NAME, json.toString());
                }
            }else{
                Log.e(TAG_NAME, json.toString());
            }
            // store = parseJSON(json);
        } catch (MalformedURLException e) {
            Log.e("MarketClient", "Malformed URL");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("MarketClient", "JSON parsing error");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("MarketClient", "URL Connection failed");
            e.printStackTrace();
        } finally {
            if(in != null) try { in.close(); } catch(Exception e){}
            if(conn != null) try { conn.disconnect(); } catch(Exception e){}
        }
    }

    /**
     * 용인시데이터
     */
    public void loadStore02() {
        storeRef.removeValue();
        ArrayList<Store> stores = new ArrayList<>();

        stores.add(new Store("S0001", "농협 파머스마켓 용인점", "처인구", "경기 용인시 처인구 금령로55번길 3", "031-321-7100", 37.237361, 127.201766, "ic_hanaromart"));
        stores.add(new Store("S0002", "GS슈퍼 용인점", "처인구", "경기도 용인시 처인구 금령로55번길 12 LG수퍼마켓", "031-337-2033", 37.255793, 127.199047, "ic_market"));
        stores.add(new Store("S0003", "원삼농협하나로마트 본점", "처인구", "경기도 용인시 처인구 원삼면 고당로 24", "", 37.165023, 127.309725, "ic_hanaromart"));
        stores.add(new Store("S0004", "이동농협하나로마트천리점", "처인구", "경기도 용인시 처인구 이동읍 백옥대로 595", "031-336-4422", 37.19012, 127.205721, "ic_hanaromart"));
        stores.add(new Store("S0005", "파머스마켓양지점", "처인구", "경기도 용인시 처인구 양지면 양지로133번길 3", "", 37.23511, 127.282705, "ic_market"));
        stores.add(new Store("S0006", "럭키마트", "처인구", "경기도 용인시 처인구 백옥대로 1384", "031-337-0744", 37.258605, 127.212998, "ic_market"));
        stores.add(new Store("S0007", "한국유통 동부점", "처인구", "경기도 용인시 처인구 금령로173번길 6", "031-321-1357", 37.234934, 127.215741, "ic_market"));
        stores.add(new Store("S0008", "GS슈퍼마켓 신갈점", "기흥구", "경기도 용인시 기흥구 신구로12번길 23", "031-281-4477 ", 37.274769, 127.111441, "ic_market"));
        stores.add(new Store("S0009", "구성농협하나로마트", "기흥구", "경기 용인시 기흥구 구성로 40", "031-283-9483", 37.294822, 127.11418, "ic_hanaromart"));
        stores.add(new Store("S0010", "이마트 동백점", "기흥구", "경기도 용인시 기흥구 동백죽전대로 444", "031-546-1234", 37.27824, 127.151573, "ic_emart"));
        stores.add(new Store("S0011", "하나로클럽기흥점", "기흥구", "경기도 용인시 기흥구 민속촌로 6", "031-284-2097", 37.255217, 127.105615, "ic_hanaromart"));
        stores.add(new Store("S0012", "이마트 보라점", "기흥구", "경기도 용인시 기흥구 한보라1로 92 보라지구E-MART", "031-8009-1234", 37.247789, 127.105036, "ic_emart"));
        stores.add(new Store("S0013", "이마트 죽전점", "수지구", "경기도 용인시 수지구 포은대로 552", "031-888-1234", 37.325596, 127.110011, "ic_emart"));
        stores.add(new Store("S0014", "이마트 수지점", "수지구", "경기도 용인시 수지구 수지로 203", "031-270-1234", 37.320013, 127.083336, "ic_emart"));
        stores.add(new Store("S0015", "한세유통", "수지구", "경기도 용인시 수지구 수지로342번길 15", "031-265-4411", 37.327246, 127.096119, "ic_market"));

        ArrayList<String[]> rowdata = new ArrayList<>();
        rowdata.add(new String[]{"3717", "3380", "5480", "28000", "2570", "1145", "2580", "3290", "6600", "6950", "8840", "7900", "930", "1500", "1400", "7420", "1410", "1230", "1850", "12650", "4200", "1470", "11500", "29000", "13200", "850", "5800", "9900", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"3750", "3380", "5990", "27500", "2650", "1300", "3354", "3480", "6580", "7050", "10980", "8900", "1950", "1880", "1580", "6750", "1560", "1210", "1970", "8580", "5700", "1500", "8900", "25000", "12573", "1050", "8780", "10500", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"3750", "3500", "5950", "31000", "2600", "1100", "2500", "3480", "6600", "5950", "8800", "7900", "1800", "1700", "1400", "4600", "1410", "1230", "1350", "8900", "6500", "1160", "5900", "28000", "16900", "950", "7200", "7400", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"3750", "3380", "5880", "25800", "2570", "1200", "2200", "3350", "4580", "3980", "5390", "7900", "1780", "1580", "1100", "7500", "1460", "1230", "1700", "8900", "5900", "2300", "8600", "26000", "16000", "1100", "8600", "11700", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"3500", "3500", "5480", "26900", "2570", "1300", "2300", "3347", "6400", "6700", "8790", "8700", "1850", "1620", "1450", "7750", "1410", "1230", "1800", "9100", "4900", "1890", "7650", "27800", "17700", "920", "8600", "10300", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"3600", "3400", "3500", "20000", "2600", "1100", "2300", "3400", "4720", "5250", "7550", "4200", "1530", "1990", "1350", "7150", "1600", "1300", "1700", "16400", "7400", "2680", "7800", "23800", "18800", "800", "3600", "4100", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"2700", "3600", "3600", "23000", "2800", "1300", "2800", "3750", "5200", "4980", "9000", "9800", "1800", "2300", "2000", "5300", "1650", "1400", "1300", "9800", "3750", "2200", "8500", "23500", "9800", "1200", "7000", "10800", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"3333", "3550", "6300", "27800", "2700", "1300", "3630", "3600", "6000", "3980", "9980", "8250", "1850", "1850", "1500", "7980", "1700", "1280", "1200", "8690", "2500", "1200", "5700", "24300", "17133", "1050", "7453", "11800", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"2200", "3660", "5480", "29900", "2510", "2980", "4500", "2200", "7500", "4600", "8950", "7950", "1870", "1500", "1500", "7200", "1330", "2100", "2500", "11900", "6900", "5100", "5750", "34400", "15900", "990", "8000", "8400", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"3400", "3380", "5980", "19400", "2570", "1250", "2480", "2680", "5980", "6880", "8780", "8900", "930", "1680", "1680", "6680", "1240", "1150", "1180", "11450", "3900", "2630", "11500", "24500", "15900", "1020", "8300", "10700", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"1750", "3473", "5580", "54900", "2570", "1245", "2430", "6000", "7610", "3720", "129550", "9410", "1780", "1680", "1460", "7650", "1510", "1240", "14550", "86440", "3450", "1180", "8400", "21400", "12000", "980", "10200", "9900", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"3950", "3380", "5980", "22400", "2570", "1056", "2422", "3480", "7250", "6950", "9580", "8900", "1440", "1680", "1280", "7450", "1382", "1190", "1279", "7936", "2100", "2159", "8412", "26784", "21233", "1020", "8300", "6600", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"3323", "2947", "5480", "21333", "2570", "675", "1750", "3480", "7250", "5480", "8780", "8900", "1440", "1630", "1280", "7433", "1410", "1190", "1279", "7960", "3004", "1873", "8433", "24079", "14767", "1020", "7419", "10300", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"1980", "3380", "4400", "29700", "2570", "1250", "2560", "3480", "5980", "6950", "8350", "7950", "1500", "1630", "1680", "7430", "1810", "1190", "1600", "15900", "2450", "1500", "9900", "25800", "15900", "950", "4750", "11500", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});
        rowdata.add(new String[]{"3250", "4200", "4200", "26000", "2967", "1100", "2400", "3300", "4533", "5700", "6800", "8000", "1750", "2400", "3200", "8000", "1500", "1200", "2300", "15000", "4750", "3200", "8500", "21000", "17000", "1100", "8000", "12000", "풀무원국산콩옛맛두부(찌개용)1모", "농심신라면1번들(5개입)", "제일제당스팸 340g", "남양임페리얼XO800g 1캔", "서울우유 1리터", "남양불가리스프라임150ml", "동원참치 라이트스텐다드 165g", "델몬트오렌지100%1.5L", "오뚜기(고소한)320mL", "해표(콩기름)식용류 1.8L", "제일제당 쇠고기다시다 500g", "샘표진간장 F-3 1.8L", "미원 맛소금500g", "백설 하얀설탕1kg", "백설중력분밀가루1kg", "맥심오리지날커피믹스50입(600g)", "하이트 500ml 1병", "진로참이슬360ml 1병", "도브모이스쳐밀크(100g)", "제일제당 비트 3.2kg", "피죤핑크2100ml", "LG페리오 일반용(150g)", "덴트롤두피케어600g", "하기스보송보송대형 60p", "깨끗한나라(55m)24롤", "농심 새우깡", "순창 찰고추장 500g", " 화이트중형(날개형)36p"});

        for(int i = 0; i < stores.size(); i++){
            Store store = stores.get(i);

            HashMap<String, Product> map = new HashMap<>();
            String[] data = rowdata.get(i);

            for(int j = 0; j < data.length / 2; j++){
                String prdtCode = "PRDT" + j;
                String prdtName = data[data.length / 2 + j];
                String price = data[j];
                Product product = new Product(
                        store.getStoreCode()      // storeCode
                        , prdtCode                // prdtCode
                        , prdtName                // prdtName
                        , ""               // grams
                        , Integer.parseInt(price));
                map.put(prdtCode, product);
            }
            store.setProductList(map);

            storeRef.child(store.getStoreCode()).setValue(store);
        }
    }

    private String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}