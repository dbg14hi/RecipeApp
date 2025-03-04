package hbv601g.Recipe.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import hbv601g.Recipe.R
import hbv601g.Recipe.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var createRecipeFab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        createRecipeFab = binding.createRecipeFab

        if (FirebaseAuth.getInstance().currentUser != null) {
            createRecipeFab.visibility = View.VISIBLE
            binding.createRecipeFab.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_home_to_createRecipeFragment)
            }
        } else {
            createRecipeFab.visibility = View.GONE
        }

        adapter = RecipeAdapter(emptyList())
        recyclerView.adapter = adapter

        homeViewModel.recipesLiveData.observe(viewLifecycleOwner) { recipes ->
            Log.d("HomeFragment", "Received ${recipes.size} recipes in Fragment")
            adapter.updateData(recipes ?: emptyList())
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}