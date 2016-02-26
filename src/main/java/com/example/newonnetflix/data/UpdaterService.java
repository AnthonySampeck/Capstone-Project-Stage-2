package com.example.newonnetflix.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class UpdaterService extends IntentService {
    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.example.newonnetflix.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.example.newonnetflix.intent.extra.REFRESHING";


    private String mBase_URL = "http://instantwatcher.com/new?content_type=1+2&sort=available_from+desc";


    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String mSort = prefs.getString("sort", "783");

        String fullPath = mBase_URL + "&genres=" + mSort;


        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Log.w(TAG, "Not online, not refreshing.");
            return;
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();

        Uri dirUri = MoviesContract.Items.buildDirUri();

        cpo.add(ContentProviderOperation.newDelete(dirUri).build());



        /* The API database I used to research my final project (ProgrammableWeb) listed Netflix API as publicly available.
        The capstone protocol requires students to submit proposal before starting to code, so I had everything
        planned, approved, and started before registering for my API key and learning the API is only privately available to a few sites.

        I contacted Netflix for an API just for this project, and have gotten promising responses but still no clear answer. In the meantime,
        I'm using an HTML parser library called JSOUP to get the information off of one of the few sites Netflix
        allowed to keep their API (instantwatcher.com). This creates a middle man of getting the data from a site that is getting their
        data from the Netlifx API, and if you do this too many times the instantwatcher site will block your IP for
        datascraping, but as a temporary fix it works until Netflix gets back to me with an API key. I just don't
        want to keep waiting and paying by the month so I thought I'd turn this in as a temporary solution.
        */


        try {

            Document doc = Jsoup.connect(fullPath).get();
            Elements searchResElems = doc.getElementsByClass("box-synopsis-mode");

            for (Element src : searchResElems) {
                String title = src.getElementsByClass("title").text();
                Log.d("titles: ", title);

                String photo_url = src.getElementsByClass("iw-boxart").attr("src");
                Log.d("posters: ", photo_url);

                String id = src.getElementsByClass("title-link").attr("data-title-id");
                Log.d("ids: ", id);


                /*
                The site I'm data scraping has horrible resolution images on the
                search results page, so for each item in search result I'm making a call to the
                detail page where there is a decent quality image. This leads to around 25 rather than 1
                call for the list of new movies on netflix and will get your IP blocked if you were to use
                this app regularly. As soon as I get a Netflix API key I won't have to make so many calls, but
                Netflix is going back and forth between saying they will probably be able to give me one to
                not getting back to me. This is costing me time and money, so I'm turning this in as a
                temporary solution.


                 */

                Document docDets = Jsoup.connect("http://instantwatcher.com/title/" + id + "?device=responsive").get();


                String betterImage = docDets.getElementsByClass("iw-boxart").attr("src");
                String desc = docDets.getElementsByClass("synopsis").text();
                String betterTitle = docDets.getElementsByClass("title").text();


                Log.d("betterImage", betterImage);

                Log.d("desc", desc);


                String published_date = src.getElementsByClass("available-from").text();

                Log.d("added", published_date);

                ContentValues values = new ContentValues();

                values.put(MoviesContract.Items.TITLE, betterTitle);

                values.put(MoviesContract.Items.PHOTO_URL, betterImage);

                values.put(MoviesContract.Items.SERVER_ID, id);
                values.put(MoviesContract.Items.AUTHOR, title);
                values.put(MoviesContract.Items.BODY, desc);
                values.put(MoviesContract.Items.THUMB_URL, betterImage);


                values.put(MoviesContract.Items.ASPECT_RATIO, "1.49925");


                values.put(MoviesContract.Items.PUBLISHED_DATE, published_date);
                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
            }

            getContentResolver().applyBatch(MoviesContract.CONTENT_AUTHORITY, cpo);

        } catch (Throwable t) {
            t.printStackTrace();
        }


        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
    }


}
