# ExpandLayoutManager for RecyclerView
======================

## Examples

![Example](resources/animation_layout_manager.gif "working example")

## Integration with Gradle (comming soon)

```
    compile 'com.azoft.expandlayoutmanager:expandlayoutmanager:1.0.0'
```

## Description

To use this LayoutManager add this code for your RecyclerView:

    final ExpandLayoutManager layoutManager = new ExpandLayoutManager();
	final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
	recyclerView.setLayoutManager(layoutManager);
	
You can simply change animation duration with constuctor paramener:

    final ExpandLayoutManager layoutManager = new ExpandLayoutManager(1000); // setup animation duration as 1 second (1000 msec)


To make rotation animation like in sample do this:

    * Use adapter item root view as com.azoft.expandlayoutmanager.view.SimpleAnimationView
    * Add container with @id/view_to_animate. This container will be animated during collapse and expand actions.

You can add any other collapse/expand view animation. For this you should create custom view and implementing com.azoft.expandlayoutmanager.AnimationView.

To open/close item call method:
    	
	layoutManager.actionItem(adapterPosition);
    // or by calling with view from Adapter
    layoutManager.actionItem(animationView);

Full code from this sample:

	private ExpandLayoutManager mLayoutManager;
	
	...
    // vertical layout
	mLayoutManager = new ExpandLayoutManager();
	final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
	recyclerView.setLayoutManager(mLayoutManager);
	
    final DataAdapter dataAdapter = new DataAdapter(citiesResponse.getCities());
    recyclerView.setAdapter(dataAdapter);
    dataAdapter.setItemClickListener(new DataAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(final int pos) {
                mLayoutManager.actionItem(pos);
            }
        });
	}


#### Contact ####

Feel free to get in touch.

    Website:    http://www.azoft.com
    Twitter:    @azoft
    LinkedIn:   https://www.linkedin.com/company/azoft
    Facebook:   https://www.facebook.com/azoft.company
    Email:      android-mobile@azoft.com

#### License ####

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
