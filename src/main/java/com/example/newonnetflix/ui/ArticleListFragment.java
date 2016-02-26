package com.example.newonnetflix.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.newonnetflix.R;
import com.example.newonnetflix.data.MovieLoader;
import com.example.newonnetflix.data.MoviesContract;
import com.example.newonnetflix.data.UpdaterService;


public class ArticleListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {


    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    final GridLayoutManager mSglm = new GridLayoutManager(getActivity(), 2);
    ;


    public ArticleListFragment() {

    }


    public interface Callback {

        public void onItemSelected(Uri idUri);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);


    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        getLoaderManager().initLoader(0, null, this);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.activity_article_list, container, false);

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);

        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_refresh_layout);


        return mRootView;
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
        return MovieLoader.newAllArticlesInstance(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);

        mRecyclerView.setLayoutManager(mSglm);


    }


    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            mSglm.onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mSglm.onSaveInstanceState());
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
            View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //added by tony

                    Uri contentUri = MoviesContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()));

                    ((Callback) getActivity()).onItemSelected(contentUri);


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
                    ImageLoaderHelper.getInstance(getActivity()).getImageLoader());
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


}
