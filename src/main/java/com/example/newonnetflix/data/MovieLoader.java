package com.example.newonnetflix.data;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

/**
 * Helper for loading a list of articles or a single article.
 */
public class MovieLoader extends CursorLoader {
    public static MovieLoader newAllArticlesInstance(Context context) {
        return new MovieLoader(context, MoviesContract.Items.buildDirUri());
    }

    public static MovieLoader newInstanceForItemId(Context context, long itemId) {
        return new MovieLoader(context, MoviesContract.Items.buildItemUri(itemId));
    }

    public MovieLoader(Context context, Uri uri) {
        super(context, uri, Query.PROJECTION, null, null, MoviesContract.Items.DEFAULT_SORT);
    }

    public interface Query {
        String[] PROJECTION = {
                MoviesContract.Items._ID,
                MoviesContract.Items.TITLE,
                MoviesContract.Items.PUBLISHED_DATE,
                MoviesContract.Items.AUTHOR,
                MoviesContract.Items.THUMB_URL,
                MoviesContract.Items.PHOTO_URL,
                MoviesContract.Items.ASPECT_RATIO,
                MoviesContract.Items.BODY,
        };

        int _ID = 0;
        int TITLE = 1;
        int PUBLISHED_DATE = 2;
        int AUTHOR = 3;
        int THUMB_URL = 4;
        int PHOTO_URL = 5;
        int ASPECT_RATIO = 6;
        int BODY = 7;
    }
}
