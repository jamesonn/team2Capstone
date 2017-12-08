package team2.mkesocial.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import team2.mkesocial.Activities.LoginActivity;
import team2.mkesocial.R;

/**
 * Created by Nate on 12/7/2017.
 */

public class PhoneLoginDialogFragment extends DialogFragment{

    private EditText phoneNumber;

    public interface PhoneLoginDialogListener {
        public void onDialogPositiveClick(String pn);
    }

    PhoneLoginDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.fragment_phone_login, null));
        builder.setMessage("Enter your verification code:");
        builder
                .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText phoneNumber = (EditText)getDialog().findViewById(R.id.phoneNumber);
                        mListener.onDialogPositiveClick(phoneNumber.getText().toString());
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (PhoneLoginDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement PhoneLoginDialogListener");
        }
    }
}
