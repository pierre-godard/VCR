package fr.insa_lyon.vcr.modele;

import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import fr.insa_lyon.vcr.vcr.R;

public class StationVelov {

    String id;
    String name;
    LatLng position;
    Marker marker;
    int numberOfBikes;
    int numberOfFreeBikeStands;
    boolean withdrawal;
    String snippetText;
    boolean selected = false;

    public StationVelov(JSONObject jsonObj, Marker marqueur) {
        // Static Data
        try {
            name = jsonObj.getString("name").split("-", 2)[1]; // get only second part of the name
            id = jsonObj.getString("id");
        } catch (JSONException e) {
            Log.e("STATION_VELOV", "Problem when parsing JSONObject.");
        }


        // Marker
        this.marker = marqueur;
        this.marker.setTitle(name);
        snippetText = this.marker.getSnippet();
        this.marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marqueurperso));
        //this.marker.setFlat(false);
        position = this.marker.getPosition();
        numberOfBikes = 0;      // waiting for VelocityRaptorMain to fetch these data...
        numberOfFreeBikeStands = 0;
        withdrawal = true;  // default case assume user want to withdraw a bike from the station.
    }

    //------------------------------------------------------------------------- OPERATIONS ON MARKER

    public void setBikesAndStands(int bikes, int stands) {
        Log.d("SET_BIKES_AND_STANDS", "In method.");
        numberOfBikes = bikes;
        numberOfFreeBikeStands = stands;
        setMarkerSnippet(numberOfBikes, numberOfFreeBikeStands);
        updateMarkerIcon();
        Log.d("SET_BIKES_AND_STANDS", "Done");
    }

    public void setMode(boolean withdrawal) {
        this.withdrawal = withdrawal;
        setMarkerSnippet(numberOfBikes, numberOfFreeBikeStands);
        marker.hideInfoWindow();
        updateMarkerIcon();
    }

    /**
     * Update the icon according to the number of places left and the user choice
     * (withdraw or deposit a bike). Updating icon here allows us to have both withdraw and deposit
     * stations at the same time on the map (icons will have different shapes) so that we can
     * display a whole itinerary, with potential departure stations, and potential arrival stations
     */
    private void updateMarkerIcon() {

        if (numberOfFreeBikeStands == 0) {
            if (withdrawal) {    // user want to withdraw
                // set icon to green : station is full of bikes
                return;
            }
            // set icon to red : no place to put your bike
        } else if (numberOfFreeBikeStands > 0 && numberOfFreeBikeStands <= 5) {
            if (withdrawal) {

                return;
            }

        } else if (numberOfFreeBikeStands > 5 && numberOfFreeBikeStands <= 10) {
            if (withdrawal) {

                return;
            }

        } else if (numberOfFreeBikeStands > 10 && numberOfFreeBikeStands <= 15) {
            if (withdrawal) {

                return;
            }

        } else if (numberOfFreeBikeStands > 15 && numberOfFreeBikeStands <= 20) {
            if (withdrawal) {

                return;
            }

        } else {
            if (withdrawal) {

                return;
            }

        }
    }

    public void setMarkerSnippet(int avail_Bikes, int avail_Spaces) {
        Log.d("SET_MARKER_SNIPPET", "Setting snippet for marker of station" + this.name);
        String snippet;
        if (withdrawal) {
            snippet = "Retirer un vélo.\nVélos disponibles : " + avail_Bikes + ".\n" +
                    "Empacements disponibles : " + avail_Spaces + ".";
        } else {
            snippet = "Reposer un vélo.\nVélos disponibles : " + avail_Bikes + ".\n" +
                    "Empacements disponibles : " + avail_Spaces + ".";
        }
        marker.setSnippet(snippet);
    }

    public void setMarkerTitle(String text) {
        marker.setTitle(text);
    }

    //---------------------------------------------------------------------------- GETTERS - SETTERS
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getPosition() {
        return position;
    }

    public Marker getMarqueur() {
        return marker;
    }

    public void setMarqueur(Marker marqueur) {
        this.marker = marqueur;
    }

    public boolean getMode() {
        return withdrawal;
    }


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateMarkerIcon();
    }


}
