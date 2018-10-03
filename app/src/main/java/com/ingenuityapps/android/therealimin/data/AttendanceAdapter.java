package com.ingenuityapps.android.therealimin.data;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ingenuityapps.android.therealimin.CheckInActivity;
import com.ingenuityapps.android.therealimin.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by pabloalbuja on 6/2/18.
 */

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceAdapterViewHolder> {

    private String[] mAttendanceData;

    private final AttendanceAdapterOnClickHandler mClickHandler;
    private static final String TAG = AttendanceAdapter.class.getSimpleName();

    public interface AttendanceAdapterOnClickHandler{
        void onClick(String attendanceForEvent);

    }

    public AttendanceAdapter(AttendanceAdapterOnClickHandler clickHandler)
    {
        mClickHandler = clickHandler;
    }

    public class AttendanceAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mAttendanceTextView;
        public final TextView mAttendanceEventTitle;
        public final TextView mAttendanceEventDate;
        public final ImageView mAttendanceEventCI;

        public AttendanceAdapterViewHolder(View view)
        {
            super(view);
            mAttendanceTextView = (TextView) view.findViewById(R.id.tv_attendance_data);
            mAttendanceEventTitle = (TextView) view.findViewById(R.id.tv_event_title);
            mAttendanceEventDate = (TextView) view.findViewById(R.id.tv_event_date);
            mAttendanceEventCI = (ImageView) view.findViewById(R.id.iv_event_checkin);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            String attendanceForEvent = mAttendanceData[adapterPosition];
            mClickHandler.onClick(attendanceForEvent);

        }
    }


    @Override
    public AttendanceAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.attendance_list_item;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = layoutInflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);

        return new AttendanceAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AttendanceAdapterViewHolder holder, int position) {

        SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
        SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy");

        String attendanceForEvent = mAttendanceData[position];
        holder.mAttendanceTextView.setText(attendanceForEvent);

        try {
            String [] attendanceForEventDetail = attendanceForEvent.split("\\|");
            holder.mAttendanceEventTitle.setText(attendanceForEventDetail[1]);
            holder.mAttendanceEventDate.setText(simpleDate.format(formatter.parse(attendanceForEventDetail[2])));
            holder.mAttendanceEventCI.setImageResource(!attendanceForEventDetail[8].equals("0") ? R.drawable.ic_check_out : R.drawable.ic_check_in);

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        if(mAttendanceData == null) return 0;

        return mAttendanceData.length;
    }

    public void setmAttendanceData(String[] attendanceData)
    {
        mAttendanceData = attendanceData;
        notifyDataSetChanged();
    }



}
