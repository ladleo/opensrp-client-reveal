package org.smartregister.reveal.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.vijay.jsonwizard.customviews.TreeViewDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.smartregister.AllConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.reveal.R;
import org.smartregister.reveal.activity.BaseMapActivity;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.ListTaskContract;
import org.smartregister.reveal.presenter.ListTaskPresenter;
import org.smartregister.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by samuelgithengi on 11/20/18.
 */
public class ListTasksActivity extends BaseMapActivity implements ListTaskContract.ListTaskView, View.OnClickListener, SyncStatusBroadcastReceiver.SyncStatusListener {

    private static final String TAG = "ListTasksActivity";

    private AllSharedPreferences sharedPreferences;

    private ListTaskPresenter listTaskPresenter;

    private View rootView;

    private TextView campaignTextView;
    private TextView operationalAreaTextView;
    private TextView districtTextView;
    private TextView facilityTextView;
    private TextView operatorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_tasks);

        listTaskPresenter = new ListTaskPresenter(this);

        rootView = findViewById(R.id.content_frame);
        kujakuMapView = findViewById(R.id.kujakuMapView);
        kujakuMapView.onCreate(savedInstanceState);

        kujakuMapView.setStyleUrl("asset://reveal-streets-style.json");

        kujakuMapView.showCurrentLocationBtn(true);
        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                mapboxMap.setMinZoomPreference(14);
                mapboxMap.setMaxZoomPreference(21);

                String geoJson = Utils.readAssetContents(ListTasksActivity.this, "geojson.json");

                FeatureCollection featureCollection = FeatureCollection.fromJson(geoJson);

                GeoJsonSource geoJsonSource = mapboxMap.getSourceAs("reveal-data-set");

                if (geoJsonSource != null) {
                    geoJsonSource.setGeoJson(featureCollection);
                }

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(-14.1706623, 32.5987837))
                        .zoom(18)
                        .build();
                mapboxMap.setCameraPosition(cameraPosition);
            }
        });

        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);

        ImageButton mDrawerMenuButton = findViewById(R.id.drawerMenu);
        mDrawerMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        sharedPreferences = RevealApplication.getInstance().getContext().allSharedPreferences();

        initializeDrawerLayout();


    }

    private void initializeDrawerLayout() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        int screenHeightPixels = getResources().getDisplayMetrics().heightPixels
                - getResources().getDimensionPixelSize(R.dimen.drawer_margin_vertical);
        headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, screenHeightPixels));

        try {
            ((TextView) headerView.findViewById(R.id.application_version))
                    .setText(getString(R.string.app_version, Utils.getVersion(this)));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        String buildDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(AllConstants.BUILD_TIMESTAMP));
        ((TextView) headerView.findViewById(R.id.application_updated)).setText(buildDate);

        campaignTextView = headerView.findViewById(R.id.campaign_selector);
        operationalAreaTextView = headerView.findViewById(R.id.operational_area_selector);
        districtTextView = headerView.findViewById(R.id.district_label);
        facilityTextView = headerView.findViewById(R.id.facility_label);
        operatorTextView = headerView.findViewById(R.id.operator_label);

        listTaskPresenter.onInitializeDrawerLayout();

        operationalAreaTextView.setOnClickListener(this);

        campaignTextView.setOnClickListener(this);

        headerView.findViewById(R.id.logout_button).setOnClickListener(this);
        headerView.findViewById(R.id.sync_button).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.operational_area_selector)
            listTaskPresenter.onShowOperationalAreaSelector();
        else if (v.getId() == R.id.campaign_selector)
            listTaskPresenter.onShowCampaignSelector();
        else if (v.getId() == R.id.logout_button)
            RevealApplication.getInstance().logoutCurrentUser();
        else if (v.getId() == R.id.sync_button) {
            SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
        }
    }

    @Override
    public void showOperationalAreaSelector(Pair<String, ArrayList<String>> locationHierarchy) {
        try {
            TreeViewDialog treeViewDialog = new TreeViewDialog(ListTasksActivity.this,
                    R.style.AppTheme_WideDialog,
                    new JSONArray(locationHierarchy.first), locationHierarchy.second, locationHierarchy.second);
            treeViewDialog.setCancelable(true);
            treeViewDialog.setCanceledOnTouchOutside(true);
            treeViewDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    listTaskPresenter.onOperationalAreaSelectorClicked(treeViewDialog.getName());
                }
            });
            treeViewDialog.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void showCampaignSelector(List<String> campaigns, String entireTreeString) {
        try {
            TreeViewDialog treeViewDialog = new TreeViewDialog(ListTasksActivity.this,
                    R.style.AppTheme_WideDialog,
                    new JSONArray(entireTreeString), new ArrayList<>(campaigns), new ArrayList<>(campaigns));
            treeViewDialog.show();
            treeViewDialog.setCanceledOnTouchOutside(true);
            treeViewDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    listTaskPresenter.onCampaignSelectorClicked(treeViewDialog.getValue(), treeViewDialog.getName());
                }
            });
            treeViewDialog.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCampaign(String campaign) {
        campaignTextView.setText(campaign);
    }

    @Override
    public void setOperationalArea(String operationalArea) {
        operationalAreaTextView.setText(operationalArea);
    }

    @Override
    public void setDistrict(String district) {
        org.smartregister.reveal.util.Utils.setTextViewText(districtTextView, R.string.district, district);
    }

    @Override
    public void setFacility(String facility) {
        org.smartregister.reveal.util.Utils.setTextViewText(facilityTextView, R.string.facility, facility);
    }

    @Override
    public void setOperator() {
        org.smartregister.reveal.util.Utils.setTextViewText(operatorTextView, R.string.operator, sharedPreferences.fetchRegisteredANM());
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        listTaskPresenter = null;
        super.onDestroy();
    }

    @Override
    public void onSyncStart() {
        if (SyncStatusBroadcastReceiver.getInstance().isSyncing()) {
            Snackbar.make(rootView, getString(org.smartregister.R.string.syncing), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        if (fetchStatus.equals(FetchStatus.fetchedFailed)) {
            Snackbar.make(rootView, getString(org.smartregister.R.string.sync_failed), Snackbar.LENGTH_SHORT).show();

        } else if (fetchStatus.equals(FetchStatus.fetched)
                || fetchStatus.equals(FetchStatus.nothingFetched)) {
            Snackbar.make(rootView, getString(org.smartregister.R.string.sync_complete), Snackbar.LENGTH_SHORT).show();
        } else if (fetchStatus.equals(FetchStatus.noConnection)) {
            Snackbar.make(rootView, getString(org.smartregister.R.string.sync_failed_no_internet), Snackbar.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        Snackbar.make(rootView, getString(org.smartregister.R.string.sync_complete), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(this);
    }

    @Override
    public void onPause() {
        SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(this);
        super.onPause();
    }
}