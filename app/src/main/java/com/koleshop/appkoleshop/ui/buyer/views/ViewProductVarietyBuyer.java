package com.koleshop.appkoleshop.ui.buyer.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.ui.common.views.ItemCountView;
import com.koleshop.appkoleshop.util.CartUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 29/01/16.
 */
public class ViewProductVarietyBuyer extends RelativeLayout {

    @Bind(R.id.icv_vpvb)
    ItemCountView itemCountView;

    private SellerSettings sellerSettings;
    private ProductVariety variety;
    private LayoutInflater inflater;
    private View v;
    private CircleImageView imageView;
    private TextView textViewQuantity, textViewPrice;//, textViewCount;
    private LinearLayout clickArea1;
    private OnClickListener clickListenerArea1;
    //private Button buttonPlus, buttonMinus;
    private boolean customerView;
    private String title;
    private boolean showTitle;

    public ViewProductVarietyBuyer(Context context) {
        super(context);
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.view_product_variety_buyer, this, true);
        imageView = (CircleImageView) v.findViewById(R.id.civ_vpvb);
        textViewQuantity = (TextView) v.findViewById(R.id.tv_vpvb_quantity);
        textViewPrice = (TextView) v.findViewById(R.id.tv_vpvb_price);
        //textViewCount = (TextView) v.findViewById(R.id.tv_vpvb_count);
        clickArea1 = (LinearLayout) v.findViewById(R.id.ll_vpvb_click_area_1);
        //buttonPlus = (Button) v.findViewById(R.id.button_vpvb_plus);
        //buttonMinus = (Button) v.findViewById(R.id.button_vpvb_minus);
        ButterKnife.bind(this, v);
    }

    public ViewProductVarietyBuyer(Context context, ProductVariety variety, boolean customerView, boolean showTitle, String title, SellerSettings sellerSettings) {
        this(context);
        this.variety = variety;
        this.customerView = customerView;
        this.showTitle = showTitle;
        this.title = title;
        this.sellerSettings = sellerSettings;
        loadTheData();
    }

    private void loadTheData() {
        if(showTitle) {
            textViewQuantity.setText(title + " " + variety.getQuantity());
        } else {
            textViewQuantity.setText(variety.getQuantity());
        }
        String price = Constants.INDIAN_RUPEE_SYMBOL + " " + variety.getPrice();
        if(price.endsWith(".0")) {
            price = price.substring(0, price.length()-2);
        }
        textViewPrice.setText(price);
        int cartCount = CartUtils.getCountOfVariety(variety, sellerSettings);
        itemCountView.setShowZeroCount(false);
        itemCountView.setOutOfStock(!variety.isLimitedStock());
        itemCountView.setCount(cartCount);
        //textViewCount.setText(cartCount + "");
    }

    public void setClickListenerArea1(OnClickListener clickListenerArea1) {
        this.clickListenerArea1 = clickListenerArea1;
        clickArea1.setOnClickListener(this.clickListenerArea1);
    }

    public void setPlusButtonClickListener(OnClickListener plusButtonClickListener) {
        itemCountView.setPlusButtonClickListener(plusButtonClickListener);
        //buttonPlus.setOnClickListener(plusButtonClickListener);
    }

    public void setMinusButtonClickListener(OnClickListener plusButtonClickListener) {
        itemCountView.setMinusButtonClickListener(plusButtonClickListener);
        //buttonMinus.setOnClickListener(plusButtonClickListener);
    }

    /*public void setCartCount(int cartCount) {
        textViewCount.setText(cartCount+"");
    }*/

}
