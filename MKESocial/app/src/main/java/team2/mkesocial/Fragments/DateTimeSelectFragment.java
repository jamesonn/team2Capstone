package team2.mkesocial.Fragments;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import team2.mkesocial.R;


public class DateTimeSelectFragment extends AppCompatDialogFragment {
    private TextView _startDateField;
    private TextView _startTimeField;
    private TextView _endDateField;
    private TextView _endTimeField;
    private Button _deleteButton;

    private Calendar _startTime = Calendar.getInstance();
    private Calendar _endTime = Calendar.getInstance();

    private DateTimeSelectListener _listener;
    private boolean _isNew = true;
    private boolean _deleteVisible = true;

    public interface DateTimeSelectListener
    {
        void onDateTimeSelectPositive(Date startTime, Date endTime);
        void onDateTimeSelectDelete(Date startTime, Date endTime);
    }

    public static DateTimeSelectFragment create(DateTimeSelectListener listener, Date start, Date end, boolean isNew) {
        final DateTimeSelectFragment fragment = new DateTimeSelectFragment();
        fragment.setListener(listener);

        if (start != null)
            fragment.setStartTime(start);
        if (end != null)
            fragment.setEndTime(end);

        fragment.setIsNew(isNew);
        fragment.setDeleteVisible(!isNew);

        return fragment;
    }

    public void setListener(DateTimeSelectListener listener) {
        _listener = listener;
    }

    private void setStartTime(Date start) {
        _startTime.setTime(start);
    }

    private void setEndTime(Date end) {
        _endTime.setTime(end);
    }

    private void setIsNew(boolean state) { _isNew = state; }

    private void setDeleteVisible(boolean state) {
        _deleteVisible = state;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.fragment_date_time_select_message);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.fragment_datetime_select, null);
        builder.setView(view);

        _startDateField = (TextView)view.findViewById(R.id.startDateText);
        _startTimeField = (TextView)view.findViewById(R.id.startTimeText);
        _endDateField = (TextView)view.findViewById(R.id.endDateText);
        _endTimeField = (TextView)view.findViewById(R.id.endTimeText);
        _deleteButton = (Button)view.findViewById(R.id.deleteButton);

        if (!_deleteVisible)
            _deleteButton.setVisibility(View.GONE);

        if (_isNew) {
            if (_startTime.get(Calendar.MINUTE) <= 30) {
                _startTime.set(Calendar.MINUTE, 30);
            } else {
                _startTime.add(Calendar.HOUR_OF_DAY, 1);
                _startTime.set(Calendar.MINUTE, _startTime.getActualMinimum(Calendar.MINUTE));
            }
        }
        _startTime.set(Calendar.SECOND, _startTime.getActualMinimum(Calendar.SECOND));
        _startTime.set(Calendar.MILLISECOND, _startTime.getActualMinimum(Calendar.MILLISECOND));

        if (_isNew) {
            _endTime = (Calendar) _startTime.clone();
            _endTime.add(Calendar.HOUR_OF_DAY, 1);
        }

        final SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());
        final SimpleDateFormat timeFormat = new SimpleDateFormat(getString(R.string.time_format), Locale.getDefault());

        _startDateField.setText(dateFormat.format(_startTime.getTime()));
        _startTimeField.setText(timeFormat.format(_startTime.getTime()));
        _endDateField.setText(dateFormat.format(_endTime.getTime()));
        _endTimeField.setText(timeFormat.format(_endTime.getTime()));


        final DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                _startTime.set(year, month, day);
                _startDateField.setText(dateFormat.format(_startTime.getTime()));
            }
        };

        final TimePickerDialog.OnTimeSetListener startTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                _startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                _startTime.set(Calendar.MINUTE, minute);
                _startTimeField.setText(timeFormat.format(_startTime.getTime()));
            }
        };

        final DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                _endTime.set(year, month, day);
                _endDateField.setText(dateFormat.format(_endTime.getTime()));
            }
        };

        final TimePickerDialog.OnTimeSetListener endTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                _endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                _endTime.set(Calendar.MINUTE, minute);
                _endTimeField.setText(timeFormat.format(_endTime.getTime()));
            }
        };

        _startDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), startDateListener,
                        _startTime.get(Calendar.YEAR), _startTime.get(Calendar.MONTH),
                        _startTime.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        _startTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(getContext(), startTimeListener,
                        _startTime.get(Calendar.HOUR_OF_DAY), _startTime.get(Calendar.MINUTE), false).show();

            }
        });

        _endDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), endDateListener,
                        _endTime.get(Calendar.YEAR), _endTime.get(Calendar.MONTH),
                        _endTime.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        _endTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(getContext(), endTimeListener,
                        _endTime.get(Calendar.HOUR_OF_DAY), _endTime.get(Calendar.MINUTE), false).show();

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

        _deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _listener.onDateTimeSelectDelete(_startTime.getTime(), _endTime.getTime());
                dismiss();
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
                    if (_endTime.compareTo(_startTime) <= 0) {
                        Toast.makeText(getContext(), R.string.fragment_date_time_select_range_error, Toast.LENGTH_LONG).show();
                    } else {
                        _listener.onDateTimeSelectPositive(_startTime.getTime(), _endTime.getTime());
                        dismiss();
                    }
                }
            });
        }
    }
}
