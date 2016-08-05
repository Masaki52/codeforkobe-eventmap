package org.codeforkobe.eventmap.ui;

import org.codefork.eventmap.model.Calendar;
import org.codefork.eventmap.model.Event;
import org.codefork.eventmap.model.Property;
import org.codeforkobe.eventmap.R;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.util.Scanner;

/**
 * @author ISHIMARU Sohei on 2016/07/01.
 */
public class EventLoadTask extends AsyncTask<Void, Void, Calendar> {

    private static final String LOG_TAG = "EventLoadTask";

    private Listener mListener;

    private Context mContext;

    public EventLoadTask(Context context) {
        mContext = context;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    protected Calendar doInBackground(Void... params) {
        Log.d(LOG_TAG, "# doInBackground(Void...)");
        Calendar calendar = new Calendar();
        InputStream is = mContext.getResources().openRawResource(R.raw.sanda_event);
        /* ical4j大きすぎる...汚いけど自前で... */

        /* icsファイルのパース処理 */
        Scanner scanner = new Scanner(is);
        Event event = null;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line != null && 0 < line.length() && line.contains(":")) {
                String[] values = line.split(":");

                switch (values[0]) {
                    case Property.BEGIN:
                        if (Property.VEVENT.equals(values[1])) {
                            event = new Event();
                        }
                        break;
                    case Property.END:
                        if (Property.VEVENT.equals(values[1])) {
                            if (event != null) {
                                try {
                                    calendar.addEvent(event.clone());
                                } catch (CloneNotSupportedException e) {
                                    e.printStackTrace();
                                }
                            }
                            event = null;
                        }
                        break;
                    default:
                        if (event == null) {
                            if (1 < values.length) {
                                handleCalendar(values[0], values[1], calendar);
                            }
                        } else {
                            if (1 < values.length) {
                                handleEvent(values[0], values[1], event);
                            }
                        }
                }
            }
        }

        scanner.close();
        return calendar;
    }

    private void handleCalendar(String key, String value, Calendar calendar) {
        if (key == null) {
            return;
        }
        switch (key) {
            case Property.METHOD:
                calendar.setMethod(value);
                break;
            case Property.VERSION:
                calendar.setVersion(value);
                break;
            case Property.PRODID:
                calendar.setProductIdentifier(value);
                break;
            case Property.CALSCALE:
                calendar.setCalendarScale(value);
                break;
            case Property.TZID:
                calendar.setTimeZoneIdentifier(value);
                break;
            case Property.TZOFFSETFROM:
                calendar.setTimeZoneOffsetFrom(value);
                break;
            case Property.TZOFFSETTO:
                calendar.setTimeZoneOffsetTo(value);
                break;
            case Property.TZNAME:
                calendar.setTimeZoneName(value);
                break;
        }
    }

    private void handleEvent(String key, String value, Event event) {
        if (key == null || event == null) {
            return;
        }
        switch (key) {
            case Property.UID:
                event.setUid(value);
                break;
            case Property.DTSTAMP:
                event.setDateTimeStamp(value);
                break;
            case Property.SUMMARY:
                event.setSummary(value);
                break;
            case Property.DESCRIPTION:
                event.setDescription(value);
                break;
            case Property.DTSTART:
                event.setDateTimeStart(value);
                break;
            case Property.DTEND:
                event.setDateTimeEnd(value);
                break;
            case Property.LOCATION:
                event.setLocation(value);
                break;
            case Property.GEO:
                event.setGeoPoint(value);
                String[] latLng = value.split(",");
                if (1 < latLng.length) {
                    event.setLatitude(Double.parseDouble(latLng[0]));
                    event.setLongitude(Double.parseDouble(latLng[1]));
                }
                break;
            case Property.CONTACT:
                event.setContact(value);
                break;
            case Property.TRANSP:
                event.setTransparent(value);
                break;
        }
    }

    @Override
    protected void onPostExecute(Calendar calendar) {
        super.onPostExecute(calendar);
        if (mListener != null) {
            mListener.onLoad(calendar);
        }
    }

    public interface Listener {
        void onLoad(Calendar calendar);
    }
}
