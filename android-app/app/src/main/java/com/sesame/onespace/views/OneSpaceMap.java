package com.sesame.onespace.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.sesame.onespace.R;
import com.sesame.onespace.constant.MapMarkerIconSelector;
import com.sesame.onespace.models.map.Corner;
import com.sesame.onespace.models.map.Place;
import com.sesame.onespace.models.map.Surfer;
import com.sesame.onespace.models.map.Walker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chongos on 10/12/15 AD.
 */
public class OneSpaceMap implements GoogleMap.OnCameraChangeListener, GoogleMap.OnMarkerClickListener, ClusterManager.OnClusterItemClickListener, ClusterManager.OnClusterClickListener {

    private static final int MAX_ZOOM = 18;

    private Context context;
    private GoogleMap map;
    private List<GoogleMap.OnCameraChangeListener> onCameraChangeListeners;
    private List<GoogleMap.OnMarkerClickListener> onMarkerClickListeners;
    private ClusterManager<ClusterItem> clusterManager;
    private float currentZoom = -1;
    private DisplayTempMarkerManager tempMarkerManager;

    public OneSpaceMap(Context context, GoogleMap map) {
        this.onCameraChangeListeners = new ArrayList<>();
        this.onMarkerClickListeners = new ArrayList<>();
        this.context = context;
        this.map = map;
        this.map.setOnCameraChangeListener(this);
        this.map.setOnMarkerClickListener(this);
        this.tempMarkerManager = new DisplayTempMarkerManager(this.map);
        this.setUpClusterer();
    }

    public void animateCamera(CameraUpdate cameraUpdate) {
        map.animateCamera(cameraUpdate);
    }

    public void animateCamera(CameraUpdate cameraUpdate, int durationMs, GoogleMap.CancelableCallback callback) {
        map.animateCamera(cameraUpdate, durationMs, callback);
    }

    public void addOnCameraChangeListener(GoogleMap.OnCameraChangeListener listener) {
        onCameraChangeListeners.add(listener);
    }

    public void addOnMarkerClickListener(GoogleMap.OnMarkerClickListener listener) {
        onMarkerClickListeners.add(listener);
    }

    public void setOnClusterItemClick(ClusterManager.OnClusterItemClickListener listener) {
        clusterManager.setOnClusterItemClickListener(listener);
    }

    public Projection getProjection() {
        return map.getProjection();
    }

    private void setUpClusterer() {
        clusterManager = new ClusterManager<>(context, map);
        clusterManager.setRenderer(new CustomRenderer());
        clusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<ClusterItem>(new GridBasedAlgorithm<ClusterItem>()));
        clusterManager.setOnClusterItemClickListener(this);
        clusterManager.setOnClusterClickListener(this);
        addOnMarkerClickListener(clusterManager);
        addOnCameraChangeListener(clusterManager);
    }

    public void addItem(Object item) {
        clusterManager.addItem((ClusterItem) item);
        clusterManager.cluster();
    }

    public void addAll(ArrayList<Object> items) {
        for(Object obj : items)
            addItem(obj);
        clusterManager.cluster();
    }

    public void remove(Object item) {
        clusterManager.removeItem((ClusterItem) item);
        clusterManager.cluster();
    }

    public void remove(ArrayList<Object> item) {
        for(Object obj : item)
            remove(obj);
        clusterManager.cluster();
    }

    public void displayTempMarker(LatLng latLng, String title) {
        tempMarkerManager.display(latLng, title);
    }

    public void clearTempMarker() {
        tempMarkerManager.clear();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (cameraPosition.zoom != currentZoom)
            currentZoom = cameraPosition.zoom;

        for(GoogleMap.OnCameraChangeListener listener: onCameraChangeListeners) {
            listener.onCameraChange(cameraPosition);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        for(GoogleMap.OnMarkerClickListener listener: onMarkerClickListeners) {
            listener.onMarkerClick(marker);
        }
        return true;
    }

    @Override
    public boolean onClusterClick(Cluster cluster) {
        animateCamera(CameraUpdateFactory.newLatLngZoom(
                cluster.getPosition(), currentZoom + 1));
        return true;
    }

    @Override
    public boolean onClusterItemClick(ClusterItem clusterItem) {
        Log.i("OneSpaceMap", "ClusterItem Clicked " + clusterItem.toString());
        return true;
    }

    private class CustomRenderer extends DefaultClusterRenderer<ClusterItem> {

        private final IconGenerator mIconGenerator = new IconGenerator(context);
        private final ImageView mImageView;

        public CustomRenderer() {
            super(context, map, clusterManager);
            mImageView = new ImageView(context);
            mIconGenerator.setBackground(null);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(ClusterItem item, MarkerOptions markerOptions) {

            int image = 0;
            String title = "";
            MapMarkerIconSelector iconSelector = MapMarkerIconSelector.getInstance(context);

            if(item instanceof Place) {
                Place place = (Place) item;
                title = place.getName();
                image = iconSelector.getPlaceMarker(place.getCategoryClass());
            }
            else if(item instanceof Surfer)
                image = iconSelector.getSurferMarker();
            else if(item instanceof Walker)
                image = iconSelector.getWalkerMarker();
            else if(item instanceof Corner) {
                Corner corner = (Corner) item;
                image = iconSelector.getCornerMarker(corner.isMine());
            }

            mImageView.setImageResource(image);
            Bitmap bitmap = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap)).title(title);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<ClusterItem> cluster, MarkerOptions markerOptions) {
            super.onBeforeClusterRendered(cluster, markerOptions);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            //Never render clusters at max zoom
            if(MAX_ZOOM <= currentZoom) {
                return false;
            } else {
                return cluster.getSize() > 5;
            }
        }
    }

    private class DisplayTempMarkerManager {

        private GoogleMap map;
        private Marker marker;

        public DisplayTempMarkerManager(GoogleMap map) {
            this.map = map;
        }

        public void display(LatLng poition, String title) {
            this.clear();
            this.marker = this.map.addMarker(
                    new MarkerOptions()
                            .position(poition)
                            .title(title)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_temp_marker))
                            .flat(true));
        }

        public void clear() {
            if(marker != null)
                marker.remove();
            marker = null;
        }

    }

}
