package model


import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class FeedTransaction(
    var id: String = "",
    val farmerId: String = "",
    val feedTypeId: String = "",
    val quantity: Double = 0.0,
    val priceAtTransaction: Double = 0.0, // Important for historical accuracy
    @ServerTimestamp val date: Date? = null
) : Parcelable
