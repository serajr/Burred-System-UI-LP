package serajr.blurred.system.ui.lp.hooks;

import serajr.blurred.system.ui.lp.Xposed;
import serajr.blurred.system.ui.lp.fragments.BlurSettingsFragment;
import serajr.blurred.system.ui.lp.utils.BlurUtils;
import serajr.blurred.system.ui.lp.utils.BlurUtils.BlurEngine;
import serajr.blurred.system.ui.lp.utils.BlurUtils.BlurTaskCallback;
import serajr.blurred.system.ui.lp.utils.DisplayUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;

import com.android.systemui.statusbar.phone.NotificationPanelView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class SystemUI_NotificationPanelView {
	
	public static boolean mBlurredStatusBarExpandedEnabled;
	public static NotificationPanelView mNotificationPanelView;
	
	private static int mBlurScale;
	private static int mBlurRadius;
	private static View mQsContainer;
	private static BlurUtils mBlurUtils;
	private static FrameLayout mBlurredView;
	private static ColorFilter mColorFilter;
	private static int mBlurDarkColorFilter;
	private static int mBlurMixedColorFilter;
	private static int mBlurLightColorFilter;
	private static int mTranslucencyPercentage;
	private static AlphaAnimation mAlphaAnimation;
	private static boolean mTranslucentQuickSettings;
	private static AnimationListener mAnimationListener = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation anim) {
			
			// visível
			mBlurredView.setVisibility(View.VISIBLE);
			
		}
		
		@Override
		public void onAnimationEnd(Animation anim) {}

		@Override
		public void onAnimationRepeat(Animation anim) {}
		
	};
	
	public static void hook() {
		
		try {
			
			// onFinishInflate
			XposedHelpers.findAndHookMethod(NotificationPanelView.class, "onFinishInflate", new XC_MethodHook() {
				
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				
					// guarda
					mNotificationPanelView = (NotificationPanelView) param.thisObject;
					mQsContainer = (View) XposedHelpers.getObjectField(mNotificationPanelView, "mQsContainer");
					
					// inicia o BlurUtils
					mBlurUtils = new BlurUtils(mNotificationPanelView.getContext());
					
					// animação
					mAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);
					mAlphaAnimation.setDuration(300);
					mAlphaAnimation.setAnimationListener(mAnimationListener);
					
					// cria o mBlurredView
					mBlurredView = new FrameLayout(mNotificationPanelView.getContext());
					
			    	// insere o mBlurredView no mNotificationPanelView na posição 0 (ordem importa)
			    	mNotificationPanelView.addView(mBlurredView, 0, new FrameLayout.LayoutParams(
			    			FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
			    	mNotificationPanelView.requestLayout();
			    	
			    	// seta o tag de: pronto para receber o blur
			    	mBlurredView.setTag("ready_to_blur");
			    	
			    	// invisível
			    	mBlurredView.setVisibility(View.INVISIBLE);
					
			    	// transparente ?
			    	handleQuickSettingsBackround();
			    	
				}
			});
			
			// setBarState
			XposedHelpers.findAndHookMethod(NotificationPanelView.class, "setBarState", int.class, boolean.class, boolean.class, new XC_MethodHook() {
				
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					
					// transparente ?
					handleQuickSettingsBackround();
					SystemUI_StatusBarHeaderView.handleStatusBarHeaderViewBackround();
					
				}
			});
			
		} catch (Exception e) {
			
			XposedBridge.log(e);
			
		}
	}
	
	public static void updatePreferences() {
		
		// atualiza
		mBlurScale = Integer.parseInt(Xposed.mXSharedPreferences.getString(BlurSettingsFragment.BLUR_SCALE_PREFERENCE_KEY, BlurSettingsFragment.BLUR_SCALE_PREFERENCE_DEFAULT));
		mBlurRadius = Integer.parseInt(Xposed.mXSharedPreferences.getString(BlurSettingsFragment.BLUR_RADIUS_PREFERENCE_KEY, BlurSettingsFragment.BLUR_RADIUS_PREFERENCE_DEFAULT));
		mBlurDarkColorFilter = Xposed.mXSharedPreferences.getInt(BlurSettingsFragment.BLUR_DARK_COLOR_PREFERENCE_KEY, BlurSettingsFragment.BLUR_DARK_COLOR_PREFERENCE_DEFAULT);
		mBlurMixedColorFilter = Xposed.mXSharedPreferences.getInt(BlurSettingsFragment.BLUR_MIXED_COLOR_PREFERENCE_KEY, BlurSettingsFragment.BLUR_MIXED_COLOR_PREFERENCE_DEFAULT);
		mBlurLightColorFilter = Xposed.mXSharedPreferences.getInt(BlurSettingsFragment.BLUR_LIGHT_COLOR_PREFERENCE_KEY, BlurSettingsFragment.BLUR_LIGHT_COLOR_PREFERENCE_DEFAULT);
		mTranslucentQuickSettings = Xposed.mXSharedPreferences.getBoolean(BlurSettingsFragment.TRANSLUCENT_QUICK_SETTINGS_PREFERENCE_KEY, BlurSettingsFragment.TRANSLUCENT_QUICK_SETTINGS_PREFERENCE_DEFAULT);
		mBlurredStatusBarExpandedEnabled = Xposed.mXSharedPreferences.getBoolean(BlurSettingsFragment.STATUS_BAR_EXPANDED_ENABLED_PREFERENCE_KEY, BlurSettingsFragment.STATUS_BAR_EXPANDED_ENABLED_PREFERENCE_DEFAULT);
		mTranslucencyPercentage = Integer.parseInt(Xposed.mXSharedPreferences.getString(BlurSettingsFragment.TRANSLUCENT_PRECENTAGE_PREFERENCE_KEY, BlurSettingsFragment.TRANSLUCENT_PRECENTAGE_PREFERENCE_DEFAULT));
		mTranslucencyPercentage = 255 - ((mTranslucencyPercentage * 255) / 100);
		
		// transparente ?
		handleQuickSettingsBackround();
		
	}
	
	private static void handleQuickSettingsBackround() {
		
		// continua ?
		if (mNotificationPanelView == null)
			return;
		
		// obtém os campos
		boolean mKeyguardShowing = XposedHelpers.getBooleanField(mNotificationPanelView, "mKeyguardShowing");
		
		// continua ?
		if (mQsContainer == null)
			return;
		
		if (mKeyguardShowing) {
			
			// opaco !
			mQsContainer.getBackground().setAlpha(255);
			
		} else {
			
			// transparente ?
			mQsContainer.getBackground().setAlpha(mTranslucentQuickSettings ? mTranslucencyPercentage : 255);
			
		}
	}
	
	public static void startBlurTask() {
		
		// habilitado ?
		if (!mBlurredStatusBarExpandedEnabled)
			return;
		
		// não continua se o blur ja foi aplicado !!!
		if (mBlurredView.getTag().toString().equals("blur_applied"))
			return;
		
		// continua ?
		if (mNotificationPanelView == null)
			return;
		
		// obtém os campos
		boolean mKeyguardShowing = XposedHelpers.getBooleanField(mNotificationPanelView, "mKeyguardShowing");
		
		// lockscreen ?
		if (mKeyguardShowing)
			return;
		
		// callback
		BlurTask.setBlurTaskCallback(new BlurTaskCallback() {
			
			@Override
			public void blurTaskDone(Bitmap blurredBitmap) {
				
				if (blurredBitmap != null) {
					
					// -------------------------
					// bitmap criado com sucesso
					// -------------------------
					
					// corrige o width do mBlurredView
					int[] screenDimens = BlurTask.getRealScreenDimensions();
					mBlurredView.getLayoutParams().width = screenDimens[0];
					mBlurredView.requestLayout();
					
					// cria o drawable com o filtro de cor
					BitmapDrawable drawable = new BitmapDrawable(blurredBitmap);
					drawable.setColorFilter(mColorFilter);
					
					// seta o drawable
					mBlurredView.setBackground(drawable);
					
					// seta o tag de: blur aplicado 
					mBlurredView.setTag("blur_applied");
					
				} else {
			
					// ----------------------------
					// bitmap nulo por algum motivo
					// ----------------------------
					
					// seta o filtro de cor
					mBlurredView.setBackgroundColor(mBlurLightColorFilter);
					
					// seta o tag de: erro
					mBlurredView.setTag("error");
					
				}
				
				// anima e mostra o blur
				mBlurredView.startAnimation(mAlphaAnimation);
				
			}

			@Override
			public void dominantColor(int color) {
				
				// obtém a luminosidade da cor dominante
				double lightness = DisplayUtils.getColorLightness(color);
				
				if (lightness >= 0.0 && color <= 1.0) {
					
					// --------------------------------------------------
					// seta o filtro de cor de acordo com a cor dominante
					// --------------------------------------------------
					
					if (lightness <= 0.33) {
					
						// imagem clara (mais perto do branco)
						mColorFilter = new PorterDuffColorFilter(mBlurLightColorFilter, PorterDuff.Mode.MULTIPLY);
						
					} else if (lightness >= 0.34 && lightness <= 0.66) {
						
						// imagem mista
						mColorFilter = new PorterDuffColorFilter(mBlurMixedColorFilter, PorterDuff.Mode.MULTIPLY);
						
					} else if (lightness >= 0.67 && lightness <= 1.0) {
						
						// imagem clara (mais perto do preto)
						mColorFilter = new PorterDuffColorFilter(mBlurDarkColorFilter, PorterDuff.Mode.MULTIPLY);
						
					}
					
				} else {
					
					// -------
					// erro !!
					// -------
					
					// seta a cor mista
					mColorFilter = new PorterDuffColorFilter(mBlurMixedColorFilter, PorterDuff.Mode.MULTIPLY);
					
				}
			}
		});
		
		// engine
		BlurTask.setBlurEngine(BlurEngine.RenderScriptBlur);
		
		// blur
		new BlurTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		
	}
	
	public static void recycle() {
		
		// limpa e recicla
		if (mBlurredView != null &&
			mBlurredView.getBackground() != null) {
	
			// bitmap ?
			if (mBlurredView.getBackground() instanceof BitmapDrawable) {
				
				// recicla
			    Bitmap bitmap = ((BitmapDrawable) mBlurredView.getBackground()).getBitmap();
			    if (bitmap != null) {
			    	
			    	bitmap.recycle();
			    	bitmap = null;
			    	
			    }
			}
			
			// limpa
			mBlurredView.setBackground(null);
			
		}
		
		// seta o tag de: pronto para receber o blur
		mBlurredView.setTag("ready_to_blur");
		
		// invisível
    	mBlurredView.setVisibility(View.INVISIBLE);
		
	}
	
	public static class BlurTask extends AsyncTask<Void, Void, Bitmap> {
		
		private static int[] mScreenDimens;
		private static BlurEngine mBlurEngine;
		private static BlurTaskCallback mCallback;
		
		private Bitmap mScreenBitmap;
			
		public static void setBlurEngine(BlurEngine blurEngine) {
			
			mBlurEngine = blurEngine;
			
		}
		
		public static void setBlurTaskCallback(BlurTaskCallback callBack) {
			
		    mCallback = callBack;
		    
		}
		
		public static int[] getRealScreenDimensions() {
			
			return mScreenDimens;
			
		}
		
		@Override
		protected void onPreExecute() {
			
			Context context = mNotificationPanelView.getContext(); 
			
			// obtém o tamamho real da tela
			mScreenDimens = DisplayUtils.getRealScreenDimensions(context);
			
			// obtém a screenshot da tela com escala reduzida
			mScreenBitmap = DisplayUtils.takeSurfaceScreenshot(context, mBlurScale);
			
		}
		
		@Override
		protected Bitmap doInBackground(Void... arg0) {
			
			try {
			
				// continua ?
				if (mScreenBitmap == null)
					return null;
				
				// calback
				mCallback.dominantColor(DisplayUtils.getDominantColorByPixelsSampling(mScreenBitmap, 10, 10));
				
				// blur engine
				if (mBlurEngine == BlurEngine.RenderScriptBlur) {
					
					mScreenBitmap = mBlurUtils.renderScriptBlur(mScreenBitmap, mBlurRadius);
					
				} else if (mBlurEngine == BlurEngine.StackBlur) {
					
					mScreenBitmap = mBlurUtils.stackBlur(mScreenBitmap, mBlurRadius);
					
				} else if (mBlurEngine == BlurEngine.FastBlur) {
					
					mBlurUtils.fastBlur(mScreenBitmap, mBlurRadius);
					
				}
					
				return mScreenBitmap;
				
			} catch (OutOfMemoryError e) {
				
				// erro
				return null;
				
			}
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			
			if (bitmap != null) {
				
				// -----------------------------
				// bitmap criado com sucesso !!!
				// -----------------------------
				
				// callback
				mCallback.blurTaskDone(bitmap);
				
			} else {
				
				// --------------------------
				// erro ao criar o bitmap !!!
				// --------------------------
					
				// callback
				mCallback.blurTaskDone(null);
				
			}
		}
	}
	
//	public class StatusBarState {
//		
//	    // the status bar is in the "normal" shade mode.
//	    public static final int SHADE = 0;
//	    
//	    // status bar is currently the Keyguard.
//	    public static final int KEYGUARD = 1;
//	    
//	    // status bar is in the special mode, where it is fully interactive but
//	    // still locked. So dismissing the shade will still show the bouncer !!
//	    public static final int SHADE_LOCKED = 2;
//
//	}
}