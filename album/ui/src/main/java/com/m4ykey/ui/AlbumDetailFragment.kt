package com.m4ykey.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.m4ykey.album.ui.R
import com.m4ykey.album.ui.databinding.FragmentAlbumDetailBinding
import com.m4ykey.core.views.BaseFragment
import com.m4ykey.core.views.buttonAnimation
import com.m4ykey.core.views.buttonsIntents
import com.m4ykey.core.views.loadImage
import com.m4ykey.core.views.utils.copyText
import com.m4ykey.core.views.utils.formatAirDate
import com.m4ykey.core.views.utils.getColorFromImage
import com.m4ykey.core.views.utils.showToast
import com.m4ykey.data.domain.model.album.AlbumDetail
import com.m4ykey.data.local.model.AlbumEntity
import com.m4ykey.data.local.model.ArtistEntity
import com.m4ykey.data.local.model.IsAlbumSaved
import com.m4ykey.data.local.model.IsListenLaterSaved
import com.m4ykey.data.local.model.relations.AlbumWithStates
import com.m4ykey.ui.adapter.TrackAdapter
import com.m4ykey.ui.colors.ColorViewModel
import com.m4ykey.ui.helpers.animateColorTransition
import com.m4ykey.ui.helpers.getArtistList
import com.m4ykey.ui.helpers.getLargestImageUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class AlbumDetailFragment : BaseFragment<FragmentAlbumDetailBinding>(
    FragmentAlbumDetailBinding::inflate
) {

    private val args by navArgs<AlbumDetailFragmentArgs>()
    private val viewModel by viewModels<AlbumViewModel>()
    private val colorViewModel by viewModels<ColorViewModel>()
    private val trackAdapter by lazy { createTrackAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNavigationVisibility?.hideBottomNavigation()

        setupRecyclerView()
        binding.apply {
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
            toolbarLoading.setNavigationOnClickListener { findNavController().navigateUp() }
        }

        lifecycleScope.launch {
            observeViewModel()
        }
    }

    private fun createTrackAdapter(): TrackAdapter {
        return TrackAdapter(
            onTrackClick = { track ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(track.externalUrls.spotify)))
            }
        )
    }

    private fun observeViewModel() {
        viewModel.getAlbumDetails(args.albumId)

        lifecycleScope.launch {
            viewModel.detail.collect { item -> item?.let { displayAlbumDetail(it) } }
        }

        lifecycleScope.launch {
            viewModel.getAlbum(args.albumId)?.let { item -> displayAlbumFromDatabase(item) }
        }

        lifecycleScope.launch {
            delay(500L)
            viewModel.tracks.collect { trackAdapter.submitList(it) }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect {
                binding.layoutLoading.isVisible = it
                binding.nestedScrollView.isVisible = !it
            }
        }

        lifecycleScope.launch {
            viewModel.isLoadingTracks.collect {
                binding.progressBarTracks.isVisible = it
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { errorMessage ->
                errorMessage?.let {
                    showToast(requireContext(), it)
                }
            }
        }
    }

    private fun displayAlbumFromDatabase(album: AlbumEntity) {
        binding.apply {
            with(album) {
                lifecycleScope.launch {
                    val albumWithStates = viewModel.getAlbumWithStates(id)
                    if (albumWithStates != null) {
                        updateIcons(albumWithStates)
                    }
                }

                loadImage(imgAlbum, images, requireContext())
                if (colorViewModel.selectedColor.value == null) {
                    getColorFromImage(images, context = requireContext()) { color ->
                        colorViewModel.setColor(color)
                        animateColorTransition(Color.parseColor("#4FC3F7"), color, btnAlbum, btnArtist)
                    }
                } else {
                    colorViewModel.selectedColor.value?.let { color ->
                        btnAlbum.setBackgroundColor(color)
                        btnArtist.setBackgroundColor(color)
                    }
                }

                cardView.setOnClickListener {
                    val action = AlbumDetailFragmentDirections.actionAlbumDetailFragmentToAlbumCoverFragment(images)
                    findNavController().navigate(action)
                }

                buttonsIntents(button = btnArtist, url = artists[0].url, requireContext())
                buttonsIntents(button = btnAlbum, url = albumUrl, requireContext())

                txtAlbumName.apply {
                    text = name
                    setOnClickListener { copyText(name, requireContext(), getString(com.m4ykey.core.R.string.copied_to_clipboard)) }
                }
                txtArtist.text = getArtistList()
                txtInfo.text = getString(
                    R.string.album_info,
                    albumType,
                    releaseDate,
                    totalTracks,
                    getString(R.string.tracks)
                )

                imgSave.setOnClickListener {
                    lifecycleScope.launch {
                        val isAlbumSaved = viewModel.getSavedAlbumState(id)
                        val isListenLaterSaved = viewModel.getListenLaterState(id)
                        if (isListenLaterSaved?.isListenLaterSaved == true) {
                            viewModel.deleteListenLaterState(id)
                            if (isAlbumSaved == null) {
                                viewModel.deleteAlbum(id)
                            }
                            imgListenLater.buttonAnimation(R.drawable.ic_listen_later_border)
                        }
                        if (isAlbumSaved != null) {
                            viewModel.deleteSavedAlbumState(id)
                            if (isListenLaterSaved == null) {
                                viewModel.deleteAlbum(id)
                            }
                            imgSave.buttonAnimation(R.drawable.ic_favorite_border)
                        } else {
                            viewModel.insertAlbum(album)
                            viewModel.insertSavedAlbum(IsAlbumSaved(id, true))
                            imgSave.buttonAnimation(R.drawable.ic_favorite)
                        }
                    }
                }

                imgListenLater.setOnClickListener {
                    lifecycleScope.launch {
                        val isListenLaterSaved = viewModel.getListenLaterState(id)
                        if (isListenLaterSaved != null) {
                            viewModel.deleteListenLaterState(id)
                            val isAlbumSaved = viewModel.getSavedAlbumState(id)
                            if (isAlbumSaved == null) {
                                viewModel.deleteAlbum(id)
                            }
                            imgListenLater.buttonAnimation(R.drawable.ic_listen_later_border)
                        } else {
                            viewModel.insertAlbum(album)
                            viewModel.insertListenLaterAlbum(IsListenLaterSaved(id, true))
                            imgListenLater.buttonAnimation(R.drawable.ic_listen_later)
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvTrackList.apply {
            adapter = trackAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (lastVisibleItemPosition == totalItemCount - 1) {
                        if (!viewModel.isPaginationEnded && !viewModel.isLoadingTracks.value) {
                            lifecycleScope.launch { viewModel.getAlbumTracks(args.albumId) }
                        }
                    }
                }
            })

            lifecycleScope.launch {
                delay(500L)
                viewModel.totalTracksDuration.collect { totalDuration ->
                    val hours = (totalDuration / 1000) / 3600
                    val minutes = ((totalDuration / 1000) % 3600) / 60
                    val seconds = (totalDuration / 1000) % 60

                    val formattedDuration = when {
                        hours > 0 -> String.format(Locale.getDefault(), "%d ${getString(R.string.hour)} %d min", hours, minutes)
                        minutes > 0 -> String.format(Locale.getDefault(), "%d min %d ${getString(R.string.sec)}", minutes, seconds)
                        else -> String.format(Locale.getDefault(), "%d ${getString(R.string.sec)}", seconds)
                    }
                    binding.txtTotalDuration.text = getString(R.string.duration, formattedDuration)
                }
            }
        }
    }

    private fun displayAlbumDetail(item: AlbumDetail) {
        binding.apply {
            val albumType = when {
                item.totalTracks in 2..6 && item.albumType.equals("Single", ignoreCase = true) -> "EP"
                else -> item.albumType.replaceFirstChar { it.uppercase() }
            }
            val formatAirDate = formatAirDate(item.releaseDate)
            val albumInfo = "$albumType • $formatAirDate • ${item.totalTracks} " + getString(
                R.string.tracks
            )
            val albumUrl = item.externalUrls.spotify
            val artistUrl = item.artists[0].externalUrls.spotify
            val copyrights = item.copyrights
                .map { it.text }
                .distinct()
                .joinToString(separator = "\n")

            txtAlbumName.apply {
                text = item.name
                setOnClickListener { copyText(item.name, requireContext(), getString(com.m4ykey.core.R.string.copied_to_clipboard)) }
            }
            txtArtist.text = item.getArtistList()
            txtInfo.text = albumInfo

            cardView.setOnClickListener {
                val action =
                    AlbumDetailFragmentDirections.actionAlbumDetailFragmentToAlbumCoverFragment(
                        item.getLargestImageUrl().toString()
                    )
                findNavController().navigate(action)
            }

            txtCopyrights.text = copyrights

            loadImage(imgAlbum, item.getLargestImageUrl().toString(), requireContext())

            if (colorViewModel.selectedColor.value == null) {
                getColorFromImage(item.getLargestImageUrl().toString(), context = requireContext()) { color ->
                    colorViewModel.setColor(color)
                    animateColorTransition(Color.parseColor("#4FC3F7"), color, btnAlbum, btnArtist)
                }
            } else {
                colorViewModel.selectedColor.value?.let { color ->
                    btnAlbum.setBackgroundColor(color)
                    btnArtist.setBackgroundColor(color)
                }
            }

            buttonsIntents(button = btnAlbum, url = albumUrl ?: "", requireContext())
            btnArtist.setOnClickListener {
                val artistId = item.artists[0].id
                artistId.let {
                    val intent = Intent(requireContext(), ArtistActivity::class.java)
                    intent.putExtra("artistId", it)
                    startActivity(intent)
                }
            }

            val artistsEntity = item.artists.map { artist ->
                ArtistEntity(
                    name = artist.name,
                    artistId = artist.id,
                    url = artistUrl.orEmpty(),
                    albumId = args.albumId
                )
            }

            val album = AlbumEntity(
                id = item.id,
                name = item.name,
                releaseDate = formatAirDate.toString(),
                totalTracks = item.totalTracks,
                images = item.getLargestImageUrl().toString(),
                albumType = albumType,
                artists = artistsEntity,
                albumUrl = albumUrl ?: ""
            )

            lifecycleScope.launch {
                val albumWithStates = viewModel.getAlbumWithStates(item.id)
                if (albumWithStates != null) {
                    updateIcons(albumWithStates)
                }
            }

            imgSave.setOnClickListener {
                lifecycleScope.launch {
                    val isAlbumSaved = viewModel.getSavedAlbumState(item.id)
                    val isListenLaterSaved = viewModel.getListenLaterState(item.id)

                    if (isListenLaterSaved?.isListenLaterSaved == true) {
                        viewModel.deleteListenLaterState(item.id)
                        if (isAlbumSaved == null) {
                            viewModel.deleteAlbum(item.id)
                        }
                        imgListenLater.buttonAnimation(R.drawable.ic_listen_later_border)
                    }

                    if (isAlbumSaved != null) {
                        viewModel.deleteSavedAlbumState(item.id)
                        if (isListenLaterSaved == null) {
                            viewModel.deleteAlbum(item.id)
                        }
                        imgSave.buttonAnimation(R.drawable.ic_favorite_border)
                    } else {
                        viewModel.insertAlbum(album)
                        viewModel.insertSavedAlbum(IsAlbumSaved(item.id, true))
                        imgSave.buttonAnimation(R.drawable.ic_favorite)
                    }
                }
            }

            imgListenLater.setOnClickListener {
                lifecycleScope.launch {
                    val isListenLaterSaved = viewModel.getListenLaterState(item.id)
                    if (isListenLaterSaved != null) {
                        viewModel.deleteListenLaterState(item.id)
                        val isAlbumSaved = viewModel.getSavedAlbumState(item.id)
                        if (isAlbumSaved == null) {
                            viewModel.deleteAlbum(item.id)
                        }
                        imgListenLater.buttonAnimation(R.drawable.ic_listen_later_border)
                    } else {
                        viewModel.insertAlbum(album)
                        viewModel.insertListenLaterAlbum(IsListenLaterSaved(item.id, true))
                        imgListenLater.buttonAnimation(R.drawable.ic_listen_later)
                    }
                }
            }
        }
    }

    private fun updateIcons(albumWithStates: AlbumWithStates) {
        val isAlbumSaved = albumWithStates.isAlbumSaved?.isAlbumSaved
        val isListenLaterSaved = albumWithStates.isListenLaterSaved?.isListenLaterSaved

        binding.apply {
            imgSave.setImageResource(
                if (isAlbumSaved == true) R.drawable.ic_favorite
                else R.drawable.ic_favorite_border
            )

            imgListenLater.setImageResource(
                if (isListenLaterSaved == true) R.drawable.ic_listen_later
                else R.drawable.ic_listen_later_border
            )
        }
    }
}