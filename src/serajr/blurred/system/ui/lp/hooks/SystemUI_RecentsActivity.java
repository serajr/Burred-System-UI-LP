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
import android.os.Bundle;
import android.widget.FrameLayout;

import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.views.RecentsView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class SystemUI_RecentsActivity {
	
	public static boolean mBlurredRecentAppsEnabled;
	
	private static int mBlurScale;
	private static int mBlurRadius;
	private static Context mContext;
	private static BlurUtils mBlurUtils;
	private static ColorFilter mColorFilter;
	private static int mBlurDarkColorFilter;
	private static int mBlurMixedColorFilter;
	private static int mBlurLightColorFilter;
	private static RecentsActivity mRecentsActivity;
	private static FrameLayout mRecentsActivityRootView;
	
	public static void hook() {
		
		try {
			
			// RecentsActivity - onCreate
			XposedHelpers.findAndHookMethod(RecentsActivity.class, "onCreate", Bundle.class, new XC_MethodHook() {
				
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					
					// guarda
					mRecentsActivity = (RecentsActivity) param.thisObject;
					
					// obtém os campos
					RecentsView mRecentsView = (RecentsView) XposedHelpers.getObjectField(param.thisObject, "mRecentsView");
					
					// guarda o layout parente do mRecentsView (root)
					mRecentsActivityRootView = (FrameLayout) mRecentsView.getParent();
					
					// obtém o último blurred bitmap
					Bitmap lastBlurredBitmap = BlurTask.getLastBlurredBitmap();
					
					// seta o background ?
					if ((mBlurredRecentAppsEnabled) && (lastBlurredBitmap != null)) {
						
						// cria o drawable com o filtro de cor
						BitmapDrawable blurredDrawable = new BitmapDrawable(lastBlurredBitmap);
						blurredDrawable.setColorFilter(mColorFilter);
						
						// seta
						mRecentsActivityRootView.setBackground(blurredDrawable);
						
					}
				}
			});
										
		} catch (Exception e) {
			
			XposedBridge.log(e);
			
		}
	}
	
	public static void init(Context context) {
		
		// guarda
		mContext = context;
		
		// inicia o BlurUtils
		mBlurUtils = new BlurUtils(mContext);
		
	}
	
	public static void updatePreferences() {
		
		// atualiza
		mBlurScale = Integer.parseInt(Xposed.mXSharedPreferences.getString(BlurSettingsFragment.BLUR_SCALE_PREFERENCE_KEY, BlurSettingsFragment.BLUR_SCALE_PREFERENCE_DEFAULT));
		mBlurRadius = Integer.parseInt(Xposed.mXSharedPreferences.getString(BlurSettingsFragment.BLUR_RADIUS_PREFERENCE_KEY, BlurSettingsFragment.BLUR_RADIUS_PREFERENCE_DEFAULT));
		mBlurDarkColorFilter = Xposed.mXSharedPreferences.getInt(BlurSettingsFragment.BLUR_DARK_COLOR_PREFERENCE_KEY, BlurSettingsFragment.BLUR_DARK_COLOR_PREFERENCE_DEFAULT);
		mBlurMixedColorFilter = Xposed.mXSharedPreferences.getInt(BlurSettingsFragment.BLUR_MIXED_COLOR_PREFERENCE_KEY, BlurSettingsFragment.BLUR_MIXED_COLOR_PREFERENCE_DEFAULT);
		mBlurLightColorFilter = Xposed.mXSharedPreferences.getInt(BlurSettingsFragment.BLUR_LIGHT_COLOR_PREFERENCE_KEY, BlurSettingsFragment.BLUR_LIGHT_COLOR_PREFERENCE_DEFAULT);
		mBlurredRecentAppsEnabled = Xposed.mXSharedPreferences.getBoolean(BlurSettingsFragment.RECENT_APPS_ENABLED_PREFERENCE_KEY, BlurSettingsFragment.RECENT_APPS_ENABLED_PREFERENCE_DEFAULT);
		
	}
	
	public static void onConfigurationChanged() {
		
		// -----------------
		// alterou a rotação
		// -----------------
		
		// recicla
		recycle();
		
	}
	
	public static void startBlurTask() {
		
		// remove o background !
		if (mRecentsActivityRootView != null)
			mRecentsActivityRootView.setBackground(null);
		
		// habilitado ?
		if (!mBlurredRecentAppsEnabled)
			return;
		
		// continua ?
		if (mRecentsActivity != null) {
			
			// obtém os campos
			boolean mVisible = XposedHelpers.getBooleanField(mRecentsActivity, "mVisible");
			
			if (mVisible)
				return;
			
		}
		
		// callback
		BlurTask.setBlurTaskCallback(new BlurTaskCallback() {
			
			@Override
			public void blurTaskDone(final Bitmap blurredBitmap) {
				
				if (blurredBitmap != null) {
					
					// -------------------------
					// bitmap criado com sucesso
					// -------------------------
					
					if (mRecentsActivityRootView != null) {
						
						mRecentsActivityRootView.post(new Runnable() {

							@Override
							public void run() {
				
								// cria o drawable com o filtro de cor
								BitmapDrawable blurredDrawable = new BitmapDrawable(blurredBitmap);
								blurredDrawable.setColorFilter(mColorFilter);
								
								// seta
								mRecentsActivityRootView.setBackground(blurredDrawable);
								
							}
						});
					}
				}
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
	
	private static void recycle() {
		
		if (mRecentsActivityRootView == null)
			return;
		
		// limpa e recicla
		if (mRecentsActivityRootView.getBackground() != null) {
			
			// recicla
		    Bitmap bitmap = ((BitmapDrawable) mRecentsActivityRootView.getBackground()).getBitmap();
		    if (bitmap != null) {
		    	
		    	bitmap.recycle();
		    	bitmap = null;
		    	
		    }
			
			// limpa
		    mRecentsActivityRootView.setBackground(null);	
			
		}
	}
	
	public static class BlurTask extends AsyncTask<Void, Void, Bitmap> {
		
		private static int[] mScreenDimens;
		private static Bitmap mScreenBitmap;
		private static BlurEngine mBlurEngine;
		private static BlurTaskCallback mCallback;
			
		public static void setBlurEngine(BlurEngine blurEngine) {
			
			mBlurEngine = blurEngine;
			
		}
		
		public static void setBlurTaskCallback(BlurTaskCallback callBack) {
			
		    mCallback = callBack;
		    
		}
		
		public static int[] getRealScreenDimensions() {
			
			return mScreenDimens;
			
		}
		
		public static Bitmap getLastBlurredBitmap() {
			
			return mScreenBitmap;
			
		}
		
		@Override
		protected void onPreExecute() {
			
			// obtém o tamamho real da tela
			mScreenDimens = DisplayUtils.getRealScreenDimensions(mContext);
			
			// obtém a screenshot da tela com escala reduzida
			mScreenBitmap = DisplayUtils.takeSurfaceScreenshot(mContext, mBlurScale);
			
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
}