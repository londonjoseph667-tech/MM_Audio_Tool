package com.mm.audiotool.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mm.audiotool.databinding.ItemAudioBinding
import com.mm.audiotool.model.AudioItem

/**
 * RecyclerView adapter for the Audio Editor screen.
 * Displays up to 6 [AudioItem] rows per page.
 *
 * Each row contains:
 *  - Play/Pause toggle (circle icon button)
 *  - File name label
 *  - Replace button  → triggers [onReplace]
 *  - Save button     → triggers [onSave] (enabled only after a replacement is set)
 */
class AudioAdapter(
    private val onPlayPause : (AudioItem) -> Unit,
    private val onReplace   : (AudioItem) -> Unit,
    private val onSave      : (AudioItem) -> Unit
) : ListAdapter<AudioItem, AudioAdapter.AudioViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = ItemAudioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AudioViewHolder(
        private val binding: ItemAudioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AudioItem) {
            binding.apply {
                // File name
                tvAudioName.text = item.originalName

                // Replaced indicator
                tvReplacedName.text = item.replacedFile?.name ?: ""
                tvReplacedName.visibility =
                    if (item.replacedFile != null) android.view.View.VISIBLE
                    else android.view.View.GONE

                // Play / Pause icon
                val playIcon = if (item.isPlaying)
                    com.google.android.material.R.drawable.ic_m3_chip_close
                else
                    android.R.drawable.ic_media_play

                btnPlayPause.setImageResource(playIcon)
                btnPlayPause.contentDescription =
                    if (item.isPlaying) "Pause" else "Play"
                btnPlayPause.setOnClickListener { onPlayPause(item) }

                // Replace button
                btnReplace.setOnClickListener { onReplace(item) }

                // Save button — only active when a replacement has been chosen
                btnSave.isEnabled = item.replacedFile != null
                btnSave.alpha     = if (item.replacedFile != null) 1f else 0.4f
                btnSave.setOnClickListener { onSave(item) }

                // Visual feedback when saved
                root.alpha = if (item.isSaved) 0.7f else 1f
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AudioItem>() {
            override fun areItemsTheSame(old: AudioItem, new: AudioItem) =
                old.id == new.id

            override fun areContentsTheSame(old: AudioItem, new: AudioItem) =
                old == new
        }
    }
}
