package com.mnmyounus.ymr.ui.auth
import android.os.Bundle; import android.view.*; import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment; import androidx.navigation.fragment.findNavController
import com.mnmyounus.ymr.R
import com.mnmyounus.ymr.databinding.FragmentLockBinding
import com.mnmyounus.ymr.util.PrefsUtil; import com.mnmyounus.ymr.viewmodel.MainViewModel

class LockFragment : Fragment() {
    private var _b: FragmentLockBinding? = null; private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View { _b=FragmentLockBinding.inflate(i,c,false); return b.root }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s); val ctx=requireContext()
        if(!PrefsUtil.isSetup(ctx)){ findNavController().navigate(R.id.to_setup); return }
        if(MainViewModel.sessionPw!=null){ findNavController().navigate(R.id.to_home); return }
        b.tvError.visibility=View.GONE
        val unlock = {
            val pw=b.etPassword.text?.toString()?.trim()?:""
            b.tvError.visibility=View.GONE
            when {
                pw.isEmpty()->{b.tvError.text="Enter password";b.tvError.visibility=View.VISIBLE}
                PrefsUtil.verify(ctx,pw)->{
                    MainViewModel.sessionPw=pw; PrefsUtil.setPassword(ctx,pw)
                    findNavController().navigate(R.id.to_home)
                }
                else->{b.tvError.text=getString(R.string.wrong_pw);b.tvError.visibility=View.VISIBLE;b.etPassword.text?.clear()}
            }
        }
        b.btnUnlock.setOnClickListener{unlock()}
        b.etPassword.setOnEditorActionListener{_,a,_->if(a==EditorInfo.IME_ACTION_DONE){unlock();true}else false}
    }
    override fun onDestroyView() { super.onDestroyView(); _b=null }
}
