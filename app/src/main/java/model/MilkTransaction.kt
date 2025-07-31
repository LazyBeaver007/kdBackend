package model



import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class MilkTransaction(
    var id: String = "",
    val farmerId: String = "",
    val quantity: Double = 0.0,
    val session: String = "Morning", // "Morning" or "Evening"
    @ServerTimestamp val date: Date? = null
) : Parcelable


