package com.mnmyounus.ymr.ui.features
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.mnmyounus.ymr.R

class MediaFragment : Fragment() {

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_media_tabs, c, false)

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        val tabs = listOf("Photos" to "photos", "Videos" to "videos", "Audio" to "audio", "Docs" to "docs")
        val vp = v.findViewById<ViewPager2>(R.id.mediaViewPager)
        val tl = v.findViewById<TabLayout>(R.id.mediaTabLayout)

        vp.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = tabs.size
            override fun createFragment(pos: Int) = MediaListFragment.newInstance(tabs[pos].second)
        }
        vp.offscreenPageLimit = 1

        TabLayoutMediator(tl, vp) { tab, pos -> tab.text = tabs[pos].first }.attach()
    }
}
