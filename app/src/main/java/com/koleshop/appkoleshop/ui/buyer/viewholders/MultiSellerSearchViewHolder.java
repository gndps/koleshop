package com.koleshop.appkoleshop.ui.buyer.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.SellerSearchResults;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 07/02/16.
 */
public class MultiSellerSearchViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.tv_title_msst)
    TextView textViewTitle;
    @Bind(R.id.tv_subtitle_msst)
    TextView textViewSubtitle;
    @Bind(R.id.tv_distance_msst)
    TextView textViewDistance;
    @Bind(R.id.civ_avatar_msst)
    CircleImageView imageViewAvatar;
    @Bind(R.id.button_more_results_msst)
    Button buttonShowMoreResults;

    Context mContext;

    private SellerSearchResults results;

    public MultiSellerSearchViewHolder(Context context, View itemView) {
        super(itemView);
        this.mContext = context;
        ButterKnife.bind(mContext, itemView);
    }

    public void bindData(SellerSearchResults results) {
        this.results = results;
        textViewTitle.setText(results.getSellerSettings().getAddress().getName());
    }

}
