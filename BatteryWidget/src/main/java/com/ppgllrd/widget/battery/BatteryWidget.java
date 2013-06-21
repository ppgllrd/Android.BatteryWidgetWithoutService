package com.ppgllrd.widget.battery;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.RemoteViews;


/**
 * Created by pepeg on 21/06/13.
 */
public class BatteryWidget extends AppWidgetProvider {

    private static final String LogTag = BatteryWidget.class.getName();
    private static int previousLevel = -1;
    private static int previousStatus = -1;

    private static int level = -1;
    private static int status = -1;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        //Log.d(BatteryWidget.LogTag, "onUpdate");
        Context appContext = context.getApplicationContext();
        if(appContext != null)
            appContext.registerReceiver(this, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        updateView(context, 0, false);
    }

/*
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(BatteryWidget.LogTag, "onDeleted");
        Context appContext = context.getApplicationContext();
        if(appContext != null)
            appContext.unregisterReceiver(this);
        super.onDeleted(context, appWidgetIds);
    }
*/

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d(BatteryWidget.LogTag, "onReceive");
        try {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            if(level != previousLevel || status != previousStatus) {
                previousLevel = level;
                previousStatus = status;
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);

                if (scale != 100) {
                    if (scale <= 0) scale = 100;
                    level = (100 * level) / scale;
                }
                boolean charging = (level == BatteryManager.BATTERY_STATUS_CHARGING);
                updateView(context, level, charging);

            }
        } catch (Exception e) {
            Log.e(BatteryWidget.LogTag, "onReceive", e);
        }
        super.onReceive(context, intent);
    }



    private static final int bitmapSz = 150;

    private static RectF mkRectF(int radius) {
        int margin = (bitmapSz - radius)/2;
        return new RectF(margin,margin,bitmapSz-margin,bitmapSz-margin);
    }

    private static int radius = 100;

    private final RectF rectF1 = mkRectF(radius);
    private final RectF rectF2 = mkRectF(radius+4);

    private void updateView(Context context, int level, boolean charging) {
        Context appContext = context.getApplicationContext();
        if (appContext != null) {
            RemoteViews thisViews = new RemoteViews(appContext.getPackageName(), R.layout.widget);
            thisViews.setTextViewText(R.id.batteryLevel, Integer.toString(level));

            //create a bitmap
            Bitmap bitmap = Bitmap.createBitmap(bitmapSz, bitmapSz, Bitmap.Config.ARGB_4444);

            //create a canvas from existing bitmap that will be used for drawing
            Canvas canvas = new Canvas(bitmap);

            //create new paint
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(28, 0, 0, 0));
            canvas.drawArc(rectF1, 0, 360, false, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(charging ? Color.GREEN : Color.GRAY);
            paint.setStrokeWidth(charging ? 6 : 3);

            canvas.drawArc(charging ? rectF1 : rectF2, 0, 360, false, paint);

            int color;
            if (level < 15)
                color = Color.argb(255, 255, 10, 10);
            else if (level < 30)
                color = Color.argb(255, 255, 128, 0);
            else
                color = Color.argb(255, 0, 128, 255);
            paint.setColor(color);
            paint.setStrokeWidth(10);
            //canvas.drawArc(new RectF(25,25,125,125),-90+(100-level)*360/100,level*360/100,false,paint);
            canvas.drawArc(rectF1, -90, -level * 360 / 100, false, paint);
            thisViews.setImageViewBitmap(R.id.circleImageView, bitmap);

            AppWidgetManager appWM = AppWidgetManager.getInstance(context);
            if(appWM != null)
                appWM.updateAppWidget(new ComponentName(context,BatteryWidget.class), thisViews);
        }
    }
}