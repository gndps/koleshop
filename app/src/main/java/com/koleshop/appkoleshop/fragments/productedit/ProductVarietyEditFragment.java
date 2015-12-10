package com.koleshop.appkoleshop.fragments.productedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.common.util.ImageUtils;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InteractionListener} interface
 * to handle interaction events.
 */
public class ProductVarietyEditFragment extends Fragment implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    @Bind(R.id.met_product_edit_variety_quantity)
    MaterialEditText editTextQuantity;
    @Bind(R.id.met_product_edit_variety_price)
    MaterialEditText editTextPrice;
    @Bind(R.id.switch_product_edit)
    SwitchCompat switchStock;
    @Bind(R.id.tv_product_edit_number)
    TextView textViewNumber;
    @Bind(R.id.btn_product_edit_overflow)
    ImageButton buttonOverFlow;
    @Bind(R.id.iv_product_edit)
    ImageView imageView;
    @Bind(R.id.pb_image_upload_product_edit)
    ProgressBar progressBarImage;

    private InteractionListener mListener;
    private EditProductVar variety;
    private Context mContext;

    public ProductVarietyEditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        View v = inflater.inflate(R.layout.fragment_product_variety_edit, container, false);
        ButterKnife.bind(this, v);
        switchStock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (variety != null) {
                    variety.setSelected(isChecked);
                }
            }
        });

        editTextPrice.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                try {
                    float price = Float.parseFloat(s.toString());
                    editTextPrice.setError(null);
                    if(variety!=null) {
                        variety.setPrice(price);
                    }
                } catch (Exception e) {
                    editTextPrice.setError("Price should be a number");
                }
            }
        });

        editTextQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (variety != null) {
                    variety.setQuantity(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadData();
    }

    private void loadTheDataIntoUi() {
        setPosition(variety.getPosition());
        Drawable drawableCamera;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawableCamera = mContext.getDrawable(R.drawable.ic_photo_camera_grey600_48dp);
        } else {
            drawableCamera = mContext.getResources().getDrawable(R.drawable.ic_photo_camera_grey600_48dp);
        }

        if (variety.getTempBitmapByteArray() != null) {
            //load local image
            if (imageView != null) {
                final Bitmap bitmap = ImageUtils.getBitmapFromByteArray(variety.getTempBitmapByteArray());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.invalidate();
                        imageView.setImageBitmap(bitmap);
                    }
                });
            } else {
                Toast.makeText(mContext, "image view not created", Toast.LENGTH_SHORT).show();
            }
        } else {

            Picasso.with(mContext)
                    .load(variety.getImageUrl())
                    .placeholder(drawableCamera)
                    .error(drawableCamera)
                    .into(imageView);
        }

        if (variety.isShowImageProcessing()) {
            progressBarImage.setVisibility(View.VISIBLE);
        } else {
            progressBarImage.setVisibility(View.GONE);
        }

        switchStock.setChecked(variety.getLimitedStock() > 0);
        editTextQuantity.setText(variety.getQuantity());
        if(variety.getPrice()>0) {
            String priceString  = variety.getPrice() + "";
            if(priceString.endsWith(".0")) {
                priceString = priceString.substring(0,priceString.length()-2);
            }
            editTextPrice.setText(priceString);
        }
        buttonOverFlow.setOnClickListener(this);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InteractionListener) {
            mListener = (InteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ProductVarietyEditFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case com.koleshop.appkoleshop.R.id.btn_product_edit_overflow:
                PopupMenu popup = new PopupMenu(mContext, v);
                popup.setOnMenuItemClickListener(this);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(com.koleshop.appkoleshop.R.menu.menu_product_variety_seller, popup.getMenu());
                popup.show();
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case com.koleshop.appkoleshop.R.id.item_delete_product_variety:
                deleteProductVariety();
                return true;
            default:
                return false;
        }
    }

    public void deleteProductVariety() {
        if (mListener != null) {
            mListener.deleteVariety(variety.getTag());
        }
    }

    private void captureImage() {
        if (mListener != null) {
            mListener.captureImage(variety.getTag());
        }
    }

    public interface InteractionListener {
        void deleteVariety(String varietyTag);

        void captureImage(String varietyTag);

        EditProductVar getVariety(String varietyTag);
    }

    public void setPosition(int position) {
        variety.setPosition(position);
        textViewNumber.setText(position + 1 + ".");
    }

    public void reloadData() {
        variety = mListener.getVariety(getTag());
        loadTheDataIntoUi();
    }
}
