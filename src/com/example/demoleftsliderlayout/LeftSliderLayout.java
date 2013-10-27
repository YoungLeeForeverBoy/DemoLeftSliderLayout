package com.example.demoleftsliderlayout;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.Scroller;

public class LeftSliderLayout extends ViewGroup{
    public final static String TAG = "LeftSliderLayout";

    public final static int TOUCH_STATE_IDLE = 0;
    public final static int TOUCH_STATE_SCROLLING = 1;

    private int mTouchState = TOUCH_STATE_IDLE;
    private int mTouchSlop = 0;
    private float mLastMotionX;
    private float mLastMotionY;

    public final static int VELOCITY_UNITS = 1000;
    private int mVelocaityUnits;

    public final static float MINOR_VELOCITY = 150f;
    private int mMinorVelocity;

    public final static int SLIDING_WIDTH = 250;
    private int mSlidingWidth;

    public final static int SHADOW_WIDTH = 10;
    private int mShadowWidth;

    private boolean mIsTouchEventDone = false;
    private boolean mIsOpen = false;
    private boolean mIsEnable = true;
    /**
     * mTapToClose use to close the slider when user had open
     * the slider and tap it to want it back close;
     * */
    private boolean mTapToClose = false;

    private int mSaveScrollX = 0;

    private Scroller mScroller;
    private VelocityTracker mTracker;
    private HorizontalScrollView mHorizontalSv;
    private View mMainChild = null;
    private OnLeftSliderLayoutStateListener mListener = null;
    private View.OnClickListener mClickListener = null;

    public LeftSliderLayout(Context context) {
        super(context);
        init();
    }

    public LeftSliderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LeftSliderLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        mTouchSlop = ViewConfiguration.get(getContext()).getTouchSlop();

        final float fDensity = getResources().getDisplayMetrics().density;
        mVelocaityUnits = (int)(VELOCITY_UNITS * fDensity + 0.5f);
        mMinorVelocity = (int)(MINOR_VELOCITY * fDensity + 0.5f);
        mSlidingWidth = (int)(SLIDING_WIDTH * fDensity + 0.5f);
        mShadowWidth = (int)(SHADOW_WIDTH * fDensity + 0.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if(widthMode != MeasureSpec.EXACTLY)
            throw new IllegalStateException("widthMode, only run at EXACTLY mode");
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if(heightMode != MeasureSpec.EXACTLY)
            throw new IllegalStateException("heightMode, only run at ExACTLY mode");

        int count = getChildCount();
        for(int i = 2; i < count; i++)
            removeViewAt(i);

        if(getChildCount() > 0) {
            if(getChildCount() > 1) {
                mMainChild = getChildAt(1);
                getChildAt(0).measure(widthMeasureSpec, heightMeasureSpec);
            } else {
                mMainChild = getChildAt(0);
            }
            mMainChild.measure(widthMeasureSpec, heightMeasureSpec);
        }

        scrollTo(mSaveScrollX, 0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(getChildCount() <= 0)
            return;

        if(mMainChild != null) {
            mMainChild.layout(left, top,
                    right + mMainChild.getMeasuredWidth(),
                    bottom + mMainChild.getMeasuredHeight());
        }

        if(getChildCount() > 1) {
            int leftChildWidth = 0;
            View leftChild = getChildAt(0);
            ViewGroup.LayoutParams param = leftChild.getLayoutParams();
            if(param.width == ViewGroup.LayoutParams.FILL_PARENT ||
                    param.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                leftChildWidth = mShadowWidth;
            } else {
                leftChildWidth = param.width;
            }
            leftChild.layout(-leftChildWidth, top, left, bottom);
        }
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mTapToClose) {
            if(event.getAction() == MotionEvent.ACTION_CANCEL ||
                    event.getAction() == MotionEvent.ACTION_UP)
                mTapToClose = false;
            
            return true;
        }

        int curScrollX = getScrollX();
        if(mMainChild != null && mTouchState != TOUCH_STATE_SCROLLING &&
                mIsTouchEventDone) {
            Rect rect = new Rect();
            mMainChild.getHitRect(rect);
            if(!rect.contains((int)event.getX() + curScrollX, (int)event.getY()))
                return false;
        }

        if(mTracker == null)
            mTracker = VelocityTracker.obtain();
        mTracker.addMovement(event);

        final int eventX = (int)event.getX();
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if(!mScroller.isFinished())
                    mScroller.abortAnimation();
                mIsTouchEventDone = false;
                mLastMotionX = eventX;
            }break;

            case MotionEvent.ACTION_MOVE: {
                if(!mIsEnable)
                    break;

                int deltaX = (int) (mLastMotionX - eventX);
                if(curScrollX + deltaX < getMinScrollX()) {
                    deltaX = getMinScrollX() - curScrollX;
                    mLastMotionX = mLastMotionX - deltaX; 
                } else if(curScrollX + deltaX > getMaxScrollX()) {
                    deltaX = getMaxScrollX() - curScrollX;
                    mLastMotionX = mLastMotionX - deltaX;
                } else {
                    mLastMotionX = eventX;
                }

                if(deltaX != 0)
                    scrollBy(deltaX, 0);

                mSaveScrollX = getScrollX();
            }break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if(!mIsEnable)
                    break;

                VelocityTracker mVelocityTracker = mTracker;
                mVelocityTracker.computeCurrentVelocity(mVelocaityUnits);

                if(curScrollX < 0) {
                    int velocityX = (int)mVelocityTracker.getXVelocity();
                    if(velocityX > mMinorVelocity) {
                        scrollByWithAnim(getMinScrollX() - curScrollX);
                        setState(true);
                    } else if(velocityX < -mMinorVelocity) {
                        scrollByWithAnim(-curScrollX);
                        setState(false);
                    } else {
                        if(curScrollX >= getMinScrollX() / 2) {
                            scrollByWithAnim(-curScrollX);
                            setState(false);
                        } else {
                            scrollByWithAnim(getMinScrollX() - curScrollX);
                            setState(true);
                        }
                    }
                } else {
                    if(curScrollX > 0) {
                        scrollByWithAnim(-curScrollX);
                    }
                    setState(false);
                }

                if(mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                mTouchState = TOUCH_STATE_IDLE;
                mIsTouchEventDone = true;
            }break;
        }

        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(isTouchEventContain(ev) && ev.getAction() == MotionEvent.ACTION_DOWN
                && mTouchState == TOUCH_STATE_IDLE && isOpen()) {
            close();
            mTapToClose = true;

            return true;
        }

        if(mListener != null && !mListener.onLeftSliderLayoutInterceptTouch(ev))
            return false;

        if(ev.getAction() == MotionEvent.ACTION_MOVE
                && mTouchState != TOUCH_STATE_IDLE)
            return true;

        switch(ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();
                mTouchState = mScroller.isFinished() ?
                        TOUCH_STATE_IDLE : TOUCH_STATE_SCROLLING;
            }break;

            case MotionEvent.ACTION_MOVE: {
                final int xDiff = (int)Math.abs(mLastMotionX - ev.getX());
                if (xDiff > mTouchSlop
                    && Math.abs(mLastMotionY - ev.getY())
                            / Math.abs(mLastMotionX - ev.getY()) < 1) {
                    mTouchState = TOUCH_STATE_SCROLLING;
                }
            }break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mTouchState = TOUCH_STATE_IDLE;
            }break;
        }

        return mTouchState != TOUCH_STATE_IDLE;
    }

    private int getMinScrollX() {
        return -mSlidingWidth;
    }

    private int getMaxScrollX() {
        return 0;
    }

    private void scrollByWithAnim(int delta) {
        if(delta == 0)
            return;

        mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta));
        invalidate();
    }

    private void setState(boolean state) {
        boolean stateChanged = false;
        if(mIsOpen && !state)
            stateChanged = true;
        else if(!mIsOpen && state) {
            stateChanged = true;
        }

        mIsOpen = state;

        if(mIsOpen)
            mSaveScrollX = getMaxScrollX();
        else
            mSaveScrollX = 0;

        if(stateChanged && mListener != null)
            mListener.onLeftSliderLayoutStateChanged(state);
    }

    public void open() {
        if(mIsEnable) {
            scrollByWithAnim(getMinScrollX() - getScrollX());
            setState(true);
        }
    }

    public void close() {
        if(mIsEnable) {
            scrollByWithAnim(-getScrollX());
            setState(false);
        }
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    public void enableSlide(boolean enable) {
        mIsEnable = enable;
    }

    public void setOnLeftSliderLayoutStateListener(
            OnLeftSliderLayoutStateListener l) {
        mListener = l;
    }

    public interface OnLeftSliderLayoutStateListener {
        public void onLeftSliderLayoutStateChanged(boolean isOpen);
        public boolean onLeftSliderLayoutInterceptTouch(MotionEvent ev);
    }

    /**
     * ensure if the MotionEvent happended in this view's content
     * but not the view, because even the view's content had
     * been scrolled to right, but the view is not scrolled,
     * we can see after call scrollTo() or scrollBy(), the
     * getScrollX() maybe changed but the view.getLeft() is
     * never changed
     * */
    private boolean isTouchEventContain(MotionEvent ev) {
        if(ev.getX() > Math.abs(getScrollX()) &&
                ev.getY() > Math.abs(getScrollY()))
                return true;
        return false;
    }
}
