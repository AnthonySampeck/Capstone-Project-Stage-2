package com.example.newonnetflix;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.newonnetflix.data.MovieLoader;
import com.example.newonnetflix.data.MoviesContract;

/**
 * WidgetService is the {@link RemoteViewsService} that will return our RemoteViewsFactory
 */
public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;


            @Override
            public void onCreate() {
            }


            @Override
            public void onDataSetChanged() {
                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver()
                        .query(MoviesContract
                                .Items.buildDirUri()
                                , null, null, null, null);

                Binder.restoreCallingIdentity(identityToken);
            }


            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }

            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews remoteViews = new RemoteViews(getPackageName(),
                        android.R.layout.simple_list_item_1);


                remoteViews.setTextViewText(android.R.id.text1, data.getString(MovieLoader.Query.PUBLISHED_DATE));


                //Click intent
                final Intent intentFill = new Intent();

                String movieID = data.getString(MovieLoader.Query._ID);

                intentFill.putExtra("flick", movieID);

                remoteViews.setOnClickFillInIntent(android.R.id.text1, intentFill);


                return remoteViews;

            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
            }


            @Override
            public int getViewTypeCount() {
                return 1;
            }


            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(MovieLoader.Query._ID);
                return position;
            }


            @Override
            public boolean hasStableIds() {
                return true;
            }


        };
    }
}