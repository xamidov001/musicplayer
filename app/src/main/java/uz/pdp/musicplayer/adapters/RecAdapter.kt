package uz.pdp.musicplayer.adapters

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.pdp.musicplayer.R
import uz.pdp.musicplayer.classes.MusicClass
import uz.pdp.musicplayer.databinding.ItemBinding
import java.util.*

class RecAdapter(var context: Context, var list: List<MusicClass>, var listener: OnMyClickListener): RecyclerView.Adapter<RecAdapter.VH>() {

    inner class VH(var binding: ItemBinding): RecyclerView.ViewHolder(binding.root)

    interface OnMyClickListener {
        fun onClickItem(position: Int, image: ImageView, constraintLayout: ConstraintLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemBinding.inflate(LayoutInflater.from(parent.context), parent,false))
    }

    class MyDiffUtil: DiffUtil.ItemCallback<MusicClass>() {
        override fun areItemsTheSame(oldItem: MusicClass, newItem: MusicClass): Boolean {
            return oldItem.musicUri == newItem.musicUri
        }

        override fun areContentsTheSame(oldItem: MusicClass, newItem: MusicClass): Boolean {
            return oldItem == newItem
        }

    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.apply {



            if (list[position].isHide) {
                if (list[position].date == 0) {
                    play.setImageResource(R.drawable.ic_play)
                } else {
                    play.setImageResource(R.drawable.ic_pause)
                }
                layoutConstrain.setBackgroundColor(Color.parseColor("#009CFB"))
            } else {
                play.setImageResource(0)
                layoutConstrain.setBackgroundColor(Color.parseColor("#303030"))
            }
            author.text = list[position].author
            title.text = list[position].title
            val time = list[position].duration/1000
            val min = time/60
            val sec = time%60
            if (min<10) duration.text = "0$min:$sec"
            else duration.text = "$min:$sec"
            val uri = Uri.parse(list[position].imageUri)
            if (uri != null){
                Glide.with(context).load(uri).placeholder(R.drawable.stellio).into(image)
            }
            root.setOnClickListener {
                listener.onClickItem(position, play, layoutConstrain)
            }
        }
    }

    override fun getItemCount(): Int  = list.size

}