package com.sams3p10l.kioskmodesample.activity

import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.UserManager
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Toast
import com.sams3p10l.kioskmodesample.MultiTapDetector
import com.sams3p10l.kioskmodesample.R
import com.sams3p10l.kioskmodesample.receiver.MyDeviceAdminReceiver
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.properties.Delegates
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var mAdminComponentName: ComponentName
    private lateinit var mDevicePolicyManager: DevicePolicyManager
    private var applyPolicies by Delegates.notNull<Boolean>()

    private var isAdmin = false

    companion object {
        const val ACTIVITY_KEY = "com.sams3p10l.kioskmodesample.activity.MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        applyPolicies = intent?.getBooleanExtra("back_enabled", true) ?: true

        mAdminComponentName = MyDeviceAdminReceiver.getComponentName(this)
        mDevicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        if (mDevicePolicyManager.isDeviceOwnerApp(packageName)) {
            isAdmin = true
        } else {
            isAdmin = false
            Toast.makeText(this, "App isn't device owner!", Toast.LENGTH_LONG).show()
        }

        if (applyPolicies)
            setKioskPolicies(true, isAdmin)
        else
            setKioskPolicies(false, isAdmin)
    }

    override fun onStart() {
        super.onStart()

        /*triple tap is a toggle - disable/enable policies*/
        MultiTapDetector(secretKey) { taps, last ->
            if (taps == 3 && last) {
                if (applyPolicies) { //disable policies
                    applyPolicies = false

                    val intent = Intent(applicationContext, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        putExtra(ACTIVITY_KEY, false)
                        putExtra("back_enabled", applyPolicies)
                    }

                    startActivity(intent)
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.secret_key_string),
                        Toast.LENGTH_LONG
                    ).show()
                } else { //reenable policies
                    applyPolicies = true

                    val intent = Intent(applicationContext, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        putExtra(ACTIVITY_KEY, false)
                        putExtra("back_enabled", applyPolicies)
                    }

                    startActivity(intent)
                }
            }
        }
    }

    private fun setKioskPolicies(enable: Boolean, isAdmin: Boolean) {
        if (isAdmin) {
            setRestrictions(enable)
            stayOnWhilePluggedIn(enable)
            setUpdatePolicy(enable)
            setAsHomeApp(enable)
            setKeyGuardDisabled(enable)
        }
        setLockTask(enable, isAdmin)
        setImmersiveMode(enable)
    }

    private fun setRestrictions(restrict: Boolean) {
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, restrict)
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, restrict)
        setUserRestriction(UserManager.DISALLOW_ADD_USER, restrict)
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, restrict)
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, restrict)
    }

    private fun setUserRestriction(restriction: String, restrict: Boolean) = if (restrict) {
        mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction)
    } else {
        mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction)
    }

    private fun stayOnWhilePluggedIn(active: Boolean) = if (active) {
        mDevicePolicyManager.setGlobalSetting(
            mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            (BatteryManager.BATTERY_PLUGGED_AC or
                    BatteryManager.BATTERY_PLUGGED_USB or
                    BatteryManager.BATTERY_PLUGGED_WIRELESS).toString()
        )
    } else {
        mDevicePolicyManager.setGlobalSetting(
            mAdminComponentName,
            Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            "0"
        )
    }

    /*enable true - updates are allowed in a time window; enable false - updates are disallowed*/
    private fun setUpdatePolicy(enable: Boolean) = if (enable) {
        mDevicePolicyManager.setSystemUpdatePolicy(
            mAdminComponentName,
            SystemUpdatePolicy.createWindowedInstallPolicy(60, 120)
        ) //numbers are minutes of the day when updates are allowed
    } else {
        mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, null)
    }

    private fun setAsHomeApp(enable: Boolean) = if (enable) {
        val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        mDevicePolicyManager.addPersistentPreferredActivity(
            mAdminComponentName, intentFilter,
            ComponentName(packageName, MainActivity::class.java.name)
        )
    } else {
        mDevicePolicyManager.clearPackagePersistentPreferredActivities(
            mAdminComponentName,
            packageName
        )
    }

    private fun setKeyGuardDisabled(enable: Boolean) {
        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, enable)
    }

    private fun setLockTask(enableTask: Boolean, isAdmin: Boolean) {
        if (isAdmin) {
            mDevicePolicyManager.setLockTaskPackages(
                mAdminComponentName,
                if (enableTask) arrayOf(packageName) else arrayOf()
            )
        }

        if (enableTask) {
            startLockTask()
        } else {
            stopLockTask()
        }
    }

    private fun setImmersiveMode(enable: Boolean) = if (enable) {
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        window.decorView.systemUiVisibility = flags
    } else {
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        window.decorView.systemUiVisibility = flags
    }

    override fun onBackPressed() {
        if (!applyPolicies) {
            finishAndRemoveTask()
            exitProcess(0)
        }
    }

}
