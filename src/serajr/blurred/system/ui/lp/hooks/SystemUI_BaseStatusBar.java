package serajr.blurred.system.ui.lp.hooks;

import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.NotificationBackgroundView;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationData.Entry;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class SystemUI_BaseStatusBar {
	
	private static NotificationData mNotificationData;
	
	public static void hook() {
		
		try {

			// start
			XposedHelpers.findAndHookMethod(BaseStatusBar.class, "start", new XC_MethodHook() {
				
				@Override
	            		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					
					// guarda
					mNotificationData = (NotificationData) XposedHelpers.getObjectField(param.thisObject, "mNotificationData"); 
					
				}
			});
			
			// toggleRecentApps
			XposedHelpers.findAndHookMethod(BaseStatusBar.class, "toggleRecentApps", new XC_MethodHook() {
				
				@Override
	            		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					
					// blur
					SystemUI_RecentsActivity.startBlurTask();
					
				}
			});
					
		} catch (Exception e) {
			
			XposedBridge.log(e);
			
		}
	}
	
	public static void updatePreferences() {
		
		// continua ?
		if (mNotificationData == null)
			return;
		
		// passam por todas a notificações
		for (Entry entry : mNotificationData.getActiveNotifications()) {
			
			// -------------------------------------------------------------------------
			// ExpandableNotificationRow (entry.row) extends ActivatableNotificationView
			// -------------------------------------------------------------------------
			
			// obtém os campos
			NotificationBackgroundView mBackgroundNormal = (NotificationBackgroundView) XposedHelpers.getObjectField(entry.row, "mBackgroundNormal");
			NotificationBackgroundView mBackgroundDimmed = (NotificationBackgroundView) XposedHelpers.getObjectField(entry.row, "mBackgroundDimmed");
			
			// invalida (força o draw)
			mBackgroundNormal.postInvalidate();
			mBackgroundDimmed.postInvalidate();
			
		}
	}
}
