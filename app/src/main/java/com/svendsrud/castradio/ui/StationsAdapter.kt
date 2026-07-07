package com.svendsrud.castradio.ui

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.svendsrud.castradio.R
import com.svendsrud.castradio.model.RadioStation

class StationsAdapter(
    private val stations: List<RadioStation>,
    private val onStationClick: (RadioStation) -> Unit
) : RecyclerView.Adapter<StationsAdapter.StationViewHolder>() {

    private var activeStation: RadioStation? = null
    private var isBuffering: Boolean = false

    fun setActiveStation(station: RadioStation?, buffering: Boolean = false) {
        val previousIndex = stations.indexOf(activeStation)
        activeStation = station
        isBuffering = buffering
        val newIndex = stations.indexOf(station)
        if (previousIndex != -1) notifyItemChanged(previousIndex)
        if (newIndex != -1 && newIndex != previousIndex) notifyItemChanged(newIndex)
    }

    fun setBuffering(buffering: Boolean) {
        isBuffering = buffering
        val index = stations.indexOf(activeStation)
        if (index != -1) notifyItemChanged(index)
    }

    class StationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView as MaterialCardView
        val logo: ImageView = itemView.findViewById(R.id.station_logo)
        val nameText: TextView = itemView.findViewById(R.id.station_name)
        val statusText: TextView = itemView.findViewById(R.id.station_status)
        val liveDot: View = itemView.findViewById(R.id.station_live_dot)
        val bufferSpinner: ProgressBar = itemView.findViewById(R.id.station_buffer_spinner)
        val castIcon: ImageView = itemView.findViewById(R.id.cast_icon)
        var pulseAnimator: ObjectAnimator? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_station, parent, false)
        return StationViewHolder(view)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val station = stations[position]
        val isActive = station == activeStation
        val accent = Color.parseColor(station.accentColor)

        holder.nameText.text = station.name
        holder.logo.setImageResource(station.logoRes)

        holder.pulseAnimator?.cancel()
        val context = holder.itemView.context
        val statusLabel: String
        when {
            isActive && isBuffering -> {
                statusLabel = context.getString(R.string.buffering_label)
                holder.statusText.text = statusLabel
                holder.statusText.setTextColor(accent)
                holder.castIcon.setImageResource(R.drawable.ic_cast_connected)
                holder.castIcon.setColorFilter(accent)
                holder.liveDot.visibility = View.INVISIBLE
                holder.bufferSpinner.visibility = View.VISIBLE
                holder.bufferSpinner.indeterminateTintList = android.content.res.ColorStateList.valueOf(accent)
            }
            isActive -> {
                statusLabel = context.getString(R.string.casting_label)
                holder.statusText.text = statusLabel
                holder.statusText.setTextColor(accent)
                holder.castIcon.setImageResource(R.drawable.ic_cast_connected)
                holder.castIcon.setColorFilter(accent)
                holder.bufferSpinner.visibility = View.GONE
                holder.liveDot.visibility = View.VISIBLE
                holder.liveDot.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(accent)
                }
                if (isReducedMotionEnabled(context)) {
                    holder.liveDot.alpha = 1f
                } else {
                    holder.pulseAnimator = ObjectAnimator.ofFloat(holder.liveDot, View.ALPHA, 1f, 0.25f).apply {
                        duration = 700
                        repeatMode = ObjectAnimator.REVERSE
                        repeatCount = ObjectAnimator.INFINITE
                        interpolator = LinearInterpolator()
                        start()
                    }
                }
            }
            else -> {
                statusLabel = context.getString(R.string.live_label)
                holder.statusText.text = statusLabel
                holder.statusText.setTextColor(context.getColor(R.color.colorTextSecondary))
                holder.castIcon.setImageResource(R.drawable.ic_cast)
                holder.castIcon.setColorFilter(context.getColor(R.color.colorTextSecondary))
                holder.bufferSpinner.visibility = View.GONE
                holder.liveDot.visibility = View.INVISIBLE
                holder.liveDot.alpha = 1f
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            holder.card.outlineAmbientShadowColor = accent
            holder.card.outlineSpotShadowColor = accent
        }

        holder.itemView.contentDescription = "${station.name}, $statusLabel"
        holder.itemView.setOnClickListener { onStationClick(station) }
    }

    override fun onViewRecycled(holder: StationViewHolder) {
        holder.pulseAnimator?.cancel()
        holder.pulseAnimator = null
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = stations.size

    private fun isReducedMotionEnabled(context: Context): Boolean {
        val scale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        return scale == 0f
    }
}
