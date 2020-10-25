package io.github.gladko.justweight.ui.main.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.gladko.justweight.R
import kotlinx.android.synthetic.main.fragment_account.*
import org.jetbrains.annotations.NotNull

class AccountFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_account, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var simpleAdapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> WebViewFragment()
                    1 -> SettingsFragmnet()
                    else -> WebViewFragment()
                }
            }

            override fun getItemCount(): Int {
                return 2
            }
        }
        view_pager.adapter = simpleAdapter

        TabLayoutMediator(tabLayout, view_pager) { tab, position ->
            tab.text = when(position) {
                0 -> "Konto"
                1 -> "Ustawienia"
                else -> ""
            }
        }.attach()

    }
}
