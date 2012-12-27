/*
 * Copyright (C) 2012 Julien Vermet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.julienvermet.twodadapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import fr.julienvermet.twodadapter.widget.TwoDScrollView;
import fr.julienvermet.twodadapter.widget.TwoDScrollView.ScrollListener;

/**
 * @author Julien Vermet
 * 
 * @param <V> : View type
 * @param <Data> : Data type
 */
public abstract class TwoDAdapter<V extends View, Data> implements
ScrollListener, ViewTreeObserver.OnGlobalLayoutListener {

	private static final String LOG_TAG = TwoDAdapter.class.getSimpleName();
	private static final boolean DEBUG = false;

	protected TwoDScrollView mTwoDScrollView;
	protected float mTwoDScrollViewHeight;
	protected float mTwoDScrollViewWidth;
	protected RelativeLayout mTwoDContent;

	protected Stack<V> mViewsAddedWillDisappear = new Stack<V>();
	protected Stack<V> mViewsBucket = new Stack<V>();
	protected ConcurrentHashMap<Object, V> mViewsInUse = new ConcurrentHashMap<Object, V>();

	/**
	 * Constructor
	 * 
	 * @param twoDScrollView : Two dimensionnal ScrollView
	 * @param twoDContent : Scrollable content
	 */
	public TwoDAdapter(TwoDScrollView twoDScrollView, RelativeLayout twoDContent) {
		mTwoDScrollView = twoDScrollView;
		mTwoDContent = twoDContent;
		twoDScrollView.setOnScrollListener(this);
		final ViewTreeObserver treeObserver = mTwoDScrollView
				.getViewTreeObserver();
		treeObserver.addOnGlobalLayoutListener(this);
	}

	/**
	 * Allows to get ScrollView width and height on layout rendered
	 */
	@Override
	public void onGlobalLayout() {
		mTwoDScrollViewHeight = mTwoDScrollView.getHeight();
		mTwoDScrollViewWidth = mTwoDScrollView.getWidth();
		render();
		ViewTreeObserver treeObserver = mTwoDScrollView.getViewTreeObserver();
		treeObserver.removeGlobalOnLayoutListener(this);
	}

	/**
	 * Method called when the content must be redrawn
	 */
	public void render() {
		float scrollLeft = mTwoDScrollView.getScrollX();
		float scrollRight = scrollLeft + mTwoDScrollViewWidth;
		float scrollTop = mTwoDScrollView.getScrollY();
		float scrollBottom = scrollTop + mTwoDScrollViewHeight;

//		if (DEBUG) Log.d(LOG_TAG, "scrollTop:"+ scrollTop);

		// Check non-visible elements
		Iterator entries = mViewsInUse.entrySet().iterator();
		while (entries.hasNext()) {
			Entry entry = (Entry) entries.next();
			Data data = (Data) entry.getKey();
			V view = (V) entry.getValue();

			float viewLeft = getViewPositionX(view);
			float viewRight = viewLeft + view.getWidth();
			float viewTop = getViewPositionY(view);
			float viewBottom = viewTop + view.getHeight();
			
			if (!(isVisibleOnX(viewLeft, viewRight, scrollLeft, scrollRight) && isVisibleOnY(
					viewTop, viewBottom, scrollTop, scrollBottom))) {
				mViewsInUse.remove(data);
				mViewsAddedWillDisappear.push(view);
			}
		}

		// Add views for datas
		for (TwoDElement<Data> element : getElements(scrollLeft, scrollRight,
				scrollTop, scrollBottom)) {
			float dataLeft = element.x;
			float dataRight = element.x + getElementWidth((Data) element.value);
			float dataTop = element.y;
			float dataBottom = element.y + getElementHeight((Data) element.value);

			if (isVisibleOnX(dataLeft, dataRight, scrollLeft, scrollRight) && isVisibleOnY(dataTop, dataBottom, scrollTop, scrollBottom)) {
				if (!mViewsInUse.containsKey(element.value)) {
					V viewToBind = getView();
					mViewsInUse.put(element.value, viewToBind);
					setViewSize(viewToBind, (int) element.width, (int) element.height);
					setViewPosition(viewToBind, element.x, element.y);
					bindView(viewToBind, element.value);
				}
			}
		}
		if (DEBUG) Log.d(LOG_TAG, "childCount:" + mTwoDContent.getChildCount());

		// Remove non visible views and push them in bucket
		for (int i = 0; i < mViewsAddedWillDisappear.size(); i++) {
			V view = mViewsAddedWillDisappear.pop();
			mTwoDContent.removeView(view);
			mViewsBucket.push(view);
		}
		
		mTwoDContent.requestLayout();
	}

	/**
	 * Retrieve all the elements to draw. Only visible elements will be drawn
	 * but it's better to as small as possible ArrayList to avoid iterate a huge
	 * collection
	 * 
	 * @param scrollLeft : Current scroll left
	 * @param scrollRight : Current scroll right
	 * @param scrollTop : Current scroll top
	 * @param scrollBottom : Current scroll bottom
	 * @return
	 */
	protected abstract ArrayList<TwoDElement<Data>> getElements(
			float scrollLeft, float scrollRight, float scrollTop,
			float scrollBottom);

	/**
	 * Bind view as you want
	 * @param view : View to bind
	 * @param data : Data used to bind view
	 */
	protected abstract void bindView(V view, Data data);

	/**
	 * Called when there is not enough views to recycle
	 * @return The new view created
	 */
	protected abstract V newView();

	/**
	 * Estimate element height to know if an element must be drawn.
	 * @param data
	 * @return Height
	 */
	protected abstract int getElementHeight(Data data);

	protected abstract int getElementWidth(Data data);

	/**
	 * Create or recycle a view
	 * @return View created or recycled
	 */
	protected V getView() {
		if (!mViewsAddedWillDisappear.isEmpty()) {
			if (DEBUG) Log.d(LOG_TAG, "recycle mViewsAddedWillDisappear");
			return mViewsAddedWillDisappear.pop();
		} else if (!mViewsBucket.isEmpty()) {
			if (DEBUG) Log.d(LOG_TAG, "recycle mViewsBucket");
			V view = mViewsBucket.pop();
			mTwoDContent.addView(view);
			return view;
		} else {
			if (DEBUG) Log.d(LOG_TAG, "newView");
			V view = newView();
			mTwoDContent.addView(view);
			return view;
		}
	}

	/**
	 * Check if a view/element is visible on X axis
	 * @param x1 : Left position
	 * @param x2 : Right position
	 * @param scrollLeft : Current scroll left position
	 * @param scrollRight : Current scroll right position
	 * @return true if visible, otherwise false
	 */
	protected boolean isVisibleOnX(float x1, float x2, float scrollLeft,
			float scrollRight) {
		if ((x1 >= scrollLeft || x2 >= scrollLeft)
				&& (x1 <= scrollRight || x2 <= scrollRight)) {
			return true;
		}
		return false;
	}

	/**
	 * Check if a view/element is visible on Y axis
	 * @param y1 : Top position
	 * @param y2 : Bottom position
	 * @param scrollTop : Current scroll top position
	 * @param scrollBottom : Current scroll bottom position
	 * @return true if visible, otherwise false
	 */
	protected boolean isVisibleOnY(float y1, float y2, float scrollTop,
			float scrollBottom) {
		if ((y1 >= scrollTop || y2 >= scrollTop)
				&& (y1 <= scrollBottom || y2 <= scrollBottom)) {
			return true;
		}
		return false;
	}

	/**
	 * When scroll change we must redraw the elements
	 */
	@Override
	public void onScrollChanged(int x, int y, int oldx, int oldy) {
		if (x!=oldx || y!=oldy) {
			render();
		}
	}

	/**
	 * Set view position
	 * @param view : View to set
	 * @param x : Position on X axis
	 * @param y : Position on Y axis
	 */
	@SuppressLint("NewApi")
	protected void setViewPosition(View view, float x, float y) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			view.setX(x);
			view.setY(y);
		} else {
			LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
			layoutParams.leftMargin = (int) x;
			layoutParams.topMargin = (int) y;
		}
	}

	/**
	 * Get view position on X axis
	 * @param view : View to get position
	 * @return Position on X axis
	 */
	@SuppressLint("NewApi")
	protected float getViewPositionX(View view) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return view.getX();
		} else {
			LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
			return layoutParams.leftMargin;
		}
	}

	/**
	 * Get view position on Y axis
	 * @param view : View to get position
	 * @return Position on Y axis
	 */
	@SuppressLint("NewApi")
	protected float getViewPositionY(View view) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return view.getY();
		} else {
			LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
			return layoutParams.topMargin;
		}
	}

	/**
	 * Set view size
	 * @param view : View to set
	 * @param width : View width
	 * @param height : View height
	 */
	protected void setViewSize(View view, int width, int height) {
		LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
		if (width != 0) {
			layoutParams.width = width;
		}
		if (height != 0) {
			layoutParams.height = height;
		}
	}
}