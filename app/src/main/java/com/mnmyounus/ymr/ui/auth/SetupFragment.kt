package com.mnmyounus.ymr.ui.auth
import android.os.Bundle; import android.view.*
import androidx.fragment.app.Fragment; import androidx.navigation.fragment.findNavController
import com.mnmyounus.ymr.R
import com.mnmyounus.ymr.databinding.FragmentSetupBinding
import com.mnmyounus.ymr.util.PrefsUtil; import com.mnmyounus.ymr.viewmodel.MainViewModel

class SetupFragment : Fragment() {
    private var _b: FragmentSetupBinding? = null; private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View { _b=FragmentSetupBinding.inflate(i,c,false); return b.root }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s)
        b.btnSet.setOnClickListener {
            val pw=b.etPassword.text?.toString()?:""; val c2=b.etConfirm.text?.toString()?:""
            b.tvError.visibility=View.GONE
            when {
                pw.isEmpty()||pw.length>5->{b.tvError.text=getString(R.string.pw_short);b.tvError.visibility=View.VISIBLE}
                pw!=c2->{b.tvError.text=getString(R.string.pw_mismatch);b.tvError.visibility=View.VISIBLE}
                else->{PrefsUtil.setPassword(requireContext(),pw);MainViewModel.sessionPw=pw;findNavController().navigate(R.id.setup_to_home)}
            }
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b=null }
}
