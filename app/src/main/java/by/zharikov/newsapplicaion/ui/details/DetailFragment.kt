package by.zharikov.newsapplicaion.ui.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.databinding.FragmentDetailBinding


class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return mBinding.root
    }


}