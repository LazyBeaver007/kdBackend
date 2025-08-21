package Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import model.User
import com.example.kisaan_dairy.databinding.ItemStaffBinding

class StaffAdapter(private var staffList: List<User>) :
    RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = ItemStaffBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StaffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(staffList[position])
    }

    override fun getItemCount(): Int = staffList.size

    fun updateData(newStaffList: List<User>) {
        this.staffList = newStaffList
        notifyDataSetChanged()
    }

    inner class StaffViewHolder(private val binding: ItemStaffBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.textViewStaffEmail.text = user.email
        }
    }
}
