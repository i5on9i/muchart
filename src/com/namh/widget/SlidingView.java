package com.namh.widget;




import java.util.LinkedList;

import com.muchart.R;
import com.namh.widget.flowindicator.TitleFlowIndicator;

import android.content.Context; 

import android.graphics.Bitmap; 
 
import android.graphics.Canvas; 
 
import android.graphics.PointF; 
import android.util.AttributeSet; 
import android.util.Log; 
import android.view.MotionEvent; 
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import android.widget.FrameLayout;


import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Scroller; 

import android.widget.Toast; 


/**
 * This class has several child views and showed one by one with horizontal
 * scrolling.
 */ 
public class SlidingView extends AdapterView<Adapter> {
	private static final String TAG = "SlidingView"; 

	private LinkedList<View> mLoadedViews;
	private LinkedList<View> mRecycledViews;
	
	

	private Adapter mAdapter;

	private int mCurrentBufferIndex;
	private int mCurrentAdapterIndex;

	private int mCurrentScreen;
	private boolean mFirstLayout = true;


	

	private VelocityTracker mVelocityTracker = null; 
	 
	// When the fliping velocity is over the below, the View starts to be scrolled.
	private static final int SNAP_VELOCITY = 100;	// (pixel/s)
	 
	 
	private int mTouchSlop;
	private int mMaximumVelocity; // maximum limit speed of scrolling

	private Bitmap mWallpaper = null;
	 
	private Scroller mScroller = null; 
	private PointF mTouchStartPoint = null;
	private float mLastMotionX;
	private int mCurPage = 0; // current page number, '0' is the first page.

	private int mCurTouchState;
	private static final int TOUCH_STATE_DOWNSTART = 0;	
	private static final int TOUCH_STATE_HSCROLLING = 1; 
	private static final int TOUCH_STATE_NORMAL = 2;
	
	public static final int TITLE_POSITION = 0;	// title index of the ListView

	private Toast mToast;
	
	private TitleFlowIndicator mIndicator;

	

	public SlidingView(Context context) { 
		super(context); 
		init(); 
	} 

	public SlidingView(Context context, AttributeSet attrs) { 
		super(context, attrs); 
		init(); 
	} 

	public SlidingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle); 
		init(); 
	} 

	private void init() { 

		// namh
		mLoadedViews = new LinkedList<View>();
		mRecycledViews = new LinkedList<View>();

		mScroller = new Scroller(getContext());
		
		mTouchStartPoint = new PointF();

		// configuration
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		// configuration.getScaledPagingTouchSlop() is too small for me.
		mTouchSlop = 32;
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		

	} 

	
	public int getViewsCount() {
		return mAdapter.getCount();
	}
	

	/**
	 * set the specific value of the width and height of the View, {@link #getChildAt(idx)}
	 * {@link #measure(int, int)} function sets the values of the it.
	 */
	@Override 
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		Log.d(TAG, "onMeasure");

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY && !isInEditMode()) {
			throw new IllegalStateException(
					"ViewFlow can only be used in EXACTLY mode.");
		}

		
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY && !isInEditMode()) {
			throw new IllegalStateException(
					"ViewFlow can only be used in EXACTLY mode.");
		}

		// The children are given the same width and height as the workspace
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		if (mFirstLayout) {
			mScroller.startScroll(0, 0, mCurrentScreen * width, 0, 0);
			mFirstLayout = false;
		}

		
	} 

	/**
	 * {@link #onLayout(boolean, int, int, int, int)} determines the positions of the Views,
	 * which attributes, such as height or width, are set in {@link #onMeasure(int, int)}.
	 * 
	 * the position of the View is the value of the coordinations in the running application.
	 *
	 * Also, the position is the value relative to the parent.
	 */
	@Override 
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.d(TAG, "onLayout");
		
		/**
		 * At here, to scroll views, the views are arranged sequentially.
		 * 
		 * <pre>
		 * +------+------+
		 * | view | view |
		 * +------+------+
		 * </pre>
		 * 
		 * TODO
		 * In my opinion, the coordinations as parameters are the relative
		 * coordinations to the parent of the SlidingView, 
		 * not the coordinations of the children of SlidingView.
		 * 
		 * Thus, you should re-calculate the coordinations for the children of the
		 * SlidngView.
		 */

		super.onLayout(changed, l, t, r, b);
		if(mAdapter == null){
			return;
		}


		positionItems();
        invalidate();

	} 

	// onDraw() for ViewGroup 
	@Override 
	protected void dispatchDraw(Canvas canvas) {
//		canvas.drawBitmap(mWallpaper, 0, 0, mPaint);
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			drawChild(canvas, getChildAt(i), 100);
		} 
	} 

	/**
	 * Without this overridden function, ListView may treat MotionEvents,
	 * so the onTouchEvent() of this ViewGroup could not get the event
	 * after ListView's treatment.
	 * Maybe, ListView's event handlers returns true.
	 * 
	 * Thus, this function must be overridden to get the event for the
	 * horizontal scrolling.
	 * 
	 * return "true" means the event is intercepted or handled,
	 * so that the event is not delivered into the sub-hierarchy
	 * Still, the next routine, i.e. {@link #onTouchEvent(MotionEvent)},
	 * is executed.
	 * 
	 * Scrolling of the list is not responsibility of this ViewGoup.
	 * Instead, the ListView(View)({@link #getChildAt(int)}) is in charge,
	 * so the Event should be delivered to the child.
	 * 
	 * 
	 * 
	 * Thus, the purpose of this function here is to identify the 
	 * scrolling type, that is, vertical or horizontal.
	 */
	@Override 
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		Log.d(TAG, "onInterceptTouchEvent : " + ev.getAction()); 
		int action = ev.getAction(); 
		int x = (int) ev.getX();
		int y = (int) ev.getY();

		switch (action) { 
		case MotionEvent.ACTION_DOWN: 

			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			
			/**
			 * Without this statement, In the middle of horizontal scrolling,
			 * if you detach your finger from the screen and touch again to fling the view,
			 * the View would start from the origin because the {@link #MotionEvent.ACTION_DOWN}
			 * is passed to ListView for the vertical scroll.
			 * 
			 * With this, however, you can continue your fling horizontally with
			 * re-touching the view after detaching the screen.
			 */
			mCurTouchState = mScroller.isFinished() ? TOUCH_STATE_NORMAL 
					: TOUCH_STATE_HSCROLLING;
			
			// mTouchStartPoint will be used in onTouchEvent(), when the return true.
			mTouchStartPoint.set(x, y);

  
			break;
		case MotionEvent.ACTION_MOVE: 
			/**
			 * Here, we check how many distances the x is moved by the drag.
			 * 
			 * If the distance of x is over the {@link #mTouchSlop},
			 * the action could be considered as the Horizontal scroll,
			 * 
			 * so that we define it as the horizontal scrolling with the 
			 * state value {@link #TOUCH_STATE_HSCROLLING}.
			 * 
			 * In addition, with the {@link #TOUCH_STATE_HSCROLLING} this
			 * function will return "true" in order to run onTouchEvent().
			 *  
			 */
			 
			// "true" means go to onTouchEvent() of this instance
			return startScrollIfNeeded(ev);	
			
		} 
 
		return mCurTouchState == TOUCH_STATE_HSCROLLING; 
	} 
	
	
	@Override 
	public boolean onTouchEvent(MotionEvent event) {
		Log.d(TAG, "event Action : " + event.getAction()); 

		if (mVelocityTracker == null)
			mVelocityTracker = VelocityTracker.obtain(); 
		 
		
		mVelocityTracker.addMovement(event);  

		switch (event.getAction()) { 

		case MotionEvent.ACTION_DOWN:
			mTouchStartPoint.set(event.getX(), event.getY()); 
			break; 

		case MotionEvent.ACTION_MOVE:
			/**
			 * This routine implements the view showed during drag.
			 */

			if(mCurTouchState == TOUCH_STATE_DOWNSTART){
				// I think this code is redundancy but can't sure.
				startScrollIfNeeded(event);
			}

			if(mCurTouchState == TOUCH_STATE_HSCROLLING){
				scrollHorizontally(event);
			}
			break; 

		case MotionEvent.ACTION_UP:
			if (mCurTouchState == TOUCH_STATE_HSCROLLING){
				// the less unit parameter is used, the smaller value will you get.(1 = 1ms)
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityX = (int) velocityTracker.getXVelocity();
		
				
				Log.d(TAG, "mVelocityTracker : " + velocityX); 
				
				/**
				 * getWidth()
				 * These dimensions define the actual size of the view on screen,
				 * at drawing time and after layout.
				 */
				final int screenWidth = getWidth();

				if (velocityX > SNAP_VELOCITY && mCurPage > 0) {
					// Fling hard enough to move left
					snapToScreen(mCurPage - 1);
				} else if (velocityX < -SNAP_VELOCITY
						&& mCurPage < getChildCount() - 1) {
					// Fling hard enough to move right
					snapToScreen(mCurPage + 1);
				} else {
					
					snapToDestination();
				}
				
				// TODO
				// how to get rid of overhead?
				toastChartName();
				
				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}
			mCurTouchState = TOUCH_STATE_NORMAL;
			break;
		case MotionEvent.ACTION_CANCEL:
			snapToDestination();
			mCurTouchState = TOUCH_STATE_NORMAL;

		} 

		return true; 
	} 

	/**
	 * computeScroll is invoked by the draw() method of View.
	 * 
	 * Draw() may be invoked by the invalidate().
	 */
	@Override 
	public void computeScroll() {
		Log.d(TAG, "computeScroll");


		if (mScroller.computeScrollOffset()) {  
			/**
			 * 
			 * 
			 * Unless, {@link #scrollTo} is not called here,
			 * The View works like the one big page, so that
			 * the fling works just like the non-elastic drag.
			 *  
			 */

			/**
			 * invalidate() is not needed after this function.
			 * because the view will be invalidated in the {@link #scrollTo}
			 * 
			 * However, in the case of {@link #getCurrX()} has the equivalent value to
			 * the {@link #getScroll()}, the invalidate() is needed for this case because
			 * {@link #scrllTo()} does not do anything.
			 * 
			 */
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}else{
			if (mIndicator != null) {
				mIndicator.onSwitched(getChildAt(mCurPage),mCurPage);
			}
		}
		
	}
	
	

	/**
	 * Show the scroll animation
	 * 
	 * To stop this animation, use {@link #mScroller.abortAnimation()} 
	 */
	private void snapToScreen(int destPage) {
//		mLastScrollDirection = destPage - mCurPage;
		if (!mScroller.isFinished())
			return;

		destPage = Math.max(0, Math.min(destPage, getChildCount() - 1));

		mCurPage = destPage; 

		final int newX = destPage * getWidth();
		final int delta = newX - getScrollX();
		mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta)*1/2);
		invalidate();
	}


	/**
	 * Show the scroll animation
	 * 
	 * To stop this animation, use {@link #mScroller.abortAnimation()} 
	 */
	private void snapToDestination(){
		/**
		 * In the case of the slow horizontal scrolling
		 * 
		 * after the scrolling, depending on the dragged range, 
		 * {@link #startScroll()} must run.
		 * 
		 *  in the case of half of the hidden view A is showed on the screen,
		 *  the view A must get the screen.
		 *  On the contrary to this, less than half of the hidden view A is
		 *  showed on the screen, the view A should not get the screen.
		 *  
		 */
		final int screenWidth = getWidth();
		final int whichView = (getScrollX() + (screenWidth / 2))
							/ screenWidth;

		snapToScreen(whichView);
	}

	
	
	/**
	 * 
	 * @return the value shows the which view is showed on the screen
	 */
	public int getCurrentItemPosition(){
		return this.mCurPage;
	}

	
	private boolean startScrollIfNeeded(MotionEvent event){

		int x = (int) event.getX();
		int y = (int) event.getY();
		
		int deltaX = Math.abs(x - (int)mTouchStartPoint.x);
		if (deltaX > mTouchSlop) {
			// we've moved far enough for this to be a scroll
			mCurTouchState = TOUCH_STATE_HSCROLLING; 
			mTouchStartPoint.set(x, y);
            return true;

		}

		

        return false;
	}
	private void scrollHorizontally(MotionEvent event){
		final float x = event.getX();
		int deltaX = (int) (x - mTouchStartPoint.x);

		// Scroll to follow the motion event

		mLastMotionX = x;

//		final int scrollX = getScrollX();
//		if (deltaX < 0) {
//			if (scrollX > 0) {
//				scrollBy(Math.max(-scrollX, deltaX), 0);
//			}
//		} else if (deltaX > 0) {
//			final int availableToScroll = getChildAt(
//					getChildCount() - 1).getRight()
//					- scrollX - getWidth();
//			if (availableToScroll > 0) {
//				scrollBy(Math.min(availableToScroll, deltaX), 0);
//			}
//		}
//		
		scrollBy(-deltaX, 0);
		return;

			
	}

	private void positionItems(){
		int childCount = getChildCount();
		int childLeft = 0;
		for (int i = 0; i < childCount; i++) {	
			// top position should be '0'(zero) because the position parameters
			// should have the coordinations relative to the parent, here, SlidingView.
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth,
						child.getMeasuredHeight());
				childLeft += childWidth;
			}
			
		}
	}

	/**
	 *
	 *	Show the name of the music chart using Toast
	 */
	private void toastChartName(){
		FrameLayout layout = (FrameLayout)getChildAt(mCurPage);

		CharSequence chartName = (CharSequence)layout
								.findViewById(R.id.listview)
								.getTag();

		if (mToast != null) {
			mToast.setText(chartName); 
		} else { 
			mToast = Toast.makeText(getContext(), chartName, 
					Toast.LENGTH_SHORT); 
		} 
		mToast.show();
		
	}

	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}

	
	

	

	@Override
	public View getSelectedView() {
		return (mCurrentBufferIndex < mLoadedViews.size() ? mLoadedViews
				.get(mCurrentBufferIndex) : null);
	}

	protected void recycleViews() {
		while (!mLoadedViews.isEmpty())
			recycleView(mLoadedViews.remove());
	}

	protected void recycleView(View v) {
		if (v == null)
			return;
		mRecycledViews.add(v);
		detachViewFromParent(v);
	}

	protected View getRecycledView() {
		return (mRecycledViews.isEmpty() ? null : mRecycledViews.remove(0));
	}

	@Override
	public void setAdapter(Adapter adapter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSelection(int position) {
		// TODO Auto-generated method stub
		
	}

	
}

