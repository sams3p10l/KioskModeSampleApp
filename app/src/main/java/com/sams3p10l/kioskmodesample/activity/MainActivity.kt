package com.sams3p10l.kioskmodesample.activity

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Toast
import com.sams3p10l.kioskmodesample.MultiTapDetector
import com.sams3p10l.kioskmodesample.R
import com.sams3p10l.kioskmodesample.receiver.MyDeviceAdminReceiver
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mAdminComponentName: ComponentName
    private lateinit var mDevicePolicyManager: DevicePolicyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAdminComponentName = MyDeviceAdminReceiver.getComponentName(this)
        mDevicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        if (mDevicePolicyManager.isDeviceOwnerApp(packageName)) {
            mDevicePolicyManager.setPermissionPolicy(
                mAdminComponentName,
                R.xml.device_admin_receiver
            )
            mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, arrayOf(packageName))

            startLockTask()

            val intentFilter = IntentFilter(Intent.ACTION_MAIN)
            intentFilter.addCategory(Intent.CATEGORY_HOME)
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT)

            mDevicePolicyManager.addPersistentPreferredActivity(
                mAdminComponentName, intentFilter,
                ComponentName(packageName, MainActivity::class.java.name)
            )
            mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, true)
            mDevicePolicyManager.setGlobalSetting(
                mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                (BatteryManager.BATTERY_PLUGGED_AC or
                        BatteryManager.BATTERY_PLUGGED_USB or
                        BatteryManager.BATTERY_PLUGGED_WIRELESS).toString()
            )

            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

            window.decorView.systemUiVisibility = flags

        } else {
            //TODO exit app?
            Toast.makeText(this, "Device is not admin", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()

        MultiTapDetector(secretKey) {taps, last ->
            if (taps == 3 && last) {
                Toast.makeText(applicationContext, "RADI CALLBACK", Toast.LENGTH_SHORT).show()
            } else
                Toast.makeText(applicationContext, "op op", Toast.LENGTH_SHORT).show()
        }
    }

}
