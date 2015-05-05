package fr.insa_lyon.vcr.utilitaires;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;

import java.util.HashMap;
import java.util.Map;

import fr.insa_lyon.vcr.modele.StationVelov;
import fr.insa_lyon.vcr.vcr.R;

/**
 * Created by julien on 04/05/15.
 */
public class ClusterIconRenderer extends DefaultClusterRenderer<StationVelov> {

    private final float mDensity;
    private ShapeDrawable mColoredCircleBackground;
    private SparseArray<BitmapDescriptor> mIcons = new SparseArray<BitmapDescriptor>();
    private final IconGenerator mIconGenerator;
    private final ClusterManager<StationVelov> mClusterManager;

    private ClusterManager.OnClusterClickListener<StationVelov> mClickListener;
    private ClusterManager.OnClusterInfoWindowClickListener<StationVelov> mInfoWindowClickListener;
    private ClusterManager.OnClusterItemClickListener<StationVelov> mItemClickListener;
    private ClusterManager.OnClusterItemInfoWindowClickListener<StationVelov> mItemInfoWindowClickListener;

    private MarkerCache<StationVelov> mMarkerCache = new MarkerCache<StationVelov>();

    private Map<Marker, Cluster<StationVelov>> mMarkerToCluster = new HashMap<Marker, Cluster<StationVelov>>();
    private Map<Cluster<StationVelov>, Marker> mClusterToMarker = new HashMap<Cluster<StationVelov>, Marker>();



    public ClusterIconRenderer(Context context, GoogleMap map, ClusterManager<StationVelov> clusterManager) {
        super(context, map, clusterManager);
        mClusterManager = clusterManager;
        mIconGenerator = new IconGenerator(context);
        mIconGenerator.setContentView(makeSquareTextView(context));
        mIconGenerator.setTextAppearance(R.style.ClusterIcon_TextAppearance);
        mIconGenerator.setBackground(makeClusterBackground());
        mDensity = context.getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onBeforeClusterItemRendered(StationVelov item, MarkerOptions markerOptions) {
        markerOptions.icon(item.getIcon());
        markerOptions.title(item.getTitle());
        markerOptions.snippet(item.getSnippet());
    }


    /**
     * Called before the marker for a Cluster is added to the map.
     * The default implementation draws a circle with a rough count of the number of items.
     */
    @Override
    protected void onBeforeClusterRendered(Cluster<StationVelov> cluster, MarkerOptions markerOptions) {
        int bucket = getBucket(cluster);
        BitmapDescriptor descriptor = mIcons.get(bucket);
        if (descriptor == null) {
            mColoredCircleBackground.getPaint().setColor(getColor(bucket));
            descriptor = BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(getClusterText(bucket)));
            mIcons.put(bucket, descriptor);
        }
        // TODO: consider adding anchor(.5, .5) (Individual markers will overlap more often)
        markerOptions.icon(descriptor);
    }


    private class MarkerCache<T> {
        private Map<T, Marker> mCache = new HashMap<T, Marker>();
        private Map<Marker, T> mCacheReverse = new HashMap<Marker, T>();

        public Marker get(T item) {
            return mCache.get(item);
        }

        public T get(Marker m) {
            return mCacheReverse.get(m);
        }

        public void put(T item, Marker m) {
            mCache.put(item, m);
            mCacheReverse.put(m, item);
        }

        public void remove(Marker m) {
            T item = mCacheReverse.get(m);
            mCacheReverse.remove(m);
            mCache.remove(item);
        }
    }


    private SquareTextView makeSquareTextView(Context context) {
        SquareTextView squareTextView = new SquareTextView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        squareTextView.setLayoutParams(layoutParams);
        squareTextView.setId(R.id.text);
        int twelveDpi = (int) (12 * mDensity);
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi);
        return squareTextView;
    }


    private LayerDrawable makeClusterBackground() {
        mColoredCircleBackground = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        outline.getPaint().setColor(0x80ffffff); // Transparent white.
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, mColoredCircleBackground});
        int strokeWidth = (int) (mDensity * 3);
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    private int getColor(int clusterSize) {
        final float hueRange = 220;
        final float sizeRange = 300;
        final float size = Math.min(clusterSize, sizeRange);
        final float hue = (sizeRange - size) * (sizeRange - size) / (sizeRange * sizeRange) * hueRange;
        return Color.HSVToColor(new float[]{
                hue, 1f, .6f
        });
    }

}
