package com.koleshop.appkoleshop.fragments.productedit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.common.util.ImageUtils;
import com.koleshop.appkoleshop.common.util.NetworkUtils;
import com.koleshop.appkoleshop.common.util.PreferenceUtils;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.io.File;

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
    private BroadcastReceiver editFragmentBroadcastReceiver;
    private String TAG = "ProVarEditFrag";

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
                    if((variety.getLimitedStock()<=0 && isChecked) || variety.getLimitedStock()>=1 && !isChecked) {
                        mListener.productModified(true);
                    }
                    if(isChecked) {
                        variety.setLimitedStock(1);
                    } else {
                        variety.setLimitedStock(0);
                    }
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
                    if(variety.getPrice() != price) {
                        mListener.productModified(true);
                    }
                    editTextPrice.setError(null);
                    if (variety != null) {
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
                    if(variety.getQuantity()!=null && !variety.getQuantity().equalsIgnoreCase(s.toString())) {
                        mListener.productModified(true);
                    }
                    variety.setQuantity(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        initializeBroadcastReceivers();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(editFragmentBroadcastReceiver, new IntentFilter(Constants.ACTION_UPLOAD_IMAGE_SUCCESS));
        lbm.registerReceiver(editFragmentBroadcastReceiver, new IntentFilter(Constants.ACTION_UPLOAD_IMAGE_FAILED));
        fixImageUploadingProcess();
        refreshData();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(editFragmentBroadcastReceiver);
        super.onPause();
    }

    private void initializeBroadcastReceivers() {
        editFragmentBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_UPLOAD_IMAGE_SUCCESS)) {
                    String tag = intent.getStringExtra("tag");
                    String filename = intent.getStringExtra("filename");
                    if (tag != null && !tag.isEmpty() && filename != null && !filename.isEmpty() && tag.equalsIgnoreCase(variety.getTag())) {
                        NetworkUtils.setRequestStatusComplete(mContext, tag);
                        String url = Constants.PUBLIC_IMAGE_URL_PREFIX + filename;
                        variety.setShowImageProcessing(false);
                        variety.setImageUrl(url);
                        reloadImageViewAndProcessing();
                    }

                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_UPLOAD_IMAGE_FAILED)) {
                    String tag = intent.getStringExtra("tag");
                    if (tag != null && !tag.isEmpty() && tag.equalsIgnoreCase(variety.getTag())) {
                        NetworkUtils.setRequestStatusComplete(mContext, tag);
                        variety.setShowImageProcessing(false);
                        variety.setImagePath(null);
                        reloadImageViewAndProcessing();
                    }
                }
            }
        };
    }

    public void refreshData() {
        variety = mListener.refreshVariety(getTag());
        loadTheDataIntoUi();
    }

    private void loadTheDataIntoUi() {
        setPosition(variety.getPosition());

        reloadImageViewAndProcessing();

        switchStock.setChecked(variety.getLimitedStock() > 0);
        editTextQuantity.setText(variety.getQuantity());
        if (variety.getPrice() > 0) {
            String priceString = variety.getPrice() + "";
            if (priceString.endsWith(".0")) {
                priceString = priceString.substring(0, priceString.length() - 2);
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
            mListener.productModified(true);
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

        EditProductVar refreshVariety(String varietyTag);

        void productModified(boolean modified);
    }

    public void setPosition(int position) {
        variety.setPosition(position);
        textViewNumber.setText(position + 1 + ".");
    }

    private void fixImageUploadingProcess() {
        if (variety == null) {
            return;
        }
        if (variety.isShowImageProcessing()) {
            String status = NetworkUtils.getRequestStatus(mContext, variety.getTag());
            switch (status) {
                case Constants.REQUEST_STATUS_PROCESSING:
                    break;
                case Constants.REQUEST_STATUS_SUCCESS:
                    variety.setShowImageProcessing(false);
                    String filename = variety.getImageFilename();
                    if (filename != null && !filename.isEmpty()) {
                        String url = Constants.PUBLIC_IMAGE_URL_PREFIX + filename;
                        variety.setImageUrl(url);
                    } else {
                        Log.d(TAG, "some problem while getting filename for tag");
                    }
                    break;
                case Constants.REQUEST_STATUS_FAILED:
                    variety.setShowImageProcessing(false);
                    variety.setImagePath(null);
                    break;
                default:
                    break;
            }
        }
    }

    private void reloadImageViewAndProcessing() {

        Drawable drawableCamera;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawableCamera = mContext.getDrawable(R.drawable.ic_photo_camera_grey600_48dp);
        } else {
            drawableCamera = mContext.getResources().getDrawable(R.drawable.ic_photo_camera_grey600_48dp);
        }

        if (variety.getImagePath() != null && imageView != null) {
            //load local image
            final String imageLoadPath = new File(variety.getImagePath()).toString();
            Log.d(TAG, "will try to load local image from path : " + imageLoadPath);
            try {
                new AsyncTask<String, Void, Void>() {
                    @Override
                    protected Void doInBackground(String... params) {
                        String path = params[0];
                        final Bitmap resizedBitmap = ImageUtils.getResizedBitmap(150, 150, path);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(resizedBitmap);
                            }
                        });
                        return null;
                    }
                }.execute(imageLoadPath);

            } catch (Exception e) {
                Log.d(TAG, "some problem occurred while setting local image to image view", e);
            }
        } else if (imageView != null) {
            //load from internet
            String imageUrl = variety.getImageUrl();
            boolean validUrl = URLUtil.isValidUrl(imageUrl);
            if(!validUrl) {
                imageUrl = Constants.PUBLIC_IMAGE_URL_PREFIX + imageUrl;
            } else {
                Log.d(TAG, "url is valid");
            }
            Log.d(TAG, "will load this url : " + imageUrl);
            Picasso.with(mContext)
                    .load(imageUrl)
                    .fit()
                    .centerCrop()
                    .placeholder(drawableCamera)
                    .error(drawableCamera)
                    .into(imageView);
        } else if (imageView == null) {
            //this condition should never be called
            Toast.makeText(mContext, "image view not created", Toast.LENGTH_SHORT).show();
        }

        reloadProcessing();
    }

    private void reloadProcessing() {
        if (variety.isShowImageProcessing()) {
            progressBarImage.setVisibility(View.VISIBLE);
        } else {
            progressBarImage.setVisibility(View.GONE);
        }
    }

    public EditProductVar getVariety() {
        return variety;
    }
}
