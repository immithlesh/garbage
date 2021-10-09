package com.garbagecollection.app

import android.content.Context

import androidx.databinding.ViewDataBinding
import com.garbagecollection.ContainerActivity
import com.garbagecollection.base.BaseFragment
/**
 * Created by Mithilesh Kumar on 26/08/2021.
 */

abstract class GCBaseFragment<T : ViewDataBinding?> : BaseFragment<T>(), FragmentView {

    private lateinit var mContainerActivity: ContainerActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContainerActivity = context as ContainerActivity
    }

    fun getContainerActivity(): ContainerActivity {
        return mContainerActivity
    }

}
