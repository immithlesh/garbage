package com.garbagecollection.base

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import com.garbagecollection.R
import com.garbagecollection.common.GCCommon
import com.garbagecollection.network.ApiClient
import com.garbagecollection.network.ApiService
import com.garbagecollection.viewUI.customers_list.CustomerListFragment
import com.garbagecollection.viewUI.map.MapContainerFragment
import com.garbagecollection.viewUI.weekday.WeekDayFragment
import io.reactivex.disposables.CompositeDisposable
import java.util.*
/**
 * Created by Mithilesh Kumar on 26/08/2021.
 */
abstract class BaseActivity<T : ViewDataBinding?> : AppCompatActivity(),
    BaseFragment.Callback, FragmentManager.OnBackStackChangedListener {
    private var fragmentTransaction: FragmentTransaction? = null
    private var mHandler: Handler? = null
    private var mDialog: Dialog? = null
    var currentFragment: Fragment? = null
    var viewDataBinding: T? = null
    private var service: ApiService? = null
    val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mHandler = Handler()
        performDataBinding()
        service = ApiClient().getClient(this).create(ApiService::class.java)


    }

    open fun getApiService(): ApiService? {
        return service
    }

    private fun performDataBinding() {
        viewDataBinding = DataBindingUtil.setContentView(this, getLayoutId())
    }

    override fun onFragmentAttached() {}
    override fun onFragmentDetached(tag: String?) {}
    fun showLoading() {
        hideLoading()
        mDialog = GCCommon.setLoadingDialog(this)
    }

    fun hideLoading() {
        if (mDialog != null && mDialog!!.isShowing) {
            mDialog!!.cancel()
        }
    }

    abstract fun getCurrentActivity(): Activity?

    /**
     * @return layout resource id
     */
    @LayoutRes
    abstract fun getLayoutId(): Int

    fun displayIt(mFragment: Fragment?, tag: String?, isBack: Boolean, allowAnim: Boolean) {
        mHandler!!.post {
            currentFragment = mFragment
            fragmentTransaction = supportFragmentManager
                .beginTransaction()
            if (isBack) {
                fragmentTransaction!!.addToBackStack(tag)
            }
            if (allowAnim) {
                if (allowAnim) {
                    fragmentTransaction!!.setCustomAnimations(
                        R.anim.enter,
                        R.anim.exit,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right
                    )
                }
            }
            fragmentTransaction!!
                .replace(setContainerLayout(), currentFragment!!, tag)
                .commitAllowingStateLoss()
        }
    }

    fun setArguments(mFragment: Fragment, mBundle: Bundle?): Fragment {
        if (mBundle != null) {
            mFragment.arguments = mBundle
        }
        return mFragment
    }

    val Exit = { dialog: DialogInterface, which: Int ->
        finish()
    }

    val cancel = { dialog: DialogInterface, which: Int ->
        dialog.dismiss()
    }

    fun ExitAlert(message: String, header: String) {
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle(header)
            setMessage(message)
            setCancelable(false)
            setPositiveButton(getString(R.string.exit), DialogInterface.OnClickListener(Exit))
            setNegativeButton(getString(R.string.canel), DialogInterface.OnClickListener(cancel))
            show()
        }
    }

    fun ExitAlert2(message: String, header: String) {
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle(header)
            setMessage(message)
            setCancelable(false)
            setPositiveButton(
                getString(R.string.yes),
                DialogInterface.OnClickListener { dialog, which ->
                    supportFragmentManager.popBackStackImmediate()
                })
            setNegativeButton(getString(R.string.canel), DialogInterface.OnClickListener(cancel))
            show()
        }
    }

    fun ExitAlert3(message: String, header: String) {
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle(header)
            setMessage(message)
            setCancelable(false)
            setPositiveButton(
                getString(R.string.ok),
                DialogInterface.OnClickListener { dialog, which ->
                    supportFragmentManager.popBackStackImmediate()
                })
            setNegativeButton(getString(R.string.canel), DialogInterface.OnClickListener(cancel))
            show()
        }
    }

    abstract fun setContainerLayout(): Int
    override fun onBackStackChanged() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (fragment is WeekDayFragment) {
            ExitAlert("Press Exit to close the Application", "Garbage Collection")
            return
        }
        if (fragment is MapContainerFragment) {
            supportFragmentManager.popBackStackImmediate()
            return
        }
        if (fragment is CustomerListFragment) {
            ExitAlert2("Do you want to move out?", "Garbage Collection")
            return
        }
        val localFragmentManager = supportFragmentManager
        val i = localFragmentManager.backStackEntryCount
        if (i == 1 || i == 0) {
            finish()
        } else {
            mHandler!!.postDelayed({ localFragmentManager.popBackStack() }, 100)
        }
    }

    override fun onBackPressed() {
        onBackStackChanged()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val view = currentFocus
        if (view != null && (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_MOVE) && view is EditText && !view.javaClass.name.startsWith(
                "android.webkit."
            )
        ) {
            val scrcoords = IntArray(2)
            view.getLocationOnScreen(scrcoords)
            val x = ev.rawX + view.getLeft() - scrcoords[0]
            val y = ev.rawY + view.getTop() - scrcoords[1]
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom()) (Objects.requireNonNull(
                this.getSystemService(
                    INPUT_METHOD_SERVICE
                )
            ) as InputMethodManager).hideSoftInputFromWindow(
                this.window.decorView.applicationWindowToken, 0
            )
        }
        return super.dispatchTouchEvent(ev)
    }

    var booleanMutableLiveData: MutableLiveData<Boolean>? = null
    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}