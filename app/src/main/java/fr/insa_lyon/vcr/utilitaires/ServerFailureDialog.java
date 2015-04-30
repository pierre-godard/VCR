package fr.insa_lyon.vcr.utilitaires;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import fr.insa_lyon.vcr.vcr.R;

/**
 * Created by julien on 30/04/15.
 */
public class ServerFailureDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_message)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onChoose();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }


    private FinishWithDialog mListener;

    @Override
    public void onAttach(Activity activity) {
        mListener = (FinishWithDialog) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }
}