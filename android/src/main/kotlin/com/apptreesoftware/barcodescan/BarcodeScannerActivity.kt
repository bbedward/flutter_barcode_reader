package com.apptreesoftware.barcodescan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import android.R
import android.graphics.Color
import android.view.WindowManager






class BarcodeScannerActivity : Activity(), ZXingScannerView.ResultHandler {

    lateinit var scannerView: me.dm7.barcodescanner.zxing.ZXingScannerView

    var actionBarColor = Color.WHITE

    companion object {
        val REQUEST_TAKE_PHOTO_CAMERA_PERMISSION = 100
        val TOGGLE_FLASH = 200

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = ""
        val p = intent.extras
        val theme = p!!.getString("theme");
        scannerView = ZXingScannerView(this)
        scannerView.setAutoFocus(true)
        scannerView.setLaserEnabled(false)
        scannerView.setSquareViewFinder(true)
        if (theme != null && theme.equals("kalium")) {
            actionBarColor = 0xFFFBDD11.toInt()
            scannerView.setBorderColor(0xFFFBDD11.toInt())
            actionBar.setBackgroundDrawable(ColorDrawable(0xFF212124.toInt()))
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = 0xFF212124.toInt()
            }
        } else if (theme != null && theme.equals("natrium")) {
            actionBarColor = 0xFFA3CDFF.toInt()
            scannerView.setBorderColor(0xFFA3CDFF.toInt())
            actionBar.setBackgroundDrawable(ColorDrawable(0xFF2A3A4D.toInt()))
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = 0xFF2A3A4D.toInt()
            }
        }
        setContentView(scannerView)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (scannerView.flash) {
            val item = menu.add(0,
                    TOGGLE_FLASH, 0, "Flash Off")
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            val spanString = SpannableString(item.title.toString())
            spanString.setSpan(ForegroundColorSpan(actionBarColor), 0, spanString.length, 0)
            item.title = spanString
        } else {
            val item = menu.add(0,
                    TOGGLE_FLASH, 0, "Flash On")
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            val spanString = SpannableString(item.title.toString())
            spanString.setSpan(ForegroundColorSpan(actionBarColor), 0, spanString.length, 0)
            item.title = spanString
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == TOGGLE_FLASH) {
            scannerView.flash = !scannerView.flash
            this.invalidateOptionsMenu()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        scannerView.setResultHandler(this)
        // start camera immediately if permission is already given
        if (!requestCameraAccessIfNecessary()) {
            scannerView.startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        scannerView.stopCamera()
    }

    override fun handleResult(result: Result?) {
        val intent = Intent()
        intent.putExtra("SCAN_RESULT", result.toString())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun finishWithError(errorCode: String) {
        val intent = Intent()
        intent.putExtra("ERROR_CODE", errorCode)
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    private fun requestCameraAccessIfNecessary(): Boolean {
        val array = arrayOf(Manifest.permission.CAMERA)
        if (ContextCompat
                .checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, array,
                    REQUEST_TAKE_PHOTO_CAMERA_PERMISSION)
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,grantResults: IntArray) {
        when (requestCode) {
            REQUEST_TAKE_PHOTO_CAMERA_PERMISSION -> {
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    scannerView.startCamera()
                } else {
                    finishWithError("PERMISSION_NOT_GRANTED")
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}

object PermissionUtil {

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value [PackageManager.PERMISSION_GRANTED].

     * @see Activity.onRequestPermissionsResult
     */
    fun verifyPermissions(grantResults: IntArray): Boolean {
        // At least one result must be checked.
        if (grantResults.size < 1) {
            return false
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}
