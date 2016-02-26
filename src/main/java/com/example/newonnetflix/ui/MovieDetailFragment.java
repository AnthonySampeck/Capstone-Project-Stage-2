package com.example.newonnetflix.ui;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.newonnetflix.R;
import com.example.newonnetflix.data.MovieLoader;
import com.example.newonnetflix.data.MoviesContract;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class MovieDetailFragment extends ArticleListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MovieDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ObservableScrollView mScrollView;
    private FrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;

    private Tracker mTracker;
    private String mTitle;

    static final String DETAIL_URI = "76341";
    private Uri mUri;
    private String mStringUri;


    public MovieDetailFragment() {
    }

    public static MovieDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);


        } else if (getArguments() != null) {
            mUri = getArguments().getParcelable(MovieDetailFragment.DETAIL_URI);
            mStringUri = mUri.toString();

            mItemId = MoviesContract.Items.getItemId(mUri);

        }


        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
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


        //analytics
        AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();


        Bundle arguments = getArguments();
        if (arguments != null) {


        }


        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mDrawInsetsFrameLayout = (FrameLayout)
                mRootView.findViewById(R.id.draw_insets_frame_layout);


        mScrollView = (ObservableScrollView) mRootView.findViewById(R.id.scrollview);
        mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
            @Override
            public void onScrollChanged() {
                mScrollY = mScrollView.getScrollY();

                mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
                updateStatusBar();
            }
        });

        mPhotoView = (ImageView) mRootView.findViewById(R.id.backdrop);
        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        mStatusBarColorDrawable = new ColorDrawable(0);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        updateStatusBar();
        return mRootView;
    }

    private void updateStatusBar() {
        int color = 0;
        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
        }
        mStatusBarColorDrawable.setColor(color);
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }


        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        ImageView posterView = (ImageView) mRootView.findViewById(R.id.backdrop);


        if (mCursor != null) {


            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);

            mTitle = mCursor.getString(MovieLoader.Query.TITLE);

            titleView.setText(mCursor.getString(MovieLoader.Query.TITLE));
            titleView.setContentDescription("Title: " + titleView.getText());

            //analytics
            mTracker.setScreenName("Details~" + mTitle);
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());


            bylineView.setText(Html.fromHtml("Added "
                    +
                    mCursor.getString(MovieLoader.Query.PUBLISHED_DATE)));
            bylineView.setContentDescription(bylineView.getText());


            bodyView.setText(Html.fromHtml(mCursor.getString(MovieLoader.Query.BODY)));
            bodyView.setContentDescription("Description: " + bodyView.getText());


            posterView.setContentDescription("Image for " + titleView.getText());

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(MovieLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                                mRootView.findViewById(R.id.meta_bar)
                                        .setBackgroundColor(mMutedColor);
                                updateStatusBar();
                            }
                        }


                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });


        } else {

            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return MovieLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();


            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {


            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();


    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();


    }

    public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        return mIsCard
                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
                : mPhotoView.getHeight() - mScrollY;
    }


}
