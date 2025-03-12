package hbv601g.Recipe.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hbv601g.Recipe.R
import hbv601g.Recipe.databinding.FragmentHomeBinding
import hbv601g.Recipe.entities.Recipe

class HomeFragment : Fragment(), RecipeAdapter.OnRecipeClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var createRecipeFab: FloatingActionButton
    private val db = FirebaseFirestore.getInstance()

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
            createRecipeFab.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_home_to_createRecipeFragment)
            }
        } else {
            createRecipeFab.visibility = View.GONE
        }

        // Initialize adapter and pass 'this' as the click listener
        adapter = RecipeAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        setupSortingDropdown()
        setupFilteringDropdown()

        // Observe recipe list changes
        homeViewModel.recipesLiveData.observe(viewLifecycleOwner, Observer { recipes ->
            adapter.updateData(recipes)
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupSortingDropdown() {
        val sortingOptions = arrayOf("Name", "Date Added")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortingOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.sortSpinner.adapter = adapter
        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSort = sortingOptions[position]
                homeViewModel.sortRecipes(selectedSort)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFilteringDropdown() {
        val filterOptions = arrayOf("All", "Breakfast", "Lunch", "Dinner")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.filterSpinner.adapter = adapter
        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFilter = filterOptions[position]
                homeViewModel.filterRecipes(selectedFilter)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onRecipeClick(recipe: Recipe) {
        val bundle = Bundle().apply {
            putString("recipeId", recipe.recipeId)
            putString("recipeTitle", recipe.title)
            putString("recipeDescription", recipe.description)
            putStringArrayList("recipeIngredients", ArrayList(recipe.ingredients))
            putInt("recipeCookingTime", recipe.cookingTime)
        }

        val navController = findNavController()

        // Ensure we are not already in RecipeDetailFragment to prevent crashes
        if (navController.currentDestination?.id == R.id.recipeDetailFragment) {
            return
        }

        navController.navigate(R.id.action_navigation_home_to_recipeDetailFragment, bundle)
    }
}
