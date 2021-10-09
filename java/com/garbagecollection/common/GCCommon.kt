package com.garbagecollection.common


import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.garbagecollection.R
import com.google.android.material.snackbar.Snackbar
/**
 * Created by Mithilesh Kumar on 26/08/2021.
 */

class GCCommon {

    companion object {
        const val MY_MARKET_INFO = "x-my-markers-information"

        //TOILET_CONSTANT
        var install = "Install"
        var selectedGarbage = ""

        var note = "Note"
        var selectedPosition = "Selected Position"
        var extraPumpOut = "Extra Pump Out"
        var trailerPumpOut = "Trailer Pump Out"

        //RESIDENTIAL_CONSTANT
        var bag = "Bag"
        var bagTag = "Bag Tag"
        var two_ty = "2TY"
        var four_ty = "5TY"
        var toter = "Toter"

        //FCM Common
        var notificationToken = ""
        var deviceId = ""
        const val deviceType = "A"

        var closeBtnBoolean: Boolean = false


        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
        }

        //bitmap
        fun BitmapFromVector(context: Context, vectorResId: Int, tint: String): Bitmap? {
            val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
            vectorDrawable!!.setBounds(
                0,
                0,
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight
            )
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val paint = Paint()
            paint.colorFilter =
                PorterDuffColorFilter(Color.parseColor(tint), PorterDuff.Mode.SRC_IN)
            val bitmapResult =
                Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmapResult)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            vectorDrawable.draw(canvas)


            return bitmapResult
        }


        fun getMarkerIconFromDrawable(drawable: Drawable): Bitmap? {
            val canvas = Canvas()
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            canvas.setBitmap(bitmap)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(canvas)
            return bitmap
        }

        fun newBitmapFromVector(context: Context, vectorDrawable: Drawable, tint: String): Bitmap? {
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val paint = Paint()
            paint.colorFilter =
                PorterDuffColorFilter(Color.parseColor(tint), PorterDuff.Mode.SRC_IN)
            val bitmapResult =
                Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmapResult)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            vectorDrawable.draw(canvas)
            return bitmapResult
        }

        /* Show warning snackbar if user on home fragment and click on back button. */
        fun showExitWarning(layout: ViewGroup, activity: AppCompatActivity) {
            val snack =
                Snackbar.make(layout, "To exit, Tap back button again.", Snackbar.LENGTH_SHORT)
            snack.setActionTextColor(ContextCompat.getColor(activity, R.color.white))
            snack.setAction("EXIT NOW") {
                activity.finish()
            }
            val snackBarView = snack.view
            snackBarView.setBackgroundColor(ContextCompat.getColor(activity, R.color.bg_color))
            snack.show()
        }

        fun setLoadingDialog(activity: AppCompatActivity): Dialog {


            val dialog = Dialog(activity)
            dialog.show()
            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            dialog.setContentView(R.layout.loader)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            return dialog
        }

    }


}