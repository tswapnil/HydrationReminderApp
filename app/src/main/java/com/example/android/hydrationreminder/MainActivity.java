/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.hydrationreminder;

import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.*;


public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView mWaterCountDisplay;
    private TextView mChargingCountDisplay;
    private ImageView mChargingImageView;
    private IntentFilter intentFilter;
    private BroadcastReceiver mBroadCastReceiver;
    private Toast mToast;

    private void showCharging (boolean isCharging){
        if(isCharging){
            mChargingImageView.setImageResource(R.drawable.ic_power_pink_80px);
        }
        else{
            mChargingImageView.setImageResource(R.drawable.ic_power_grey_80px);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** Get the views **/
        mWaterCountDisplay = (TextView) findViewById(R.id.tv_water_count);
        mChargingCountDisplay = (TextView) findViewById(R.id.tv_charging_reminder_count);
        mChargingImageView = (ImageView) findViewById(R.id.iv_power_increment);

        /** Set the original values in the UI **/
        updateWaterCount();
        updateChargingReminderCount();

        ReminderUtilities.scheduleChargingReminder(this);
        /** Setup the shared preference listener **/
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        mBroadCastReceiver = new ChargingBroadcastReceiver();
    }

    /**
     * Updates the TextView to display the new water count from SharedPreferences
     */
    private void updateWaterCount() {
        int waterCount = PreferenceUtilities.getWaterCount(this);
        mWaterCountDisplay.setText(waterCount+"");
    }

    /**
     * Updates the TextView to display the new charging reminder count from SharedPreferences
     */
    private void updateChargingReminderCount() {
        int chargingReminders = PreferenceUtilities.getChargingReminderCount(this);
        String formattedChargingReminders = getResources().getQuantityString(
                R.plurals.charge_notification_count, chargingReminders, chargingReminders);
        mChargingCountDisplay.setText(formattedChargingReminders);

    }

    /**
     * Adds one to the water count and shows a toast
     */
    public void incrementWater(View view) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, R.string.water_chug_toast, Toast.LENGTH_SHORT);
        mToast.show();
        Intent intent = new Intent(this, WaterReminderIntentService.class);
        intent.setAction(ReminderTasks.ACTION_INCREMENT_WATER_COUNT);
        startService(intent);
        // TODO (15) Create an explicit intent for WaterReminderIntentService
        // TODO (16) Set the action of the intent to ACTION_INCREMENT_WATER_COUNT
        // TODO (17) Call startService and pass the explicit intent you just created
    }
    public void testNotification(View view){
        NotificationUtils.remindUserBecauseCharging(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /** Cleanup the shared preference listener **/
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * This is a listener that will update the UI when the water count or charging reminder counts
     * change
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PreferenceUtilities.KEY_WATER_COUNT.equals(key)) {
            updateWaterCount();
        } else if (PreferenceUtilities.KEY_CHARGING_REMINDER_COUNT.equals(key)) {
            updateChargingReminderCount();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                  BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
                 showCharging(batteryManager.isCharging());
                    }
                else {
                                            // sticky broadcast that contains a lot of information about the battery state.
                     IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    // for the receiver. Pass in your intent filter as well. Passing in null means that you're
                    // getting the current state of a sticky broadcast - the intent returned will contain the
                    // battery information you need.
                   Intent currentBatteryStatusIntent = registerReceiver(null, ifilter);
                    // BatteryManager.BATTERY_STATUS_CHARGING or BatteryManager.BATTERY_STATUS_FULL. This means
                    // the battery is currently charging.
                int batteryStatus = currentBatteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                 boolean isCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                                        batteryStatus == BatteryManager.BATTERY_STATUS_FULL;
                                showCharging(isCharging);
                    }

                /** Register the receiver for future state changes **/
                 registerReceiver(mBroadCastReceiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadCastReceiver);
    }

    private class ChargingBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean isCharging = (action.equals(Intent.ACTION_POWER_CONNECTED));
            showCharging(isCharging);
        }
    }
}