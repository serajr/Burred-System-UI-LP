package serajr.blurred.system.ui.lp.hooks;

import serajr.blurred.system.ui.lp.Xposed;
import serajr.blurred.system.ui.lp.activities.BlurSettingsActivity;
import serajr.blurred.system.ui.lp.fragments.BlurSettingsFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Process;

import com.android.systemui.statusbar.phone.PhoneStatusBar;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class SystemUI_PhoneStatusBar {
	
	private static Handler mHandler = new Handler();
	
	public static void hook() {
		
		try {
			
			// makeStatusBarView
			XposedHelpers.findAndHookMethod(PhoneStatusBar.class, "makeStatusBarView", new XC_MethodHook() {
				
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					
					final PhoneStatusBar thiz = (PhoneStatusBar) param.thisObject;
	
					// receiver
					BroadcastReceiver receiver = new BroadcastReceiver() {
            			
                        @Override
                        public void onReceive(Context context, Intent intent) {
                        	
                        	// obtém os campos
        					boolean mExpandedVisible = XposedHelpers.getBooleanField(thiz, "mExpandedVisible");
        					
                        	String action = intent.getAction();
                        	
    						// alterou a rotação ?
                        	if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                        		
                        		// recents
                        		SystemUI_RecentsActivity.onConfigurationChanged();
                        		
                        		// ----------------------------------------------------------------------
        						// se na rotação do celular o mod estiver habilitado e o painel expandido
        						// estiver aberto, fecha o painel expandido forçando o usuário a expandir
        						// o painel novamente para obtér a imagem desfocada com a rotação atual!!
        						// ----------------------------------------------------------------------
                        		
        						// habilitado ?
        						if (mExpandedVisible &&
        							SystemUI_NotificationPanelView.mBlurredStatusBarExpandedEnabled) {
        					
        							// fecha o painel
        							XposedHelpers.callMethod(thiz, "makeExpandedInvisible");
        						
        						}
                        	}
                        	
                        	// atualiza
                        	if (action.equals(BlurSettingsFragment.BLURRED_SYSTEM_UI_UPDATE_INTENT)) {
                        		
                        		mHandler.postDelayed(new Runnable() {
                                	
                                    @Override
                                    public void run() {
                                    	
                                    	// recarregam as preferências
                                		Xposed.mXSharedPreferences.reload();
                                		
                                		// atualizam as preferências
                                		updatePreferences();
                                		
                                    }
                                }, 100);
                        	}
                        	
                        	// mata
                        	if (action.equals(BlurSettingsActivity.BLURRED_SYSTEM_UI_KILL_SYSTEM_UI_INTENT)) {
				
                        		mHandler.postDelayed(new Runnable() {
                                	
                                    @Override
                                    public void run() {
                                    	
                                    	// mata
                                    	Process.sendSignal(Process.myPid(), Process.SIGNAL_KILL);
                                    	
                                    }
                             	}, 100);
                        	}
                        }
                    };
                    	
                    // registra o receiver
                    IntentFilter intent = new IntentFilter();
                    intent.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
                    intent.addAction(BlurSettingsFragment.BLURRED_SYSTEM_UI_UPDATE_INTENT);
                    intent.addAction(BlurSettingsActivity.BLURRED_SYSTEM_UI_KILL_SYSTEM_UI_INTENT);
                    thiz.mContext.registerReceiver(receiver, intent);
            		
                    // inicia
                    SystemUI_RecentsActivity.init(thiz.mContext);
                    
                    // atualizam as preferências
            		updatePreferences();
            		
				}
			});
			
			// makeExpandedVisible
			XposedHelpers.findAndHookMethod(PhoneStatusBar.class, "makeExpandedVisible", boolean.class, new XC_MethodHook() {
							
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					
					// blur
					SystemUI_NotificationPanelView.startBlurTask();
					
				}
			});
			
			// makeExpandedInvisible
			XposedHelpers.findAndHookMethod(PhoneStatusBar.class, "makeExpandedInvisible", new XC_MethodHook() {
						
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					
					// recicla
					SystemUI_NotificationPanelView.recycle();
					
				}
			});
			
		} catch (Exception e) {
			
			XposedBridge.log(e);
			
		}
	}
	
	private static void updatePreferences() {
		
		// atualiza
		SystemUI_NotificationPanelView.updatePreferences();
		SystemUI_RecentsActivity.updatePreferences();
		SystemUI_NotificationBackgroundView.updatePreferences();
		SystemUI_StatusBarHeaderView.updatePreferences();
		SystemUI_BaseStatusBar.updatePreferences();
		
	}
}