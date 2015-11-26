package com.kolshop.kolshopmaterial.viewholders.inventory;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.ProductUtil;
import com.kolshop.kolshopmaterial.extensions.InventoryProductClickListener;
import com.kolshop.kolshopmaterial.network.volley.VolleyUtil;
import com.kolshop.kolshopmaterial.views.ViewInventoryProductExpanded;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProductVariety;
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
    private InventoryProduct product;
    private Map<Long, Boolean> productVarietyCheckBoxProgress;
    private String title;
    private ViewInventoryProductExpanded viewInventoryProductExpanded;
    private LinearLayout clickArea1, clickArea2;
    private Context mContext;
    private View.OnClickListener checkBoxOnClickListener;
    private TextView textViewHowManySelected;
    private boolean myInventory;

    public InventoryProductViewHolder(View view, int viewType, Context context, boolean myInventory) {
        super(view);
        mContext = context;
        this.myInventory = myInventory;
        if(viewType == VIEW_TYPE_HEADER) {
            textViewTitleProductMasterList = (TextView) view.findViewById(R.id.tv_inventory_product_header);
        } else if(viewType == VIEW_TYPE_CONTENT) {
            clickArea1 = (LinearLayout) view.findViewById(R.id.ll_inventory_product_click_area_1);
            clickArea1.setOnClickListener(this);
            clickArea2 = (LinearLayout) view.findViewById(R.id.ll_inventory_product_click_area_2);
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
            }
        } else {
            viewInventoryProductExpanded = new ViewInventoryProductExpanded(context, view);
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

    public void sendImageFetchRequest(Context context) {
        Picasso.with(context)
                .load(imageUrl)
                .into(circleImageViewProductMasterList);
        /*ImageRequest request = new ImageRequest(imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        circleImageViewProductMasterList.setImageBitmap(bitmap);
                    }
                }, IMAGE_SIZE_XXXDPI, IMAGE_SIZE_XXXDPI, ImageView.ScaleType.CENTER_INSIDE, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        circleImageViewProductMasterList.setImageResource(R.drawable.image_just_gray);
                    }
                });
        uniqueTag = CommonUtils.randomString(10);
        VolleyUtil.getInstance().addToRequestQueue(request, uniqueTag);*/
    }

    public void cancelImageFetchRequest() {
        VolleyUtil.getInstance().cancelRequestsWithTag(uniqueTag);
    }

    public void bindData(int viewType, String title, InventoryProduct product, Map<Long, Boolean> productVarietyCheckBoxProgress, int positionInParentView, Long categoryId) {
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
            List<InventoryProductVariety> varieties = product.getVarieties();
            if(varieties!=null) {
                String imageUrl = varieties.get(0).getImageUrl();
                String smallSizeImageUrl="";
                if(imageUrl!=null)
                    smallSizeImageUrl = imageUrl.replaceFirst("small", "prod-image/286X224");
                setImageUrl(smallSizeImageUrl);
                //todo launch this request when kolserver image server is working
                //holder.sendImageFetchRequest(context);
            }
        } else { //expanded view
            viewInventoryProductExpanded.setProduct(product, productVarietyCheckBoxProgress, positionInParentView, categoryId);
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
        clickArea2.setOnClickListener(null);
        progressBarCheckBox.setVisibility(View.VISIBLE);
    }

    private void stopCheckBoxProgress() {
        //imageViewCheckBox.setVisibility(View.VISIBLE);
        clickArea2.setVisibility(View.VISIBLE);
        clickArea2.setOnClickListener(checkBoxOnClickListener);
        progressBarCheckBox.setVisibility(View.GONE);
    }

    private String makeDescription() {
        if(product==null) {
            return "";
        } else{
            String description = "";
            boolean first = true;
            for(InventoryProductVariety ipv : product.getVarieties()) {
                String desc = ipv.getQuantity() + "(" + Constants.INDIAN_RUPEE_SYMBOL + " " + ipv.getPrice() + ")";
                if(ipv.getSelected()) {
                    desc += " ✓";
                } else {
                    desc += " ✗";
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
