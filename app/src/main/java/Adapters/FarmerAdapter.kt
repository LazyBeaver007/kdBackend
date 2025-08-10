package Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kisaan_dairy.R
import model.Farmer
import com.example.kisaan_dairy.databinding.ItemFarmerBinding

class FarmerAdapter(
    private var farmers: List<Farmer>,
    private val onFarmerClicked: (Farmer) -> Unit
) : RecyclerView.Adapter<FarmerAdapter.FarmerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmerViewHolder {
        val binding = ItemFarmerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class FarmerViewHolder(private val binding: ItemFarmerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(farmer: Farmer) {
            binding.textViewFarmerName.text = farmer.name
            binding.textViewFarmerId.text = farmer.userId

            if (farmer.isActive) {
                binding.textViewFarmerStatus.text = "Active"
                //binding.textViewFarmerStatus.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_active)
            } else {
                binding.textViewFarmerStatus.text = "Inactive"
                //binding.textViewFarmerStatus.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_inactive)
            }

            itemView.setOnClickListener {
                onFarmerClicked(farmer)
            }
        }
    }
}
