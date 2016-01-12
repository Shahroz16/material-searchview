package com.shahroz.svlibrary.widgets;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shahroz.svlibrary.R;
import com.shahroz.svlibrary.interfaces.onSearchActionsListener;
import com.shahroz.svlibrary.interfaces.onSearchListener;
import com.shahroz.svlibrary.interfaces.onSimpleSearchActionsListener;
import com.shahroz.svlibrary.utils.Util;

/**
 * Created by shahroz on 1/8/2016.
 */

public class MaterialSearchView extends FrameLayout implements View.OnClickListener, onSearchActionsListener {

    private final EditText mSearchEditText;
    private final ImageView mClearSearch;
    private final ProgressBar mProgressBar;
    private onSearchListener mOnSearchListener;
    private View lineDivider;
    private CardView cardLayout;
    private RelativeLayout searchLayout;
    private ImageView backArrowImg;
    private ListView mFrameLayout;
    private Context mContext;
    private SearchViewResults searchViewResults;
    private FrameLayout progressBarLayout;
    private onSimpleSearchActionsListener searchListener;
    private TextView noResultsFoundText;

    public void setHintText(String hint) {
        mSearchEditText.setHint(hint);
    }

    public CardView getCardLayout() {
        return cardLayout;
    }


    public ListView getListview() {
        return mFrameLayout;
    }

    public View getLineDivider() {
        return lineDivider;
    }

    final Animation fade_in;
    final Animation fade_out;

    @Override
    public void onItemClicked(String item) {
        this.searchListener.onItemClicked(item);
    }

    @Override
    public void showProgress(boolean show) {
        if(show) {
            progressBarLayout.setVisibility(VISIBLE);
            mProgressBar.setVisibility(VISIBLE);
            noResultsFoundText.setVisibility(GONE);
        }
        else {
            progressBarLayout.setVisibility(GONE);
        }

    }

    @Override
    public void listEmpty() {
        progressBarLayout.setVisibility(VISIBLE);
        noResultsFoundText.setVisibility(VISIBLE);
        mProgressBar.setVisibility(GONE);
    }

    @Override
    public void onScroll() {
        this.searchListener.onScroll();
    }

    @Override
    public void error(String localizedMessage) {
        this.searchListener.error(localizedMessage);
    }

    public MaterialSearchView(final Context context) {
        this(context, null);
    }

    public MaterialSearchView(final Context context, final AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public MaterialSearchView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        final LayoutInflater factory = LayoutInflater.from(context);
        factory.inflate(R.layout.toolbar_searchview, this);
        mContext = context;
        cardLayout = (CardView) findViewById(R.id.card_search);
        mFrameLayout = (ListView) findViewById(R.id.material_search_container);
        searchLayout = (RelativeLayout) findViewById(R.id.view_search);
        lineDivider = findViewById(R.id.line_divider);
        noResultsFoundText = (TextView) findViewById(R.id.textView13);
        mSearchEditText = (EditText)findViewById(R.id.edit_text_search);
        backArrowImg = (ImageView) findViewById(R.id.image_search_back);
        mClearSearch = (ImageView) findViewById(R.id.clearSearch);
        progressBarLayout = (FrameLayout) findViewById(R.id.progressLayout);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mSearchEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        fade_in = AnimationUtils.loadAnimation(getContext().getApplicationContext(), android.R.anim.fade_in);
        fade_out = AnimationUtils.loadAnimation(getContext().getApplicationContext(), android.R.anim.fade_out);

        mClearSearch.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        backArrowImg.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mOnSearchListener != null) {
                    mOnSearchListener.onSearch(getSearchQuery());
                    onQuery(getSearchQuery());
                }
                toggleClearSearchButton(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSearchEditText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER ||
                                keyCode == KeyEvent.KEYCODE_SEARCH)) {
                    final String query = getSearchQuery();
                    if (!TextUtils.isEmpty(query) && mOnSearchListener != null) {
                        mOnSearchListener.onSearch(query);
                    }
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.image_search_back).setOnClickListener(this);
        mClearSearch.setOnClickListener(this);
        setVisibility(View.GONE);
        clearAnimation();
    }

    public void setOnSearchListener(final onSearchListener l) {
        mOnSearchListener = l;
    }

    public void setSearchResultsListener(onSimpleSearchActionsListener listener) {
        this.searchListener = listener;
    }

    public void setSearchQuery(final String query) {
        mSearchEditText.setText(query);
        toggleClearSearchButton(query);
    }

    public String getSearchQuery() {
        return mSearchEditText.getText() != null ? mSearchEditText.getText().toString() : "";
    }


    public boolean isSearchViewVisible() {
        return getVisibility() == View.VISIBLE;
    }

    // Show the SearchView
    public void display() {
        if (isSearchViewVisible()) return;
        setVisibility(View.VISIBLE);
        mOnSearchListener.searchViewOpened();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Animator animator = ViewAnimationUtils.createCircularReveal(cardLayout,
                    cardLayout.getWidth() - Util.dpToPx(getContext(), 56),
                    Util.dpToPx(getContext(), 23),
                    0,
                    (float) Math.hypot(cardLayout.getWidth(), cardLayout.getHeight()));
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    searchLayout.setVisibility(View.VISIBLE);
                    searchLayout.startAnimation(fade_in);
                    ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            cardLayout.setVisibility(View.VISIBLE);
            if (cardLayout.getVisibility() == View.VISIBLE) {
                animator.setDuration(300);
                animator.start();
                cardLayout.setEnabled(true);
            }
            fade_in.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    mFrameLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            cardLayout.setVisibility(View.VISIBLE);
            cardLayout.setEnabled(true);

            mFrameLayout.setVisibility(View.VISIBLE);
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }

    }

    public void onQuery(String query) {
        String trim = query.trim();
        if(TextUtils.isEmpty(trim)){
            progressBarLayout.setVisibility(GONE);
        }
        if (searchViewResults != null) {
            searchViewResults.updateSequence(trim);
        } else {
            searchViewResults = new SearchViewResults(mContext,trim);
            searchViewResults.setListView(mFrameLayout);
            searchViewResults.setSearchProvidersListener(this);
        }
    }


    public void hide() {
        if (!isSearchViewVisible()) return;
        mOnSearchListener.searchViewClosed();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Animator animatorHide = ViewAnimationUtils.createCircularReveal(cardLayout,
                    cardLayout.getWidth() - Util.dpToPx(getContext(), 56),
                    Util.dpToPx(getContext(), 23),
                    (float) Math.hypot(cardLayout.getWidth(), cardLayout.getHeight()),
                    0);
            animatorHide.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    searchLayout.startAnimation(fade_out);
                    searchLayout.setVisibility(View.INVISIBLE);
                    cardLayout.setVisibility(View.GONE);
                    ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchLayout.getWindowToken(), 0);

                    mFrameLayout.setVisibility(View.GONE);
                    clearSearch();
                    setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animatorHide.setDuration(300);
            animatorHide.start();
        } else {
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchLayout.getWindowToken(), 0);
            searchLayout.startAnimation(fade_out);
            searchLayout.setVisibility(View.INVISIBLE);
            cardLayout.setVisibility(View.GONE);
            clearSearch();
            setVisibility(View.GONE);
        }
    }

    public EditText getSearchView(){
        return mSearchEditText;
    }

    public static WindowManager.LayoutParams getSearchViewLayoutParams(final Activity activity) {
        final Rect rect = new Rect();
        final Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        final int statusBarHeight = rect.top;

        final TypedArray actionBarSize = activity.getTheme().obtainStyledAttributes(
                new int[] {R.attr.actionBarSize});
        actionBarSize.recycle();
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                rect.right /* This ensures we don't go under the navigation bar in landscape */,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = statusBarHeight;
        return params;
    }

    private void toggleClearSearchButton(final CharSequence query) {
        mClearSearch.setVisibility(!TextUtils.isEmpty(query) ? View.VISIBLE : View.INVISIBLE);
    }

    private void clearSearch() {
        mSearchEditText.setText("");
        mClearSearch.setVisibility(View.INVISIBLE);
    }

    private void onCancelSearch() {
        if (mOnSearchListener != null) {
            mOnSearchListener.onCancelSearch();
        } else {
            hide();
        }
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP &&
                event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            onCancelSearch();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.image_search_back) {
            onCancelSearch();
        } else if (id == R.id.clearSearch) {
            clearSearch();
        }
    }
}