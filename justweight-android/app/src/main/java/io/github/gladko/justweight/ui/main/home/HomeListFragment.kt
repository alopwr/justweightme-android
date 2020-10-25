package io.github.gladko.justweight.ui.main.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import io.github.gladko.justweight.R
import io.github.gladko.justweight.ui.btscan.BtScanAdapter
import io.github.gladko.justweight.ui.btscan.OnClickListener
import io.github.gladko.justweight.ui.main.MainActivity
import io.github.gladko.justweight.ui.main.MainViewModel
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.singleTop

class HomeListFragment : Fragment() {

    private lateinit var viewModel: ListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =
            ViewModelProviders.of(activity as FragmentActivity).get(ListViewModel::class.java)
        var adapter  = List1Adapter()

        viewModel.list.observe(this, Observer {
            it?.let {
                adapter.submitList(viewModel.list.value?.toList())
            }
        })

        viewModel.start()

        adapter.submitList(viewModel.list.value?.toList())
    }
}
