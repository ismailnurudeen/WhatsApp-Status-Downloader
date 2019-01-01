package xyz.ismailnurudeen.whatsappstatusdownloader.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_status_item.view.*
import org.apache.commons.io.FileUtils
import xyz.ismailnurudeen.whatsappstatusdownloader.R
import xyz.ismailnurudeen.whatsappstatusdownloader.utils.AppUtil
import java.io.File

class StatusAdapter(private val context: Context, private val statusList: MutableCollection<File>, private val onItemClick: OnItemClickListener, val onItemLongClick: OnItemLongClickListener, val showDownloadIcon: Boolean = true) : RecyclerView.Adapter<StatusAdapter.StatusHolder>() {

    override fun onBindViewHolder(holder: StatusHolder, position: Int) {
        holder.bind(statusList.elementAt(position))
    }

    override fun getItemCount(): Int = statusList.size
    override fun getItemViewType(position: Int): Int {
        return if (statusList.isEmpty()) {
            R.layout.layout_status_empty
        } else {
            R.layout.layout_status_item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusHolder {
        val view = LayoutInflater.from(context).inflate(viewType, parent, false)
        return StatusHolder(view, onItemClick, onItemLongClick)
    }

    inner class StatusHolder(itemView: View, private val itemClick: OnItemClickListener, private val itemLongClick: OnItemLongClickListener) : RecyclerView.ViewHolder(itemView) {
        fun bind(status: File) = with(itemView) {
            Glide.with(context)
                    .load(status)
                    .into(itemView.status_image)
            if (!showDownloadIcon) {
                itemView.status_download_btn.setImageResource(R.drawable.ic_delete_black_24dp)
                itemView.status_download_btn.setOnClickListener {
                    itemClick.onItemClick(it, adapterPosition, OnItemClickListener.Companion.ITEM_CLICKED_TYPE.DELETE_BUTTON)
                }
                itemView.status_time_left.text = status.name
            } else {
                //Set download icon to green if file has already been downloaded
                for (savedFile in AppUtil(context).savedStatuses) {
                    if (FileUtils.contentEquals(status, savedFile)) {
                        itemView.status_download_btn.setColorFilter(context.resources.getColor(R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN)
                    }
                }
                itemView.status_time_left.text = AppUtil(context).getStatusTimeLeft(status)
                itemView.status_download_btn.setOnClickListener {
                    itemClick.onItemClick(it, adapterPosition, OnItemClickListener.Companion.ITEM_CLICKED_TYPE.DOWNLOAD_BUTTON)
                }
            }

            itemView.status_image.setOnClickListener {
                itemClick.onItemClick(it, adapterPosition, OnItemClickListener.Companion.ITEM_CLICKED_TYPE.STATUS_IMAGE)
            }

            setOnClickListener {
                itemClick.onItemClick(it, adapterPosition, OnItemClickListener.Companion.ITEM_CLICKED_TYPE.ALL)
            }
            setOnLongClickListener {
                itemLongClick.onItemLongClick(it, adapterPosition)
                true
            }
        }
    }

    interface OnItemClickListener {
        companion object {
            enum class ITEM_CLICKED_TYPE { STATUS_IMAGE, DOWNLOAD_BUTTON, DELETE_BUTTON, ALL }
        }

        fun onItemClick(v: View, pos: Int, which: ITEM_CLICKED_TYPE)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(view: View?, pos: Int)
    }
}