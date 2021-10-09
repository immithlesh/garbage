package com.garbagecollection.base

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.garbagecollection.network.ApiService
import io.reactivex.disposables.CompositeDisposable
/**
 * Created by Mithilesh Kumar on 26/08/2021.
 */
abstract class BaseFragment<T : ViewDataBinding?> : Fragment() {
    private var mActivity = null
    var viewDataBinding: T? = null
    private var mRootView: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (viewDataBinding == null) {
            viewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
            mRootView = viewDataBinding!!.getRoot()
        }
        return mRootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity<*>) {
            val activity = context
            activity.onFragmentAttached()
        }
    }

    val baseActivity
        get() = activity as BaseActivity<*>?

    override fun onDetach() {
        mActivity = null
        super.onDetach()
    }

    interface Callback {
        fun onFragmentAttached()
        fun onFragmentDetached(tag: String?)
    }

    open fun getApiService(): ApiService? {
        return baseActivity!!.getApiService()
    }
    fun showLoading() {
        baseActivity!!.showLoading()
    }

    fun hideLoading() {
       try {
           baseActivity?.hideLoading()
       }catch (e:Exception){
           e.printStackTrace()
       }
    }

    val disposable: CompositeDisposable
        get() = baseActivity!!.disposable

    fun displayIt(mFragment: Fragment?, tag: String?, isBack: Boolean,allowAnim:Boolean) {
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            if (baseActivity == null) {
                return@Runnable
            }
            baseActivity!!.displayIt(mFragment, tag, isBack,allowAnim)
        }, 20)
    }

    fun setArguments(mFragment: Fragment?, mBundle: Bundle?): Fragment {
        return baseActivity!!.setArguments(mFragment!!, mBundle)
    }
    /**
     * @return layout resource id
     */
    @LayoutRes
    abstract fun getLayoutId(): Int

    fun showToast(msg: String?) {
        try {
            Toast.makeText(baseActivity, msg, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}