package com.example.newonnetflix.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.newonnetflix.R;
import com.example.newonnetflix.data.MovieLoader;
import com.example.newonnetflix.data.UpdaterService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class ArticleListActivity extends ActionBarActivity implements
        ArticleListFragment.Callback, LoaderManager.LoaderCallbacks<Cursor> {

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        final View toolbarContainerView = findViewById(R.id.toolbar_container);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null && !getIntent().hasExtra("flick")) {
            refresh();
        }


        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        if (findViewById(R.id.movie_detail_container) != null) {

            mTwoPane = true;

            if (savedInstanceState == null || getIntent().hasExtra("flick")) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }


        String test = getIntent().getStringExtra("flick");
        if (getIntent().hasExtra("flick")) {
            Log.v("test", test);
            Uri testUri = Uri.parse("content://com.example.newonnetflix/items/" + test);


            onItemSelected(testUri);
            Log.v("testURI", testUri.toString());
        } else Log.v("string test is null", getIntent().toString());


    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //now getIntent() should always return the last received intent
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (findViewById(R.id.movie_detail_container) != null) {

            mTwoPane = true;

            if (getIntent().hasExtra("flick")) {
                Log.v("onResume", "hasExtra flick");

                getFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        String test = getIntent().getStringExtra("flick");
        if (getIntent().hasExtra("flick")) {
            Log.v("test", test);
            Uri testUri = Uri.parse("content://com.example.newonnetflix/items/" + test);


            onItemSelected(testUri);
            Log.v("testURI", testUri.toString());
        } else Log.v("string test is null", getIntent().toString());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_grid_view, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));

            return true;
        }
        return super.onOptionsItemSelected(item);


    }


    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return MovieLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }


    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;

        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(MovieLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(MovieLoader.Query.TITLE));
            holder.titleView.setContentDescription("Title: " + holder.titleView.getText());

            holder.subtitleView.setText("Added " +
                    mCursor.getString(MovieLoader.Query.PUBLISHED_DATE));
            holder.subtitleView.setContentDescription(holder.subtitleView.getText());


            holder.thumbnailView.setImageUrl(
                    mCursor.getString(MovieLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());
            holder.thumbnailView.setAspectRatio(mCursor.getFloat(MovieLoader.Query.ASPECT_RATIO));

        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public DynamicHeightNetworkImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {

            Bundle args = new Bundle();
            args.putParcelable(MovieDetailFragment.DETAIL_URI, contentUri);

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(args);

            getFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();

            Log.d("mTwoPane2:", "true");


        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class)
                    .setData(contentUri);

            startActivity(intent);

            Log.d("else2", "called");

        }

    }


}
