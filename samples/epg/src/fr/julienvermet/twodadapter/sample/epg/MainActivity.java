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

package fr.julienvermet.twodadapter.sample.epg;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fr.julienvermet.twodadapter.TwoDAdapter;
import fr.julienvermet.twodadapter.TwoDElement;
import fr.julienvermet.twodadapter.widget.TwoDScrollView;

public class MainActivity extends Activity {

	private static final String LOG_TAG = MainActivity.class.getSimpleName();
	private static final long ONE_HOUR = 60*60;

	float mEpgOneHour;
	float mEpgElementHeight;
	long mXOrigin;

	private RelativeLayout mTwoDContent;
	private TwoDScrollView mTwoDScrollView;

	
	private SparseArray<ArrayList<TvProgram>> mTvChannels = new SparseArray<ArrayList<TvProgram>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTwoDContent = (RelativeLayout) findViewById(R.id.twoDContent);
		mTwoDScrollView = (TwoDScrollView) findViewById(R.id.twoDScrollView);

		mEpgOneHour = getResources().getDimension(R.dimen.epg_one_hour);
		mEpgElementHeight = getResources().getDimension(R.dimen.epg_element_height);

		mXOrigin = 1356631200;
		
		for (int i=0; i<50; i++) {
			ArrayList<TvProgram> channel = new ArrayList<TvProgram>();
			TvProgram tvProgram1 = new TvProgram(1356631200, 1356634800, "Program"+ (i+1));
			TvProgram tvProgram2 = new TvProgram(1356635000, 1356645400, "Program2"+ (i+2));
			TvProgram tvProgram3 = new TvProgram(1356645800, 1356655800, "Program3"+ (i+3));
			TvProgram tvProgram4 = new TvProgram(1356657800, 1356667800, "Program4"+ (i+4));
			channel.add(tvProgram1);
			channel.add(tvProgram2);
			channel.add(tvProgram3);
			channel.add(tvProgram4);
			mTvChannels.put(i, channel);
		}

		new TwoDContentAdapter(mTwoDScrollView, mTwoDContent);
	}

	private ArrayList<Integer> getVisibleChannels(float y1, float y2) {
		ArrayList<Integer> visibleChannels = new ArrayList<Integer>();
		int channelY = 0;
		
		for (int i=0; i<mTvChannels.size(); i++) {
			if ((channelY >= y1 && channelY <= y2) || (channelY+mEpgElementHeight >= y1 && channelY+mEpgElementHeight <= y2)) {
				visibleChannels.add(i);
			}
			channelY += mEpgElementHeight;
		}
		return visibleChannels;
	}

	private float getProgramWidth(long startTime, long endTime) {
		double length = endTime - startTime;
		return (float) (length/ONE_HOUR)*mEpgOneHour;
	}

	private float getProgramX(long startTime) {
		return getProgramWidth(mXOrigin, startTime);
	}

	private class TwoDContentAdapter extends TwoDAdapter<TextView, TvProgram> {

		LayoutInflater mInflater;

		public TwoDContentAdapter(TwoDScrollView twoDScrollView, RelativeLayout twoDContent) {
			super(twoDScrollView, twoDContent);
			mInflater = getLayoutInflater();
		}

		@Override
		protected void bindView(TextView view, TvProgram data) {
			view.setText(data.name);
		}

		@Override
		protected TextView newView() {
			return (TextView) mInflater.inflate(R.layout.epg_element, null);
		}

		@Override
		protected ArrayList<TwoDElement<TvProgram>> getElements(float scrollLeft,
				float scrollRight, float scrollTop, float scrollBottom) {

			ArrayList<TwoDElement<TvProgram>> mElements = new ArrayList<TwoDElement<TvProgram>>();

			ArrayList<Integer> visibleChannels = getVisibleChannels(scrollTop, scrollBottom);
			for (int channel : visibleChannels) {
				float channelY = (int) (channel * mEpgElementHeight);
				ArrayList<TvProgram> tvPrograms = mTvChannels.get(channel);
				for (TvProgram tvProgram : tvPrograms) {
					float programX = getProgramX(tvProgram.startTime);
					float programWidth = getProgramWidth(tvProgram.startTime, tvProgram.endTime);
					TwoDElement<TvProgram> element = new TwoDElement<TvProgram>(programX, channelY, programWidth, mEpgElementHeight, tvProgram);
					mElements.add(element);
				}
			}
			return mElements;
		}
	}
}