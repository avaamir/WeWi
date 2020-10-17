package mp.amir.ir.wewi.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import mp.amir.ir.wewi.R
import mp.amir.ir.wewi.databinding.ActivityLoginBinding
import mp.amir.ir.wewi.respository.RemoteRepo
import mp.amir.ir.wewi.utils.general.snack
import mp.amir.ir.wewi.utils.general.toast
import mp.amir.ir.wewi.utils.wewi.Constants
import mp.amir.ir.wewi.viewmodels.LoginActivityViewModel
import java.lang.invoke.ConstantCallSite

class LoginActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityLoginBinding
    private lateinit var viewModel: LoginActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this).get(LoginActivityViewModel::class.java)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)



        initViews()
        subscribeObservers()


        RemoteRepo.setContext(this)
    }


    private fun initViews() {
        mBinding.btnLogin.setOnClickListener {
            val username = mBinding.etUsername.text.trim().toString()
            val password = mBinding.etPassword.text.trim().toString()

            when {
                username.isEmpty() -> toast("نام کاربری خود را وارد کنید")
                password.isEmpty() -> toast("گذرواژه خود را وارد کنید")
                else -> {
                    viewModel.login(username, password)
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }


        }
    }

    private fun subscribeObservers() {
        viewModel.loginResponse.observe(this, Observer {
            if (it != null) {
                if (it.isSucceed) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {

                }
            } else {
                snack(Constants.SERVER_ERROR) {
                    viewModel.loginAgain()
                }
            }
        })
    }
}