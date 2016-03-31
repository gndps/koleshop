package com.koleshop.appkoleshop.ui.seller.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koleshop.appkoleshop.listeners.InventoryProductClickListener;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.ProductUtil;
import com.koleshop.appkoleshop.network.volley.VolleyUtil;
import com.koleshop.appkoleshop.ui.seller.views.ViewInventoryProductExpanded;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 14/10/15.
 */
public class InventoryProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private int IMAGE_SIZE_XXXDPI = 160;//px
    private static final int VIEW_TYPE_HEADER = 0x01;
    private static final int VIEW_TYPE_CONTENT = 0x00;

    public TextView textViewTitleProductMasterList; // will also be used as header text
    public TextView textViewSubtitleProductMasterList;
//    public CheckBox checkBoxProductMasterList;
    private ImageView imageViewCheckBox;
    private ProgressBar progressBarCheckBox;
    public CircleImageView circleImageViewProductMasterList;
    private String imageUrl;
    private String uniqueTag;
    private InventoryProductClickListener clickListener;
    private int viewType;
    private Product product;
    private Map<Long, Boolean> productVarietyCheckBoxProgress;
    private String title;
    private ViewInventoryProductExpanded viewInventoryProductExpanded;
    private LinearLayout clickArea1, clickArea2;
    private Context mContext;
    private View.OnClickListener checkBoxOnClickListener;
    private TextView textViewHowManySelected;
    private boolean myInventory;
    private boolean customerView;
    private View verticalDivider;
    private SellerSettings sellerSettings;

    public InventoryProductViewHolder(View view, int viewType, Context context, boolean myInventory, boolean customerView, SellerSettings sellerSettings) {
        super(view);
        mContext = context;
        this.myInventory = myInventory;
        this.customerView = customerView;
        this.sellerSettings = sellerSettings;
        if(viewType == VIEW_TYPE_HEADER) {
            textViewTitleProductMasterList = (TextView) view.findViewById(R.id.tv_list_header);
        } else if(viewType == VIEW_TYPE_CONTENT) {
            clickArea1 = (LinearLayout) view.findViewById(R.id.ll_inventory_product_click_area_1);
            clickArea1.setOnClickListener(this);
            clickArea2 = (LinearLayout) view.findViewById(R.id.ll_inventory_product_click_area_2);
            verticalDivider = view.findViewById(R.id.view_vertical_divider);
            verticalDivider.setVisibility(View.GONE);
            textViewTitleProductMasterList = (TextView) view.findViewById(R.id.textViewTitleProductMasterListItem);
            textViewSubtitleProductMasterList = (TextView) view.findViewById(R.id.textViewSubtitleProductMasterListItem);
            textViewHowManySelected = (TextView) view.findViewById(R.id.tv_how_many_selected);
            //this.checkBoxProductMasterList = (CheckBox) view.findViewById(R.id.checkboxProductMasterListItem);
            circleImageViewProductMasterList = (CircleImageView) view.findViewById(R.id.circleImageViewProductMasterListItem);
            circleImageViewProductMasterList.setOnClickListener(this);
            imageViewCheckBox = (ImageView) view.findViewById(R.id.iv_inventory_product);
            progressBarCheckBox = (ProgressBar) view.findViewById(R.id.pb_inventory_product);
            if(myInventory) {
                //hide the product selection option
                clickArea2.setVisibility(View.GONE);
                verticalDivider.setVisibility(View.GONE);
            }
        } else {
            viewInventoryProductExpanded = new ViewInventoryProductExpanded(context, view, customerView, sellerSettings);
        }
    }

    public void setTitle(String title) {
        textViewTitleProductMasterList.setText(title);
    }

    public void setSubtitle(String subtitle) {
        textViewSubtitleProductMasterList.setText(subtitle);
    }

    public void setChecked(boolean checked) {
        if(checked) {
            imageViewCheckBox.setImageResource(R.drawable.ic_checkbox_true);
        } else {
            imageViewCheckBox.setImageResource(R.drawable.ic_checkbox_false_focused);
        }
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setHowManySelected(int selectedCount, int totalCount) {
        textViewHowManySelected.setText(selectedCount + "/" + totalCount);
    }

    public void sendImageFetchRequest(final Context context) {
        //loading product image
        imageUrl = KoleshopUtils.getSmallImageUrl(imageUrl);
        Picasso.with(context)
                .load(imageUrl)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_koleshop_grey_24dp))
                .error(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_koleshop_grey_24dp))
                .into(circleImageViewProductMasterList, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(context)
                                .load(imageUrl)
                                .placeholder(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_koleshop_grey_24dp))
                                .error(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_koleshop_grey_24dp))
                                .into(circleImageViewProductMasterList);
                    }
                });
    }

    public void cancelImageFetchRequest() {
        VolleyUtil.getInstance().cancelRequestsWithTag(uniqueTag);
    }

    public void bindData(int viewType, String title, Product product, Map<Long, Boolean> productVarietyCheckBoxProgress, int positionInParentView) {
        //bind view holder
        this.viewType = viewType;
        this.product = product;
        this.title = title;

        if(viewType == VIEW_TYPE_HEADER) {
            setTitle(title);
        } else if(viewType == VIEW_TYPE_CONTENT) {
            setTitle(product.getName());
            setSubtitle(makeDescription());
            if(!myInventory) {
                //selection options are only for global inventory
                int howManySelected = ProductUtil.getProductSelectionCount(product);
                setChecked(howManySelected > 0);
                setHowManySelected(howManySelected, product.getVarieties().size());
                boolean productSelectionInProgress = false;
                for (Map.Entry<Long, Boolean> entry : productVarietyCheckBoxProgress.entrySet()) {
                    if (entry.getValue()) {
                        productSelectionInProgress = true;
                        break;
                    }
                }
                if (productSelectionInProgress) {
                    startCheckBoxProgress();
                } else {
                    stopCheckBoxProgress();
                }
            }
            List<ProductVariety> varieties = product.getVarieties();
            if(varieties!=null) {
                String imageUrl = varieties.get(0).getImageUrl();
                setImageUrl(imageUrl);
                //todo launch this request when kolserver image server is working
                sendImageFetchRequest(mContext);
            }
        } else { //expanded view
            viewInventoryProductExpanded.setProduct(product, productVarietyCheckBoxProgress, positionInParentView);
        }
    }

    @Override
    public void onClick(View v) {
        clickListener.onItemClick(v);
    }

    public void setClickListener(InventoryProductClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setCheckBoxOnClickListener(View.OnClickListener checkBoxClickListener) {
        this.checkBoxOnClickListener = checkBoxClickListener;
        clickArea2.setOnClickListener(checkBoxOnClickListener);
    }

    public void startCheckBoxProgress() {
        //imageViewCheckBox.setVisibility(View.GONE);
        clickArea2.setVisibility(View.GONE);
        verticalDivider.setVisibility(View.GONE);
        clickArea2.setOnClickListener(null);
        progressBarCheckBox.setVisibility(View.VISIBLE);
    }

    private void stopCheckBoxProgress() {
        //imageViewCheckBox.setVisibility(View.VISIBLE);
        clickArea2.setVisibility(View.VISIBLE);
        verticalDivider.setVisibility(View.VISIBLE);
        clickArea2.setOnClickListener(checkBoxOnClickListener);
        progressBarCheckBox.setVisibility(View.GONE);
    }

    private String makeDescription() {
        if(product==null) {
            return "";
        } else{
            String description = "";
            boolean first = true;
            for(ProductVariety ipv : product.getVarieties()) {
                String price = Constants.INDIAN_RUPEE_SYMBOL + " " + ipv.getPrice();
                if(price.endsWith(".0")) {
                    price = price.substring(0, price.length()-2);
                }
                String desc = ipv.getQuantity() + " - " + price;
                if(!customerView && !myInventory) {
                    if (ipv.isVarietyValid()) {
                        desc += " ✓";
                    } else {
                        desc += " ✗";
                    }
                } else if(customerView && myInventory){
                    if (ipv.isVarietyValid() && !ipv.isLimitedStock()) {
                        desc += " (OUT OF STOCK)";
                    }
                }
                if(first) {
                    description += desc;
                    first = false;
                } else {
                    description += " | " + desc;
                }
            }
            return description;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return clickListener.onItemLongClick(v);
    }
}
