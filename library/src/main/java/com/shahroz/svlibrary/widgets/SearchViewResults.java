package com.shahroz.svlibrary.widgets;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.shahroz.svlibrary.interfaces.onSearchActionsListener;

import java.util.ArrayList;

/**
 * Created by shahroz on 1/8/2016.
 */
public class SearchViewResults implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    private static final int TRIGGER_SEARCH = 1;
    private static final long SEARCH_TRIGGER_DELAY_IN_MS = 400;
    private ListView mListView;
    private String sequence;
    private int mPage;
    private SearchTask mSearch;
    private Handler mHandler;
    private boolean isLoadMore;
    private ArrayAdapter mAdapter;
    private onSearchActionsListener mListener;
    private ArrayList<String> searchList;
    /*
    * Used Handler in case implement Search remotely
    * */

    public SearchViewResults(Context context, String searchQuery) {
        sequence = searchQuery;
        searchList = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(context,android.R.layout.simple_list_item_1, searchList);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_SEARCH) {
                    clearAdapter();
                    String sequence = (String) msg.obj;
                    mSearch = new SearchTask();
                    mSearch.execute(sequence);
                }
                return false;
            }
        });
    }

    public void setListView(ListView listView) {
        mListView = listView;
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);
        mListView.setAdapter(mAdapter);
        updateSequence();
    }

    public void updateSequence(String s) {
        sequence = s;
        updateSequence();
    }

    private void updateSequence() {
        mPage = 0;
        isLoadMore = true;

        if (mSearch != null) {
            mSearch.cancel(false);
        }
        if (mHandler != null) {
            mHandler.removeMessages(TRIGGER_SEARCH);
        }
        if (!sequence.isEmpty()) {
            Message searchMessage = new Message();
            searchMessage.what = TRIGGER_SEARCH;
            searchMessage.obj = sequence;
            mHandler.sendMessageDelayed(searchMessage, SEARCH_TRIGGER_DELAY_IN_MS);
        } else {
            isLoadMore = false;
            clearAdapter();
        }
    }

    private void clearAdapter() {
        mAdapter.clear();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListener.onItemClicked((String) mAdapter.getItem(position));

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING) {
            mListener.onScroll();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean loadMore = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount;
        if (loadMore && isLoadMore) {
            mPage++;
            isLoadMore = false;
            mSearch = new SearchTask();
            mSearch.execute(sequence);
        }
    }

    /*
    * Implement the Core search functionality here
    * Could be any local or remote
    */
    private ArrayList<String> findItem(String query, int page) {
        ArrayList<String> result = new ArrayList<>();
        result.add(query);
        return result;
    }

    public void setSearchProvidersListener(onSearchActionsListener listener) {
        this.mListener = listener;
    }

    /*
    * Search for the item asynchronously
    */
    private class SearchTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mListener.showProgress(true);
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            String query = params[0];
            ArrayList<String> result = findItem(query, mPage);
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
            if (!isCancelled()) {
                mListener.showProgress(false);
                if (mPage == 0 && result.isEmpty()) {
                    mListener.listEmpty();
                } else {
                    mAdapter.notifyDataSetInvalidated();
                    mAdapter.addAll(result);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

}


