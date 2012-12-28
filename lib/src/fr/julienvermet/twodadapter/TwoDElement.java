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

/**
 * Element to add on TwoDScrool
 * 
 * @author Julien Vermet
 * 
 * @param <Data>
 *            Data type
 */
public class TwoDElement<Data> {
	public float x;
	public float y;
	public float width;
	public float height;
	public Data value;

	/**
	 * TwoDElement Constructor
	 * 
	 * You must always specify width and height, otherwise, the adapter won't be
	 * able to determine if a view must be shown
	 * 
	 * @param x : Position on X axis
	 * @param y : Position on Y axis
	 * @param width : Element width
	 * @param height : Element height
	 * @param value : Data used to bind view
	 */
	public TwoDElement(float x, float y, float width, float height, Data value) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.value = value;
	}
}