package fr.insa_lyon.vcr.utilitaires;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import fr.insa_lyon.vcr.vcr.GraphStation;
import fr.insa_lyon.vcr.vcr.R;
import fr.insa_lyon.vcr.vcr.ResearchDialog;

/**
 * Created by julien on 03/05/15.
 */
public class CustomInfoWindow extends FragmentActivity implements GoogleMap.InfoWindowAdapter {


    LayoutInflater layoutInflater;
    View customInfoView;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public CustomInfoWindow(LayoutInflater layInf) {
        customInfoView = layInf.inflate(R.layout.custom_info, null);
        LinearLayout ll = (LinearLayout) customInfoView.findViewById(R.id.infoLayout);
        Drawable dr = customInfoView.getResources().getDrawable(R.drawable.cadre);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable d = new BitmapDrawable(customInfoView.getResources(), Bitmap.createScaledBitmap(bitmap, 350, 80, true));
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            ll.setBackgroundDrawable(d);
        } else {
            ll.setBackground(d);
        }
    }

    public void onButtonClickedGraph(View v) {

        DialogFragment newFragment = new GraphStation();
        newFragment.show(getFragmentManager(), "graph");
    }

    @Override
    public View getInfoContents(Marker marker) {
/*
        TextView textView_title = ((TextView) customInfoView.findViewById(R.id.title));
        textView_title.setText(marker.getTitle());
        TextView textView_BikeAvail = ((TextView) customInfoView.findViewById(R.id.snippet));
        textView_BikeAvail.setText(marker.getSnippet());

        return customInfoView;*/
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {

        TextView textView_title = ((TextView) customInfoView.findViewById(R.id.title));
        textView_title.setText(marker.getTitle());
        TextView textView_BikeAvail = ((TextView) customInfoView.findViewById(R.id.snippet));
        textView_BikeAvail.setText(marker.getSnippet());

        return customInfoView;
    }


}
