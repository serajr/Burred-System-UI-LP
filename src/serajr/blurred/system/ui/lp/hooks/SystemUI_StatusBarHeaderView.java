package serajr.blurred.system.ui.lp.hooks;

import serajr.blurred.system.ui.lp.Xposed;
import serajr.blurred.system.ui.lp.fragments.BlurSettingsFragment;

import com.android.systemui.statusbar.phone.StatusBarHeaderView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class SystemUI_StatusBarHeaderView {
	
	private static boolean mTranslucentHeader;
	private static int mTranslucencyPercentage;
	private static StatusBarHeaderView mStatusBarHeaderView;
	
	public static void hook() {
		
		try {
			
			// onFinishInflate
			XposedHelpers.findAndHookMethod(StatusBarHeaderView.class, "onFinishInflate", new XC_MethodHook() {
				
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					
					// guarda
					mStatusBarHeaderView = (StatusBarHeaderView) param.thisObject;
					
					// transparente ?
					handleStatusBarHeaderViewBackround();
					
		        }
			});
			
		} catch (Exception e) {
			
			XposedBridge.log(e);
			
		}
	}
	
	public static void updatePreferences() {
		
		// atualiza
		mTranslucentHeader = Xposed.mXSharedPreferences.getBoolean(BlurSettingsFragment.TRANSLUCENT_HEADER_PREFERENCE_KEY, BlurSettingsFragment.TRANSLUCENT_HEADER_PREFERENCE_DEFAULT);
		mTranslucencyPercentage = Integer.parseInt(Xposed.mXSharedPreferences.getString(BlurSettingsFragment.TRANSLUCENT_PRECENTAGE_PREFERENCE_KEY, BlurSettingsFragment.TRANSLUCENT_PRECENTAGE_PREFERENCE_DEFAULT));
		mTranslucencyPercentage = 255 - ((mTranslucencyPercentage * 255) / 100);
		
		// transparente ?
		handleStatusBarHeaderViewBackround();
		
	}
	
	public static void handleStatusBarHeaderViewBackround() {
		
		// continua ?
		if (SystemUI_NotificationPanelView.mNotificationPanelView == null)
			return;
		
		// obtém os campos
		boolean mKeyguardShowing = XposedHelpers.getBooleanField(SystemUI_NotificationPanelView.mNotificationPanelView, "mKeyguardShowing");
		
		// continua ?
		if (mStatusBarHeaderView == null)
			return;
		
		if (mKeyguardShowing) {
			
			// opaco !
			mStatusBarHeaderView.getBackground().setAlpha(255);
			
		} else {
			
			// transparente ?
			mStatusBarHeaderView.getBackground().setAlpha(mTranslucentHeader ? mTranslucencyPercentage : 255);
			
		}
	}
}