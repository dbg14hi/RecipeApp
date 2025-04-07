package hbv601g.Recipe.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import hbv601g.Recipe.entities.NotificationModel

/**
 * A view model for the notifications.
 *
 */
class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableLiveData<List<NotificationModel>>()
    val notifications: LiveData<List<NotificationModel>> get() = _notifications

    init {
        fetchNotificationsFromFirestore()
    }

    fun refreshNotifications() {
        fetchNotificationsFromFirestore()
    }

    private fun fetchNotificationsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    val notificationList = mutableListOf<NotificationModel>()

                    for (doc in queryDocumentSnapshots) {
                        val recipeTitle = doc.getString("recipeTitle") ?: "Unknown Recipe"
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        notificationList.add(NotificationModel(recipeTitle, timestamp))
                    }
                    _notifications.value = notificationList
                }
                .addOnFailureListener { e ->
                    _notifications.value = emptyList()
                }
        }
    }
}
