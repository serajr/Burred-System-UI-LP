package serajr.blurred.system.ui.lp.utils;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;

public class Utils {
		
	public static ArrayList<View> getAllViews(View view) {

	    	if (!(view instanceof ViewGroup)) {
	    	
	        	ArrayList<View> viewArrayList = new ArrayList<View>();
	        	viewArrayList.add(view);
	        
	        	return viewArrayList;
	        
	    	}

	    	ArrayList<View> result = new ArrayList<View>();

	    	ViewGroup viewGroup = (ViewGroup) view;
	    	for (int i = 0; i < viewGroup.getChildCount(); i++) {

	        	View child = viewGroup.getChildAt(i);

	        	ArrayList<View> viewArrayList = new ArrayList<View>();
	        	viewArrayList.add(view);
	        	viewArrayList.addAll(getAllViews(child));

	        	result.addAll(viewArrayList);
	        
	    	}
	    
	    	return result;
	    
	}
}
