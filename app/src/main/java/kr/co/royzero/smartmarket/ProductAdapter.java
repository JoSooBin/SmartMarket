package kr.co.royzero.smartmarket;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import kr.co.royzero.smartmarket.model.CartProduct;

public class ProductAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private List<CartProduct> list;

    public ProductAdapter(List<CartProduct> list){
        this.list = list;
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return list.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_product, parent, false);
        }
        final TextView txtQty = view.findViewById(R.id.qty);

        CartProduct product = list.get(position);

        ((TextView)view.findViewById(R.id.prdtName)).setText(product.getPrdtName());
        if(product.getGrams().equals("")){
            view.findViewById(R.id.grams).setVisibility(View.GONE);
        }else{
            ((TextView)view.findViewById(R.id.grams)).setText("(" + product.getGrams() + ")");
        }
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

    /**
     * 카트의 상품 목록 반환
     * @return
     */
    public List<CartProduct> getWholeCartProduct(){
        return list;
    }
}