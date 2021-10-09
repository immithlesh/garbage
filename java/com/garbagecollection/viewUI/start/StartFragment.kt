package com.garbagecollection.viewUI.start

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.garbagecollection.R
import com.garbagecollection.app.GCBaseFragment
import com.garbagecollection.common.GCCommon
import com.garbagecollection.databinding.FragmentStartBinding
import com.garbagecollection.utils.PrefUtils
import com.garbagecollection.viewUI.weekday.WeekDayFragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.fragment_start.*
/**
 * Created by Mithilesh Kumar on 26/08/2021.
 */
class StartFragment : GCBaseFragment<FragmentStartBinding>() {
    override fun getCurrentFragment(): Fragment {
        return this@StartFragment
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_start
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewDataBinding!!.startId.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.dialog_txt_color
            )
        )
        viewDataBinding!!.linearLayId.setBackgroundResource(R.drawable.circular_button)
        viewDataBinding!!.linearLayId.setOnClickListener {
            if (!GCCommon.isNetworkAvailable(getContainerActivity())) {
                val pref = PrefUtils.retrieveUserInfo(getContainerActivity())
                if (pref == null) {
                    AlertDialog.Builder(requireActivity()).setTitle("Alert")
                        .setMessage("Don't have data. Please turn on your internet connection")
                        .setPositiveButton(
                            "Ok"
                        ) { dialog, which ->
                            linearLayId.setBackgroundResource(
                                R.drawable.circular_button
                            )
                            viewDataBinding!!.startId.setTextColor(
                                ContextCompat.getColor(
                                    requireActivity(),
                                    R.color.bg_color
                                )
                            )

                            dialog.dismiss()
                        }.create().show()
                } else {

                    viewDataBinding!!.startId.setTextColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.white
                        )
                    )
                    linearLayId.setBackgroundResource(
                        R.drawable.circular_btn_selected
                    )
                    Dexter.withContext(getContainerActivity())
                        .withPermissions(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                                when {
                                    report.isAnyPermissionPermanentlyDenied -> {
                                        Toast.makeText(
                                            getContainerActivity(),
                                            "please give permissions",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        openSettings()
                                    }
                                    report.areAllPermissionsGranted() -> {
                                        displayIt(
                                            WeekDayFragment(),
                                            WeekDayFragment::class.java.canonicalName,
                                            true,
                                            true
                                        )
                                    }
                                    else -> {
                                        Toast.makeText(
                                            getContainerActivity(),
                                            "please give permissions",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: List<PermissionRequest>,
                                token: PermissionToken
                            ) {
                                token.continuePermissionRequest()
                            }
                        }).check()

                }
            } else {
                viewDataBinding!!.startId.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
                viewDataBinding!!.linearLayId.setBackgroundResource(R.drawable.circular_btn_selected)
                Dexter.withContext(getContainerActivity())
                    .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                            when {
                                report.isAnyPermissionPermanentlyDenied -> {
                                    Toast.makeText(
                                        getContainerActivity(),
                                        "please give permissions",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    openSettings()
                                }
                                report.areAllPermissionsGranted() -> {
                                    displayIt(
                                        WeekDayFragment(),
                                        WeekDayFragment::class.java.canonicalName,
                                        true,
                                        true
                                    )
                                }
                                else -> {
                                    Toast.makeText(
                                        getContainerActivity(),
                                        "please give permissions",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: List<PermissionRequest>,
                            token: PermissionToken
                        ) {
                            token.continuePermissionRequest()
                        }
                    }).check()

            }
        }
    }

    fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity?.packageName, null)
        intent.data = uri
        activity?.startActivityForResult(intent, 101)
    }

}