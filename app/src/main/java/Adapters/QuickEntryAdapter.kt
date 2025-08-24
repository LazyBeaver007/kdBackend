package Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import model.Farmer
import com.example.kisaan_dairy.databinding.ItemQuickEntryFarmerBinding // Use a new binding
import android.util.Base64

// Create a new layout file 'item_quick_entry_farmer.xml' similar to 'item_farmer.xml'
// but maybe with a photo.

class QuickEntryAdapter(
    private var farmers: List<Farmer>,
    private val onFarmerSelected: (Farmer) -> Unit
) : RecyclerView.Adapter<QuickEntryAdapter.FarmerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmerViewHolder {
        val binding = ItemQuickEntryFarmerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FarmerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FarmerViewHolder, position: Int) {
        holder.bind(farmers[position])
    }

    override fun getItemCount(): Int = farmers.size

    fun updateData(newFarmers: List<Farmer>) {
        this.farmers = newFarmers
        notifyDataSetChanged()
    }

    inner class FarmerViewHolder(private val binding: ItemQuickEntryFarmerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(farmer: Farmer) {
            binding.textViewFarmerName.text = farmer.name
            binding.textViewFarmerId.text = farmer.userId

            // Load photo from Base64 string
            if (!farmer.photoBase64.isNullOrEmpty()) {
                try {
                    val decodedString = Base64.decode(farmer.photoBase64, Base64.DEFAULT)
                    Glide.with(itemView.context).asBitmap().load(decodedString).into(binding.imageViewFarmerPhoto)
                } catch (e: Exception) {
                    // Handle error or set placeholder
                }
            }

            itemView.setOnClickListener {
                onFarmerSelected(farmer)
            }
        }
    }
}