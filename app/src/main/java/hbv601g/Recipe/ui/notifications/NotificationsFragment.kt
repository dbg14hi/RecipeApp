package hbv601g.Recipe.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import hbv601g.Recipe.databinding.FragmentNotificationsBinding
import hbv601g.Recipe.entities.NotificationModel
import hbv601g.Recipe.ui.notifications.NotificationAdapter  // Import the adapter

/**
 * A notification fragment for the view.
 *
 */
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationList: ArrayList<NotificationModel>
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this)[NotificationsViewModel::class.java]

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView: RecyclerView = binding.notificationsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationList = ArrayList()
        notificationAdapter = NotificationAdapter(notificationList)
        recyclerView.adapter = notificationAdapter

        notificationsViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            notificationList.clear()
            notificationList.addAll(notifications)
            notificationAdapter.notifyDataSetChanged()
        }
        return root
    }

    override fun onResume() {
        super.onResume()
        val notificationsViewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]
        notificationsViewModel.refreshNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
