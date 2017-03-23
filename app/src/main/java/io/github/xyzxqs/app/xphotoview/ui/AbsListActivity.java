package io.github.xyzxqs.app.xphotoview.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import io.github.xyzxqs.app.xphotoview.R;
import io.github.xyzxqs.libs.xrv.XrvAdapter;

/**
 * @author xyzxqs (xyzxqs@gmail.com)
 */

public abstract class AbsListActivity extends AppCompatActivity {
    protected RecyclerView recyclerView;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected LinearLayoutManager linearLayoutManager;

    protected XrvAdapter adapter;

    protected boolean isLoadingMoreRunning = false;
    protected boolean isLoadingMoreToLast = false;

    private boolean isRefreshing = false;

    public abstract void onViewCreated(@Nullable Bundle savedInstanceState);

    protected void onRequestRefresh() {
    }

    protected void onRequestLoadingMore() {
    }

    public final void setRefreshing(boolean isRefreshing) {
        swipeRefreshLayout.setRefreshing(isRefreshing);
        this.isRefreshing = isRefreshing;
    }

    public final boolean isRefreshing() {
        return isRefreshing;
    }

    public final void setSwipeRefreshEnabled(boolean enable) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(enable);
        }
    }

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_refreshable_recycler_view);

        if (adapter == null) {
            adapter = new XrvAdapter();
        }

        linearLayoutManager = new LinearLayoutManager(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRefreshing) {
                    onRequestRefresh();
                    isRefreshing = true;
                }
            }
        });

        setSwipeRefreshEnabled(false);

        onViewCreated(savedInstanceState);

        setupLoadMore();
    }

    private void setupLoadMore() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy < 0 || isLoadingMoreRunning || isLoadingMoreToLast) {
                    return;
                }

                final int itemCount = linearLayoutManager.getItemCount();
                final int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                final boolean isBottom = (lastVisiblePosition >= itemCount - 3);
                if (isBottom) {
                    onRequestLoadingMore();
                }

            }
        });
    }
}
