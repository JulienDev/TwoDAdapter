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

package fr.julienvermet.twodadapter.sample.text;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fr.julienvermet.twodadapter.TwoDAdapter;
import fr.julienvermet.twodadapter.TwoDElement;
import fr.julienvermet.twodadapter.widget.TwoDScrollView;

public class MainActivity extends Activity {

	private static final String LOG_TAG = MainActivity.class.getSimpleName();

	private RelativeLayout mTwoDContent;
	private TwoDScrollView mTwoDScrollView;

	private ArrayList<TwoDElement<String>> mElements = new ArrayList<TwoDElement<String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTwoDContent = (RelativeLayout) findViewById(R.id.twoDContent);
		mTwoDScrollView = (TwoDScrollView) findViewById(R.id.twoDScrollView);

		float twoDContentWidth = getResources().getDimension(R.dimen.twod_content_width);
		float twoDContentHeight = getResources().getDimension(R.dimen.twod_content_height);

		for (int i=0; i<5000; i++) {
			double randomX = (int)(Math.random() * twoDContentWidth);
			double randomY = (int)(Math.random() * twoDContentHeight);
			
			TwoDElement<String> element = new TwoDElement<String>((int) randomX, (int) randomY, "data" + i);
			mElements.add(element);
		}
		new TwoDContentAdapter(mTwoDScrollView, mTwoDContent);
	}

	private class TwoDContentAdapter extends TwoDAdapter<TextView, String> {

		public TwoDContentAdapter(TwoDScrollView twoDScrollView, RelativeLayout twoDContent) {
			super(twoDScrollView, twoDContent);
		}

		@Override
		protected void bindView(TextView view, String data) {
			view.setText(data);
		}

		@Override
		protected TextView newView() {
			return new TextView(MainActivity.this);
		}

		@Override
		protected int getElementHeight(String data) {
			return 100;
		}

		@Override
		protected int getElementWidth(String data) {
			return 300;
		}

		@Override
		protected ArrayList<TwoDElement<String>> getElements(float scrollLeft,
				float scrollRight, float scrollTop, float scrollBottom) {
			return mElements;
		}
	}
}