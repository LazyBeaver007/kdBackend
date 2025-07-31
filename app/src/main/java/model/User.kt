package model



import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class User(
    val uid: String = "", // Firebase Auth UID
    val email: String = "",
    val role: String = "staff" // "admin" or "staff"
) : Parcelable