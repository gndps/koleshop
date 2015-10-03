package com.kolshop.kolshopmaterial.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.adapters.GndpAdapter;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.kolshopmaterial.model.MenuInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NavigationDrawerFragment extends Fragment {

    private static final String PREF_FILE_NAME = "navigation_drawer_fragment";
    private static final String KEY_USER_LEARNED_DRAWER = "user_learned_drawer";
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private boolean userLearnedDrawer;
    private boolean fromSaveState;
    private View containerView;
    private RecyclerView recyclerView;
    private GndpAdapter gndpAdapter;
    private BroadcastReceiver broadcastReceiverNavigationDrawer;

    public NavigationDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userLearnedDrawer = Boolean.valueOf(readFromPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, "false"));
        if (savedInstanceState != null) {
            fromSaveState = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
        lbm.registerReceiver(broadcastReceiverNavigationDrawer, new IntentFilter(Constants.ACTION_LOG_OUT));
    }

    private void initializeBroadcastReceivers() {
        broadcastReceiverNavigationDrawer = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_LOG_OUT)) {
                    gndpAdapter.setData(getData());
                    gndpAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
        lbm.unregisterReceiver(broadcastReceiverNavigationDrawer);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        //get recycler view
        recyclerView = (RecyclerView) layout.findViewById(R.id.drawerRecyclerView);


        gndpAdapter = new GndpAdapter(getActivity(), getData());
        recyclerView.setAdapter(gndpAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(Constants.ACTION_NAVIGATION_ITEM_SELECTED);
                String navItem = getActivity().getResources().getStringArray(R.array.navigation_drawer_array)[position];
                intent.putExtra("NavigationItem", navItem);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                Toast.makeText(getActivity(), "item clicked " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View v, int position) {
                //Toast.makeText(getActivity(), "item long clicked "+ position, Toast.LENGTH_LONG).show();

            }
        }));
        return layout;
    }

    public List<MenuInfo> getData() {
        List<MenuInfo> data = new ArrayList<MenuInfo>();
        String[] titles = getResources().getStringArray(R.array.navigation_drawer_array);
        for (int i = 0; i < titles.length; i++) {
            MenuInfo info = new MenuInfo();
            info.iconId = R.drawable.check;
            info.title = titles[i];
            if (info.title.equalsIgnoreCase("Log In") && PreferenceUtils.getPreferences(getActivity(), Constants.KEY_USER_ID).isEmpty()) {
                data.add(info);
            } else if (info.title.equalsIgnoreCase("Log Out") && !PreferenceUtils.getPreferences(getActivity(), Constants.KEY_USER_ID).isEmpty()) {
                data.add(info);
            } else if (!info.title.startsWith("Log")) {
                data.add(info);
            }
        }
        return data;
    }


    public void setUp(DrawerLayout drawerLayout, final Toolbar toolbar, int fragment_navigation_drawer) {
        containerView = getActivity().findViewById(fragment_navigation_drawer);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!userLearnedDrawer) {
                    userLearnedDrawer = true;
                    saveToPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, "true");
                }
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (slideOffset < 0.6) {
                    toolbar.setAlpha(1 - slideOffset);
                }
            }
        };
        if (!userLearnedDrawer && !fromSaveState) {
            mDrawerLayout.openDrawer(containerView);
        }
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
                               @Override
                               public void run() {
                                   mDrawerToggle.syncState();
                               }
                           }
        );
    }

    public static void saveToPreferences(Context context, String name, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public static String readFromPreferences(Context context, String name, String defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return preferences.getString(name, defaultValue);
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        ClickListener clickListener;
        GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView rv, final ClickListener clickListener) {
            Log.d("gndp", "Recycler Touch Listener constructor");
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    Log.d("gndp", "Gesture Detector single tap up");
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    Log.d("gndp", "Gesture Detector long press");
                    View child = rv.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
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
            if (child != null && rv != null && gestureDetected) {
                clickListener.onItemClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }
    }

    public static interface ClickListener {
        public void onItemClick(View v, int position);

        public void onItemLongClick(View v, int position);
    }

}
