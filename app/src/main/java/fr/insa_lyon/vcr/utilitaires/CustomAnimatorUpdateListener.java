package fr.insa_lyon.vcr.utilitaires;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;

import com.google.android.gms.maps.model.Circle;

/**
 * Created by julien on 04/05/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CustomAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

    Circle c;

    public void setCircle(Circle c) {
        this.c = c;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float animatedFraction = animation.getAnimatedFraction();
        // Log.e("", "" + animatedFraction);
        c.setRadius(animatedFraction * 1000);
    }
}
