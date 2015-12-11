package serajr.blurred.system.ui.lp;

import serajr.blurred.system.ui.lp.hooks.SystemUI_BaseStatusBar;
import serajr.blurred.system.ui.lp.hooks.SystemUI_NotificationBackgroundView;
import serajr.blurred.system.ui.lp.hooks.SystemUI_NotificationPanelView;
import serajr.blurred.system.ui.lp.hooks.SystemUI_PhoneStatusBar;
import serajr.blurred.system.ui.lp.hooks.SystemUI_RecentsActivity;
import serajr.blurred.system.ui.lp.hooks.SystemUI_StatusBarHeaderView;
import android.content.res.XModuleResources;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Xposed implements IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage {
	
	public static String MODULE_PACKAGE_NAME = "serajr.blurred.system.ui.lp";
	public static String ANDROID_PACKAGE_NAME = "android";
	public static String SYSTEM_UI_PACKAGE_NAME = "com.android.systemui";
	
	public static String mModulePath;
	public static ClassLoader mClassLoader;
	public static XSharedPreferences mXSharedPreferences;
	public static XModuleResources mXModuleResources;
	public static InitPackageResourcesParam mInitPackageResourcesParam;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		
		mModulePath = startupParam.modulePath;
		mXSharedPreferences = new XSharedPreferences(MODULE_PACKAGE_NAME);
		
//		// recarregam as preferências
//		mXSharedPreferences.reload();
//		
//		// hooks
		
	}
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
    	
    		mClassLoader = lpparam.classLoader;
    	
    		if (lpparam.packageName.equals(SYSTEM_UI_PACKAGE_NAME)) {
    		
    			// setam os class loaderes parentes
    			setParentClassLoaders(lpparam);
    	
    			// recarregam as preferências
    			mXSharedPreferences.reload();
    		
			// hooks
	    		SystemUI_PhoneStatusBar.hook();
	    		SystemUI_BaseStatusBar.hook();
	    		SystemUI_NotificationPanelView.hook();
	    		SystemUI_RecentsActivity.hook();
	    		SystemUI_NotificationBackgroundView.hook();
	    		SystemUI_StatusBarHeaderView.hook();
    		   			
    		}
    	}
    
    	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
    	
		mInitPackageResourcesParam = resparam;
    		mXModuleResources = XModuleResources.createInstance(mModulePath, resparam.res);
    	
    		if (resparam.packageName.equals(SYSTEM_UI_PACKAGE_NAME)) {
    		
//    		// recarregam as preferências
//    		mXSharedPreferences.reload();
//    		
//    		// hooks

    		}
    	}
    
    	private void setParentClassLoaders(LoadPackageParam lpparam) throws Throwable {
    	
    		// todos os classloaders
    		ClassLoader packge = lpparam.classLoader;
    		ClassLoader module = getClass().getClassLoader();
    		ClassLoader xposed = module.getParent();
    	
    		// package classloader parente é: xposed classloader 
    		XposedHelpers.setObjectField(packge, "parent", xposed);
    	
    		// módulo parente classcloader é: package classloader
    		XposedHelpers.setObjectField(module, "parent", packge);
    	
    	}
}
