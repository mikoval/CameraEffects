package com.ShaderProjects.shadercam;

import android.content.Context;

import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

public class CustomScroller extends RecyclerView {

    Context context;
    ChangeHandler handler;

    public CustomScroller(Context context) {
        super(context);
        init();
        this.context = context;
    }

    public CustomScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomScroller(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {

        velocityX *= 0.5;
        return super.fling(velocityX, velocityY);
    }

    private void init() {
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(this);
        setHasFixedSize(true);

        SnapScrollListener snapListeer = new SnapScrollListener(snapHelper);
        setOnScrollListener(snapListeer);
    }

    public void setChangeHandler(ChangeHandler handler) {
        this.handler = handler;
    }

    public interface ChangeHandler {
        public void onChanged(View view);
    }

    private class SnapScrollListener extends OnScrollListener {

        private final boolean NOTIFY_ON_SCROLL = true;
        private final boolean NOTIFY_ON_SCROLL_STATE_IDLE = false;

        private View oldView;

        private SnapHelper snapHelper;
        private SnapScrollListener(SnapHelper snapHelper){
            this.snapHelper = snapHelper;

        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                updatePosition(recyclerView);
            }

            super.onScrollStateChanged(recyclerView, newState);
        }

        private void updatePosition (RecyclerView recyclerView){
            LayoutManager layoutManager = recyclerView.getLayoutManager();
            View newView = snapHelper.findSnapView(layoutManager);


            if (oldView != newView) {
                //Toast.makeText(getContext(),"DONE SCROLLING",Toast.LENGTH_SHORT).show();
                handler.onChanged(newView);
                oldView = newView;
            }

        }

    }

}