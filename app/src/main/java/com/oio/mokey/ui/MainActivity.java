package com.oio.mokey.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import com.oio.mokey.R;

import androidx.annotation.Nullable;


public class MainActivity extends Activity {

   private PackageManager mPackageManager;
   private String[] mPackages;

   public static final String douyin = "com.ss.android.ugc.aweme";

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      findViewById(R.id.bt_service).setOnClickListener(v -> goAccess());
      findViewById(R.id.bt_app).setOnClickListener(v -> goApp());
      findViewById(R.id.bt_float_service).setOnClickListener(v -> {
         FloatingWidgetService.requestOverlayPermission(MainActivity.this);
      });

      mPackageManager = this.getPackageManager();

      mPackages = new String[]{douyin};

      try{
         startService(new Intent(this,FloatingWidgetService.class));

      }catch (Throwable e){

      }

   }

   public void goAccess() {
      BaseAccessibilityService.goAccess(this);
   }


   public void cleanProcess() {
      for (String mPackage : mPackages) {
         Intent intent = new Intent();
         intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
         Uri uri = Uri.fromParts("package", mPackage, null);
         intent.setData(uri);
         startActivity(intent);
      }
   }


   public void goApp() {
      Intent intent = new Intent();
      intent.setComponent(new ComponentName(douyin,douyin+".splash.SplashActivity"));
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
   }
}
