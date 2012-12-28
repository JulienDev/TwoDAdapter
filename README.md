TwoDAdapter
===========

Two Dimensional Adapter is an Android component. If you are familiar with Android UI you know AdapterViews like ListView, GridView. They all use view recycling and make your UI smooth while scrolling. The aim of TwoDAdapter is to bring you view recycling on both X and Y axis.

[http://www.youtube.com/watch?v=3YLFGc4vgaU](http://www.youtube.com/watch?v=3YLFGc4vgaU)

For the first release, this component is not exactly an adapter because it doesn't extend AdapterView and scroll is performed by a TwoDScrollView thanks to GORGES : http://blog.gorges.us/2010/06/android-two-dimensional-scrollview/ .

How it works
===========

render()
--------

Each time you scroll the adapter will enter in render() method: 

1. Check if current added views are still visibles. If a view has disappeared since previous render, view will be added to a viewWillDisappear bucket. If it's still visible, do nothing.
2. Iterate all TwoDElements given by the program, and check if an element must be added on screen. If element must be shown, call getView() then bindView().
3. Remove unused views in viewWillDisappear and push it in a global view bucket.

getView()
---------

1. Returns a view from viewWillDisappear (if not empty)
2. Else, returns a view from global view bucket (if not empty)
3. Else, create a newView()


Developed By
===========

* Julien Vermet - ju.vermet@gmail.com

License
===========

>Copyright (C) 2012 Julien Vermet
>
>Licensed under the Apache License, Version 2.0 (the "License");
>you may not use this file except in compliance with the License.
>You may obtain a copy of the License at
>
>	 http://www.apache.org/licenses/LICENSE-2.0
>
>Unless required by applicable law or agreed to in writing, software
>distributed under the License is distributed on an "AS IS" BASIS,
>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
>See the License for the specific language governing permissions and
>limitations under the License."