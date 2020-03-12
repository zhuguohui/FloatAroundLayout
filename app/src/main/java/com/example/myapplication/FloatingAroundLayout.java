package com.example.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FloatingAroundLayout extends RelativeLayout {
    private static final String TAG = FloatingAroundLayout.class.getSimpleName();

    int pointCount = 5;
    float maxRadiusRatio = 0.9f;
    float minRadiusRatio = 0.5f;
    List<Point> mPointList = null;
    int mStep = Integer.MAX_VALUE / 2;
    int layoutWidth, layoutHeight;
    int minPadding = 30;
    private ValueAnimator animator;

    public enum Direction {
        Clockwise,
        Anti_clockwise
    }

    ;
    Direction direction = Direction.Clockwise;

    public FloatingAroundLayout(Context context) {
        this(context, null);
    }

    public FloatingAroundLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "FloatingAroundLayout");

    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutWidth = w;
        layoutHeight = h;


    }

    private class MoveInfo {
        int dx, dy;
        int startLeftMargin, startTopMargin;
    }

    Map<Integer, MoveInfo> currentPointMap = new HashMap<>();

    private void createAnimation() {
        if (direction == Direction.Anti_clockwise) {
            mStep++;
        } else {
            mStep--;
        }
        animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float ratio = (float) animation.getAnimatedValue();
                //  Log.i(TAG,"ratio="+ratio);

                for (int i = 0; i < getChildCount(); i++) {
                    View childAt = getChildAt(i);
                    RelativeLayout.LayoutParams params = (LayoutParams) childAt.getLayoutParams();
                    int index = (int) childAt.getTag();
                    int nextIndex = index + (Math.abs(mStep) % mPointList.size());
                    //获取下一个点
                    if (nextIndex >= mPointList.size()) nextIndex %= mPointList.size();

                    Point point = mPointList.get(nextIndex);
                    point = getNotBeyondBoundaryPointByView(point, childAt);
                    MoveInfo moveDistance;
                    if (ratio == 0) {
                        //记录需要移动的距离
                        Point currentPoint = new Point();
                        moveDistance = new MoveInfo();
                        int measuredHeight = childAt.getMeasuredHeight();

                        currentPoint.x = params.leftMargin + childAt.getMeasuredWidth() / 2;
                        currentPoint.y = params.topMargin + childAt.getMeasuredHeight() / 2;
                        int dx = point.x - currentPoint.x;
                        int dy = point.y - currentPoint.y;
                        moveDistance.dx = dx;
                        moveDistance.dy = dy;
                        moveDistance.startLeftMargin = params.leftMargin;
                        moveDistance.startTopMargin = params.topMargin;
                        currentPointMap.put(Integer.valueOf(i), moveDistance);
                    } else {
                        moveDistance = currentPointMap.get(Integer.valueOf(i));
                    }
                    //计算差值
                    //   Log.i(TAG, "dx=" + moveDistance.dx + " dy=" + moveDistance.dy);
                    int newX = (int) (moveDistance.startLeftMargin + (moveDistance.dx * 1.0f * ratio));
                    int newY = (int) (moveDistance.startTopMargin + (moveDistance.dy * 1.0f * ratio));
                    params.leftMargin = newX;
                    params.topMargin = newY;
                    childAt.setLayoutParams(params);
                }

            }
        });
        animator.setDuration(4000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
//                Log.i(TAG, "onAnimationEnd");
                postDelayed(() -> createAnimation(), 500);
            }
        });
        animator.start();
    }

    private void createView() {

        for (int i = 0; i < adapter.getViewCount(); i++) {
            Point point = mPointList.get(i);
            View view = adapter.getView(i, getContext(), this);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setVisibility(INVISIBLE);
            addView(view, params);
            Point pointByView = getNotBeyondBoundaryPointByView(point, view);
            params.leftMargin = pointByView.x - view.getMeasuredWidth() / 2;
            params.topMargin = pointByView.y - view.getMeasuredHeight() / 2;
            view.setVisibility(VISIBLE);
            view.setTag(i);
        }
    }

    private List<Point> getPointList() {
        mStep = Integer.MAX_VALUE / 2;
        mStep -= mStep % pointCount;
        List<Point> pointList = new ArrayList<>();
        int sw = Math.min(layoutWidth, layoutHeight);
        int maxRadius = (int) (sw / 2 * maxRadiusRatio);//最大半径
        int minRadius = (int) (sw / 2 * minRadiusRatio);//最小半径
        double stepAngle = 2 * Math.PI / pointCount;
        int radiusRange = maxRadius - minRadius;
        Random random = new Random();
        for (int i = 0; i < adapter.getPointSize(); i++) {
            //以X轴正方向为起始边，stepAngle 为步长，绕layout中心旋转，随机生成pointCount个点
            int r = minRadius + random.nextInt(radiusRange);
            Point point = new Point();
            point.x = (int) (layoutWidth / 2 + r * Math.sin(i * stepAngle));
            point.y = (int) (layoutHeight / 2 + r * Math.cos(i * stepAngle));
            pointList.add(point);
        }
        return pointList;
    }

    /**
     * 计算一个中心点是否会超出空间的边界。
     * 如果会就返回一个适合的点
     *
     * @param point
     * @param view
     * @return
     */
    private Point getNotBeyondBoundaryPointByView(Point point, View view) {
        view.measure(MeasureSpec.makeMeasureSpec(layoutWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(layoutHeight, MeasureSpec.AT_MOST));
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();
        Point point1 = new Point();
        point1.x = point.x;
        point1.y = point.y;
        if (point.x > layoutWidth / 2) {
            int beyondWidth = point.x + width / 2 - layoutWidth;
            if (beyondWidth > 0) {
                point1.x = point.x - beyondWidth - minPadding;
            }
        } else {
            int beyondWidth = point.x - width / 2;
            if (beyondWidth < 0) {
                point1.x = point.x - beyondWidth + minPadding;
            }

        }
        if (point.y > layoutHeight / 2) {
            int beyondHeight = point.y + height / 2 - layoutHeight;
            if (beyondHeight > 0) {
                point1.y = point.y - beyondHeight - minPadding;
            }

        } else {
            int beyondHeight = point.y - height / 2;
            if (beyondHeight < 0) {
                point1.y = point.y + beyondHeight + minPadding;
            }
        }


        return point1;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    Adapter adapter;

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        if (adapter != null) {
            if (adapter.getViewCount() > adapter.getPointSize()) {
                throw new IllegalArgumentException("view count must less than point size");
            }
            updateView();
        }
    }

    private void updateView() {
        removeAllViews();
        if (layoutWidth == 0 || layoutHeight == 0) {
            postDelayed(() -> {
                updateView();
            }, 100);
            return;
        }
        mPointList = getPointList();
        createView();
        createAnimation();
    }

    public  interface Adapter {
        int getPointSize();

        int getViewCount();

        View getView(int position, Context context, FloatingAroundLayout floatingAroundLayout);
    }

}
