package fr.insa_lyon.vcr.modele;

import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Integer;

import fr.insa_lyon.vcr.vcr.R;

public class StationVelov implements ClusterItem {

    String id;
    String title;
    LatLng position;
    String snippet;
    int numberOfBikes_current;
    int numberOfFreeBikeStands_current;
    int numberOfBikes_predict;
    int numberOfFreeBikeStands_predict;
    float predictionConfidence;
    boolean withdrawal;
    boolean selected = false;
    boolean infoWindowShown = false;


    public StationVelov(JSONObject jsonObj) {
        // Static Data
        try {
            title = jsonObj.getString("name");
            //title = jsonObj.getString("name").split("-", 2)[1]; // get only second part of the name
            id =((Integer)jsonObj.getInt("number")).toString();
            position = new LatLng(jsonObj.getDouble("latitude"), jsonObj.getDouble("longitude"));
        } catch (JSONException e) {
            Log.e("STATION_VELOV", "Problem when parsing JSONObject.");
        }
        //setMarkerSnippet(0,0);

        numberOfBikes_current = 0;      // waiting for VelocityRaptorMain to fetch these data...
        numberOfFreeBikeStands_current = 0;
        numberOfBikes_predict = 0;
        numberOfFreeBikeStands_predict = 0;
        predictionConfidence = 0;
        withdrawal = true;  // default case assume user want to withdraw a bike from the station.
    }

    //------------------------------------------------------------------------- OPERATIONS ON MARKER

    public void setBikesAndStands(int bikes, int stands) {
        numberOfBikes_current = bikes;
        numberOfFreeBikeStands_current = stands;
        updateMarkerIcon();
        updtSnippet();
    }

    /**
     * Update the icon according to the number of places left and the user choice
     * (withdraw or deposit a bike). Updating icon here allows us to have both withdraw and deposit
     * stations at the same time on the map (icons will have different shapes) so that we can
     * display a whole itinerary, with potential departure stations, and potential arrival stations
     */
    private BitmapDescriptor updateMarkerIcon() {
        BitmapDescriptor bitmap;

        if (numberOfBikes_current + numberOfFreeBikeStands_current == 0) {
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_y7);
        } else {
            float ratio;
            if (withdrawal) { //ratio of avail bikes on the total of possible bikes in the station
                ratio = ((float) numberOfBikes_current) / (numberOfBikes_current + numberOfFreeBikeStands_current);
            } else { // ration of avail stands on the total of possible bikes in the station
                ratio = ((float) (numberOfFreeBikeStands_current)) / (numberOfBikes_current + numberOfFreeBikeStands_current);
            }

            // choose icon according to ratio :
            if (ratio == 0) {            // no bike / bike stand at the station
                if (!selected) {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_a0);
                } else {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_z0);
                }
            } else if (ratio > 0 && ratio <= 0.2) {
                if (!selected) {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_a1);
                } else {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_z1);
                }
            } else if (ratio > 0.2 && ratio <= 0.4) {
                if (!selected) {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_a2);
                } else {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_z2);
                }
            } else if (ratio > 0.4 && ratio <= 0.5) {
                if (!selected) {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_a3);
                } else {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_z3);
                }
            } else if (ratio > 0.5 && ratio <= 0.6) {
                if (!selected) {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_a4);
                } else {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_z4);
                }
            } else if (ratio > 0.6 && ratio <= 0.8) {
                if (!selected) {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_a5);
                } else {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_z5);
                }
            } else if (ratio > 0.8 && ratio < 1) {
                if (!selected) {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_a6);
                } else {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_z6);
                }
            } else {
                if (!selected) {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_a7);
                } else {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_z7);
                }
            }
        }
        return bitmap;
    }

    public void updtSnippet() {
        if (numberOfFreeBikeStands_current + numberOfBikes_current != 0) {
            if (withdrawal) {
                snippet = "Vélos disponibles : " + numberOfBikes_current;
            } else {
                snippet = "Empacements disponibles : " + numberOfFreeBikeStands_current;
            }
        } else {
            snippet = "Pas de données";
        }
    }

    public void updtSnippetPredict(int predictTime) {
        if (numberOfFreeBikeStands_predict + numberOfBikes_predict != 0) {
            if (withdrawal) {
                snippet = "Prédiction à "+predictTime+"min :\n"+numberOfBikes_predict+" vélos disponibles.";
            } else {
                snippet = "Prédiction à "+predictTime+"min :\n"+numberOfFreeBikeStands_predict+" emplacements disponibles.";
            }
        } else {
            snippet = "Pas de prédiction disponible à "+predictTime+"min.";
        }

    }

    //---------------------------------------------------------------------------- GETTERS - SETTERS
    public String getSnippet() {
        updtSnippet();
        return snippet;
    }

    public String getSnippetPrediction(int predictTime){
        updtSnippetPredict(predictTime);
        return  snippet;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public boolean getMode() {
        return withdrawal;
    }

    public void setMode(boolean withdrawal) {
        this.withdrawal = withdrawal;
        updateMarkerIcon();
        updtSnippet();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateMarkerIcon();
    }

    public boolean isInfoWindowShown() {
        return infoWindowShown;
    }

    public void switchInfoWindowShown(){
        infoWindowShown=!infoWindowShown;
    }


    public BitmapDescriptor getIcon() {
        return updateMarkerIcon();
    }



    public int getNumberOfBikes_predict() {
        return numberOfBikes_predict;
    }

    public void setNumberOfBikes_predict(int numberOfBikes_predict) {
        this.numberOfBikes_predict = numberOfBikes_predict;
    }

    public int getNumberOfFreeBikeStands_predict() {
        return numberOfFreeBikeStands_predict;
    }

    public void setNumberOfFreeBikeStands_predict(int numberOfFreeBikeStands_predict) {
        this.numberOfFreeBikeStands_predict = numberOfFreeBikeStands_predict;
    }

    public float getPredictionConfidence(){
        return predictionConfidence;
    }

    public void setPredictionConfidence(float predictionConfidence){
        this.predictionConfidence = predictionConfidence;
    }


}
