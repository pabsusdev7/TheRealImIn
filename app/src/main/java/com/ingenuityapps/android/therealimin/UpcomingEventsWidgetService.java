package com.ingenuityapps.android.therealimin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ingenuityapps.android.therealimin.data.Event;
import com.ingenuityapps.android.therealimin.utilities.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;

public class UpcomingEventsWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new UpcomingEventsRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class UpcomingEventsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{

    private final String TAG = UpcomingEventsRemoteViewsFactory.class.getSimpleName();
    Context mContext;
    List<Event> mUpcomingEventsList;
    private FirebaseFirestore db;
    private FirebaseUser mUser;
    private SharedPreferences mSharedPreferences;


    public UpcomingEventsRemoteViewsFactory(Context applicationContext, Intent intent)
    {
        mContext = applicationContext;
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mSharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
    }

    @Override
    public void onCreate() {
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onDataSetChanged() {

        try {
            mUpcomingEventsList = mUser!=null && mSharedPreferences.contains(Constants.SHARED_PREF_ORGID) ? getUpcomingEvents() : null;
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private List<Event> getUpcomingEvents() throws ExecutionException, InterruptedException {

        List<Event> events = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,2);//Display all events before the day after tomorrow

        DocumentReference orgIdReference = db.collection(Constants.FIRESTORE_ORGANIZATION).document(mSharedPreferences.getString(Constants.SHARED_PREF_ORGID,null));

        QuerySnapshot result = Tasks.await(db.collection(Constants.FIRESTORE_EVENT)
                .whereGreaterThan(Constants.FIRESTORE_EVENT_STARTTIME,new Date(Calendar.getInstance().getTimeInMillis() - (Constants.MAX_MINUTES_CHECKIN_BEFOREEVENT * Constants.ONE_MINUTE_IN_MILLIS)))
                .whereLessThan(Constants.FIRESTORE_EVENT_STARTTIME, calendar.getTime())
                .whereEqualTo(Constants.FIRESTORE_EVENT_ORGID,orgIdReference)
                .whereEqualTo(Constants.FIRESTORE_EVENT_ACTIVE, true)
                .orderBy(Constants.FIRESTORE_EVENT_STARTTIME,Query.Direction.ASCENDING)
                .get());


        for(QueryDocumentSnapshot document:result)
        {
            Event event = new Event(document.getId(), document.get(Constants.FIRESTORE_EVENT_DESCRIPTION).toString(), document.getTimestamp(Constants.FIRESTORE_EVENT_STARTTIME), document.getTimestamp(Constants.FIRESTORE_EVENT_ENDTIME), document.getBoolean(Constants.FIRESTORE_EVENT_REQUIRED));
            if(!event.getRequired()){
                event.setRequired(Tasks.await(document.getReference()
                        .collection(Constants.FIRESTORE_EVENT_REQUIREDATTENDEE)
                        .document(mUser.getEmail())
                        .get()).exists());
            }
            events.add(event);
        }

        return events;


    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if(mUpcomingEventsList == null) return 0;
        return mUpcomingEventsList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        if(mUpcomingEventsList == null || mUpcomingEventsList.size()==0)return null;


        SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("MM/dd HH:mm");

        Event event = mUpcomingEventsList.get(position);

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.attendance_list_item);

        views.setTextViewText(R.id.tv_event_title, event.getDescription());
        try {
            views.setTextViewText(R.id.tv_event_date, timeFormatter.format(formatter.parse(event.getStarttime().toDate().toString())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(event.getRequired())
            views.setImageViewResource(R.id.iv_event_checkin, R.drawable.ic_required);
        else
            views.setViewVisibility(R.id.iv_event_checkin, View.GONE);

        Intent fillInIntent = new Intent();
        views.setOnClickFillInIntent(R.id.tv_event_title,fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
