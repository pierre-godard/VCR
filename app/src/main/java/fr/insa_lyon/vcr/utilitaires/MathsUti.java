package fr.insa_lyon.vcr.utilitaires;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Lilian on 29/04/2015.
 */
public class MathsUti {

    private static double getRadians(double angle)
    {
        return angle*(Math.PI)/180;
    }

    public static double getDistance(LatLng point1, LatLng point2)
    {
        double longitudePoint1 = getRadians(point1.longitude);
        double latitudePoint1 = getRadians(point1.latitude);
        double longitudePoint2 = getRadians(point2.longitude);
        double latitudePoint2 = getRadians(point2.latitude);
        double x = (longitudePoint1 - longitudePoint2 ) * Math.cos((latitudePoint2+latitudePoint1)/2.);
        double y = (latitudePoint1 - latitudePoint2);

        return Math.sqrt((Math.pow(x,2))+(Math.pow(y,2)))*6371*1000;
    }
}
