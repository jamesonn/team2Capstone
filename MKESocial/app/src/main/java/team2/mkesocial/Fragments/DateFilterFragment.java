package team2.mkesocial.Fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import team2.mkesocial.R;

public class DateFilterFragment extends AppCompatDialogFragment {
    private TextView _startDateField;
    private TextView _endDateField;

    private Calendar _startDate = Calendar.getInstance();
    private Calendar _endDate = Calendar.getInstance();

    private DateFilterListener _listener;

    public interface DateFilterListener
    {
        void onDateFilterPositive(Date startDate, Date endDate);
    }

    public static DateFilterFragment create(DateFilterListener listener, Date start, Date end)
    {
        final DateFilterFragment dff = new DateFilterFragment();
        dff.setListener(listener);

        if (start != null)
            dff.setStartDate(start);
        if (end != null)
            dff.setEndDate(end);

        return dff;
    }

    public void setListener(DateFilterListener listener)
    {
        _listener = listener;
    }

    private void setStartDate(Date start) {
        _startDate.setTime(start);
    }

    private void setEndDate(Date end) {
        _endDate.setTime(end);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.fragment_date_filter_message);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.fragment_date_filter, null);
        builder.setView(view);

        _startDateField = (TextView) view.findViewById(R.id.startDate);
        _endDateField = (TextView) view.findViewById(R.id.endDate);

        _startDate.set(Calendar.HOUR_OF_DAY, _startDate.getActualMinimum(Calendar.HOUR_OF_DAY));
        _startDate.set(Calendar.MINUTE, _startDate.getActualMinimum(Calendar.MINUTE));
        _startDate.set(Calendar.SECOND, _startDate.getActualMinimum(Calendar.SECOND));
        _startDate.set(Calendar.MILLISECOND, _startDate.getActualMinimum(Calendar.MILLISECOND));
        _endDate.set(Calendar.HOUR_OF_DAY, _endDate.getActualMaximum(Calendar.HOUR_OF_DAY));
        _endDate.set(Calendar.MINUTE, _endDate.getActualMaximum(Calendar.MINUTE));
        _endDate.set(Calendar.SECOND, _endDate.getActualMaximum(Calendar.SECOND));
        _endDate.set(Calendar.MILLISECOND, _endDate.getActualMaximum(Calendar.MILLISECOND));

        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());
        _startDateField.setText(sdf.format(_startDate.getTime()));
        _endDateField.setText(sdf.format(_endDate.getTime()));

        final DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());
                _startDate.set(year, month, day);
                _startDateField.setText(sdf.format(_startDate.getTime()));
            }
        };

        final DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());
                _endDate.set(year, month, day);
                _endDateField.setText(sdf.format(_endDate.getTime()));
            }
        };

        _startDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), startDateListener,
                        _startDate.get(Calendar.YEAR), _startDate.get(Calendar.MONTH),
                        _startDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        _endDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), endDateListener,
                        _endDate.get(Calendar.YEAR), _endDate.get(Calendar.MONTH),
                        _endDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        builder.setPositiveButton(R.string.ok_button_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(R.string.cancel_button_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog dialog = (AlertDialog)getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (_endDate.compareTo(_startDate) < 0) {
                        Toast.makeText(getContext(), R.string.fragment_date_filter_range_error, Toast.LENGTH_LONG).show();
                    } else {
                        _listener.onDateFilterPositive(_startDate.getTime(), _endDate.getTime());
                        dismiss();
                    }
                }
            });
        }
    }
}
