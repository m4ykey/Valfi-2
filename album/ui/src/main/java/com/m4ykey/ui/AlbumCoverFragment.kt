package com.m4ykey.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.m4ykey.album.ui.databinding.FragmentAlbumCoverBinding
import com.m4ykey.core.views.BaseFragment
import com.m4ykey.core.views.utils.copyText
import com.m4ykey.ui.adapter.ColorAdapter
import com.m4ykey.ui.colors.ColorList
import com.m4ykey.ui.colors.extractColorsFromBitmap
import com.m4ykey.ui.colors.intToHex
import com.m4ykey.ui.colors.loadImageWithColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumCoverFragment : BaseFragment<FragmentAlbumCoverBinding>(
    FragmentAlbumCoverBinding::inflate
) {

    private val args by navArgs<AlbumCoverFragmentArgs>()
    private val colorAdapter by lazy { createColorAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNavigationVisibility?.hideBottomNavigation()

        binding.apply {
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
            loadImageWithColors(imgAlbum, args.imgUrl, requireContext()) { bitmap ->
                val colors = extractColorsFromBitmap(bitmap).takeIf { it.isNotEmpty() } ?: emptyList()
                val colorItems = colors.map { ColorList(color = it) }
                colorAdapter.submitList(colorItems)
            }

            recyclerViewColors.apply {
                adapter = colorAdapter
                layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
            }
        }
    }

    private fun createColorAdapter() : ColorAdapter {
        return ColorAdapter(
            onColorClick = { color ->
                val colorToHex = intToHex(color.color)
                copyText(colorToHex, requireContext(), colorToHex)
            }
        )
    }
}