package com.sams3p10l.kioskmodesample.activity

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sams3p10l.kioskmodesample.R
import com.sams3p10l.kioskmodesample.receiver.MyDeviceAdminReceiver

class MainActivity : AppCompatActivity() {

    private lateinit var mAdminComponentName: ComponentName
    private lateinit var mDevicePolicyManager: DevicePolicyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAdminComponentName = MyDeviceAdminReceiver.getComponentName(this)
        mDevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        if (mDevicePolicyManager.isDeviceOwnerApp(packageName))
            mDevicePolicyManager.setPermissionPolicy(mAdminComponentName, R.xml.device_admin_receiver)
        else {
            //TODO exit app?
        }
    }
}
