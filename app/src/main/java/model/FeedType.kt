package model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class FeedType(
    var id: String = "", // Firestore document ID
    val name: String = "",
    val unit: String = "Bag", // e.g., "Bag", "Kg"
    val price: Double = 0.0,
    val isActive: Boolean = true
) : Parcelable
