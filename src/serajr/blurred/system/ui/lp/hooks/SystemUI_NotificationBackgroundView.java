package serajr.blurred.system.ui.lp.hooks;

import serajr.blurred.system.ui.lp.Xposed;
import serajr.blurred.system.ui.lp.fragments.BlurSettingsFragment;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.android.systemui.statusbar.NotificationBackgroundView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class SystemUI_NotificationBackgroundView {
	
	private static int mTranslucencyPercentage;
	private static boolean mTranslucentNotifications;
	
	public static void hook() {
		
		try {
			
			// draw
			XposedHelpers.findAndHookMethod(NotificationBackgroundView.class, "draw", Canvas.class, Drawable.class, new XC_MethodHook() {
				
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					
					// continua ?
					if (SystemUI_NotificationPanelView.mNotificationPanelView == null)
						return;
					
					// obtém os campos
					boolean mKeyguardShowing = XposedHelpers.getBooleanField(SystemUI_NotificationPanelView.mNotificationPanelView, "mKeyguardShowing");
					
					// lockscreen ?
					if (mKeyguardShowing)
						return;
					
					// heads up ?
					if (SystemUI_NotificationPanelView.mNotificationPanelView.isFullyCollapsed()) {
						
						if (((Drawable) param.args[1]).getAlpha() != 255)
							((Drawable) param.args[1]).setAlpha(255);
						
						return;
						
					}
					
					// transparente ?
					if (mTranslucentNotifications) {
						
						if (((Drawable) param.args[1]).getAlpha() != mTranslucencyPercentage)
							((Drawable) param.args[1]).setAlpha(mTranslucencyPercentage);
						
					} else {
						
						if (((Drawable) param.args[1]).getAlpha() != 255)
							((Drawable) param.args[1]).setAlpha(255);
						
					}
		        }
			});
			
		} catch (Exception e) {
			
			XposedBridge.log(e);
			
		}
	}
	
	public static void updatePreferences() {
		
		// atualiza
		mTranslucentNotifications = Xposed.mXSharedPreferences.getBoolean(BlurSettingsFragment.TRANSLUCENT_NOTIFICATIONS_PREFERENCE_KEY, BlurSettingsFragment.TRANSLUCENT_NOTIFICATIONS_PREFERENCE_DEFAULT);
		mTranslucencyPercentage = Integer.parseInt(Xposed.mXSharedPreferences.getString(BlurSettingsFragment.TRANSLUCENT_PRECENTAGE_PREFERENCE_KEY, BlurSettingsFragment.TRANSLUCENT_PRECENTAGE_PREFERENCE_DEFAULT));
		mTranslucencyPercentage = 255 - ((mTranslucencyPercentage * 255) / 100);
		
	}
}