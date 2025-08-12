package model


import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Farmer(
    var id: String = "", // Firestore document ID
    val userId: String = "", // KDF-1001, etc.
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    var photoBase64: String? = null, // Photo stored as a Base64 string
    var qrCodeUrl: String? = null,
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Date? = null
) : Parcelable