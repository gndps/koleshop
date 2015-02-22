package com.gndps.kolshopmaterial.extensions;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Gundeep on 23/02/15.
 */
public class KolRecyclerTouchListener implements RecyclerView.OnItemTouchListener
{
    KolClickListener clickListener;
    GestureDetector gestureDetector;
    public KolRecyclerTouchListener(Context context, final RecyclerView rv, final KolClickListener clickListener)
    {
        Log.d("gndp", "Recycler Touch Listener constructor");
        this.clickListener = clickListener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d("gndp", "Gesture Detector single tap up");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.d("gndp", "Gesture Detector long press");
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if(child!=null && clickListener!=null)
                {
                    clickListener.onItemLongClick(child, rv.getChildPosition(child));
                }
                super.onLongPress(e);
            }
        });

    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        Log.d("gndp", "on intercept touch event");
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        boolean gestureDetected = gestureDetector.onTouchEvent(e);
        if(child!=null && rv!=null && gestureDetected)
        {
            clickListener.onItemClick(child, rv.getChildPosition(child));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }
}