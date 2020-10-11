package mp.amir.ir.wewi.ui.activities

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import mp.amir.ir.wewi.R
import mp.amir.ir.wewi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*if (!UserConfigs.isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }*/
        setContentView(R.layout.activity_main)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        /*initViews()
        subscribeObservers()*/
        for (x in 1..9) {
            when (x) {
                1 -> mBinding.linearLayout
                2 -> mBinding.linearLayout1
                3 -> mBinding.linearLayout2
                4 -> mBinding.linearLayout3
                5 -> mBinding.linearLayout4
                6 -> mBinding.linearLayout5
                7 -> mBinding.linearLayout6
                8 -> mBinding.linearLayout7
                else -> mBinding.linearLayout8
            }.apply {
                scaleX = 0f
                scaleY = 0f
                visibility = View.VISIBLE
            }

        }

        Handler().postDelayed({
            appOpenedAnimation(
                views = listOf(
                    mBinding.linearLayout,
                    mBinding.linearLayout1,
                    mBinding.linearLayout2,
                    mBinding.linearLayout3,
                    mBinding.linearLayout4,
                    mBinding.linearLayout5,
                    mBinding.linearLayout7,
                ),
                index = 0
            )
        }, 800)
    }

    private fun appOpenedAnimation(views: List<View>, index: Int) {
        var isFirst = true
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300L
            addUpdateListener {
                views[index].apply {
                    scaleX = it.animatedFraction
                    scaleY = it.animatedFraction
                    visibility = View.VISIBLE
                }
                if (it.animatedFraction > 0.5f && isFirst) {
                    isFirst = false
                    if (index < views.size - 1) {
                        appOpenedAnimation(views, index + 1)
                    }
                }
            }
        }.start()

    }
}