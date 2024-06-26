package com.m4ykey.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.m4ykey.core.Constants.ALBUM
import com.m4ykey.core.Constants.COMPILATION
import com.m4ykey.core.Constants.EP
import com.m4ykey.core.Constants.SINGLE
import com.m4ykey.core.views.BaseFragment
import com.m4ykey.ui.databinding.FragmentAlbumStatisticsBinding
import com.patrykandpatrick.vico.core.entry.entryModelOf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlbumStatisticsFragment : BaseFragment<FragmentAlbumStatisticsBinding>(
    FragmentAlbumStatisticsBinding::inflate
) {

    private val viewModel by viewModels<AlbumViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNavigationVisibility?.hideBottomNavigation()

        binding.apply {
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

            lifecycleScope.launch {
                val album = viewModel.getAlbumTypeCount(ALBUM).firstOrNull() ?: 0
                val single = viewModel.getAlbumTypeCount(SINGLE).firstOrNull() ?: 0
                val compilation = viewModel.getAlbumTypeCount(COMPILATION).firstOrNull() ?: 0
                val ep = viewModel.getAlbumTypeCount(EP).firstOrNull() ?: 0

                val albumCount = viewModel.getAlbumCount().firstOrNull() ?: 0
                val tracksCount = viewModel.getTotalTracksCount().firstOrNull() ?: 0

                binding.txtAlbumCount.text = albumCount.toString()
                binding.txtTotalSongsPlayed.text = tracksCount.toString()

                val chartEntryModel = entryModelOf(album, single, compilation, ep)
                chart.setModel(chartEntryModel)
            }
        }
    }
}