package fr.insa_lyon.vcr.utilitaires;

import android.animation.ValueAnimator;

import com.google.android.gms.maps.model.Circle;

/**
 * Created by julien on 04/05/15.
 */
public class CustomAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

    Circle c;

    public void setCircle(Circle c) {
        this.c = c;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float animatedFraction = animation.getAnimatedFraction();
        // Log.e("", "" + animatedFraction);
    }
}
