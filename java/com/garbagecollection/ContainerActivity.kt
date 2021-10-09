package com.garbagecollection

import android.app.Activity
import android.os.Bundle
import com.garbagecollection.base.BaseActivity
import com.garbagecollection.databinding.ActivityContainerBinding
import com.garbagecollection.viewUI.start.StartFragment
/**
 * Created by Mithilesh Kumar on 26/08/2021.
 */
class ContainerActivity : BaseActivity<ActivityContainerBinding>() {

    override fun setContainerLayout(): Int {
        return R.id.fragmentContainer
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_container
    }

    override fun getCurrentActivity(): Activity {
        return this@ContainerActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayIt(StartFragment(), StartFragment::class.java.canonicalName, true, false)
    }
}