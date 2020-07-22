package kr.co.royzero.smartmarket;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.co.royzero.smartmarket.model.CartProduct;

public class CartAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private List<CartProduct> list;
    private Set<String> chkList;

    public CartAdapter(){
        this.list = new ArrayList<>();
        this.chkList = new HashSet<>();
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return list.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final RecyclerView.ViewHolder holder;
        final int pos = position;
        final Context context = parent.getContext();
        final CartProduct product = list.get(pos);

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_cart, parent, false);
        }
        final TextView txtQty = view.findViewById(R.id.qty);

        final CheckBox checked = view.findViewById(R.id.cartChk);
        checked.setChecked(chkList.contains(product.getPrdtCode()));
        checked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checked.isChecked())     chkList.add(product.getPrdtCode());
                else                        chkList.remove(product.getPrdtCode());
            }
        });
        ((TextView)view.findViewById(R.id.prdtName)).setText(product.getPrdtName());
        ((TextView)view.findViewById(R.id.price)).setText(String.valueOf(product.getPrice()));
        txtQty.setText(String.valueOf(product.getQty()));
        txtQty.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                int qty = 0;
                if(!txtQty.getText().toString().isEmpty()){
                    qty = Integer.parseInt(txtQty.getText().toString());
                }
                list.get(pos).setQty(qty);
                txtQty.setText(String.valueOf(qty));
                return false;
            }
        });
        view.findViewById(R.id.btnMinus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = list.get(pos).getQty();
                if(qty > 0){
                    qty--;
                }else{
                    Log.d("SMARTMARKET", "No More Decreased");
                }
                list.get(pos).setQty(qty);
                txtQty.setText(String.valueOf(qty));
            }
        });
        view.findViewById(R.id.btnPlus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = list.get(pos).getQty();
                qty++;
                list.get(pos).setQty(qty);
                txtQty.setText(String.valueOf(qty));
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
        return list.get(position) ;
    }

    public void setItem(CartProduct product){
        boolean exists = false;
        if(this.list == null)   list = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            if(product.getPrdtCode().equals(list.get(i).getPrdtCode())){
                exists = true;
                list.set(i, product);
            }
        }

        if(!exists)    list.add(product);
    }

    public void removeItem(CartProduct product){
        for(int i = 0; i < list.size(); i++){
            if(product.getPrdtCode().equals(list.get(i).getPrdtCode())){
                list.remove(i);
                break;
            }
        }
    }

    public List<CartProduct> removeCheckedItem(){
        List<CartProduct> data = new ArrayList<>();
        for(CartProduct product:list){
            if(chkList != null && chkList.contains(product.getPrdtCode())){
                // 삭제되므로 스킵
            }else{
                data.add(product);
            }
        }
        list = data;
        return list;
    }

    public void setAllCheck(){
        for(CartProduct prdt:list){
            this.chkList.add(prdt.getPrdtCode());
        }
    }

    /**
     * 카트의 상품 목록 반환
     * @return
     */
    public List<CartProduct> getWholeCartProduct(){
        return list;
    }
}