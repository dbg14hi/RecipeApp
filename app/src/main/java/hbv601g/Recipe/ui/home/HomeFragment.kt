package hbv601g.Recipe.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
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
    private lateinit var searchView: SearchView
    private lateinit var dietaryRestrictionsLayout: LinearLayout
    private lateinit var mealCategoriesLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var createRecipeFab: FloatingActionButton
    private val db = FirebaseFirestore.getInstance()

    private lateinit var filterButton: Button
    private lateinit var filterContainer: LinearLayout

    var selectedDietaryRestrictions = mutableListOf<String>()
    var selectedMealCategories = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        searchView = binding.recipeSearchView
        dietaryRestrictionsLayout = binding.dietaryRestrictionsLayout
        mealCategoriesLayout = binding.mealCategoriesLayout

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        // Filter visibility toggle on and off
        filterButton = binding.filterButton
        filterContainer = binding.filterContainer

        filterButton.setOnClickListener {
            if (filterContainer.visibility == View.GONE) {
                filterContainer.visibility = View.VISIBLE
            } else {
                filterContainer.visibility = View.GONE
            }
        }

        // Make create recipe button
        createRecipeFab = binding.createRecipeFab

        if (FirebaseAuth.getInstance().currentUser != null) {
            createRecipeFab.visibility = View.VISIBLE
            createRecipeFab.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_home_to_createRecipeFragment)
            }
        } else {
            createRecipeFab.visibility = View.GONE
        }

        adapter = RecipeAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        // Add category checkboxes and add listener
        val dietaryRestrictions = listOf("Vegan", "Vegetarian", "Gluten-Free", "Dairy-Free", "Nut-free")
        dietaryRestrictions.forEach { category ->
            val checkBox = CheckBox(context)
            checkBox.text = category
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                updateSelectedCategories()
            }
            dietaryRestrictionsLayout.addView(checkBox)
        }

        val mealCategories = listOf("Breakfast", "Lunch", "Snack", "Dinner")
        mealCategories.forEach { category ->
            val checkBox = CheckBox(context)
            checkBox.text = category
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                updateSelectedCategories()
            }
            mealCategoriesLayout.addView(checkBox)
        }

        // Search for recipes and update view
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                val selectedDietaryRestrictions = homeViewModel.selectedDietaryRestrictions.value ?: emptyList()
                val selectedMealCategories = homeViewModel.selectedMealCategories.value ?: emptyList()
                homeViewModel.filterRecipes(newText.orEmpty(), selectedDietaryRestrictions, selectedMealCategories)
                return true
            }
        })

        homeViewModel.filteredRecipesLiveData.observe(viewLifecycleOwner) { recipes ->
            adapter.updateData(recipes)
        }

        homeViewModel.errorLiveData.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }

        // Setup sorting dropdown
        setupSortingDropdown()

        // Fetch recipes in real-time
        fetchRecipesInRealTime()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupSortingDropdown() {
        val sortingOptions = arrayOf("A-Z", "Z-A", "Newest First", "Oldest First" )
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

    private fun fetchRecipesInRealTime() {
        db.collection("recipes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.e("HomeFragment", "Error loading recipes: ", error)
                    return@addSnapshotListener
                }

                if (querySnapshot == null || querySnapshot.isEmpty) {
                    Log.d("HomeFragment", "No recipes found in Firestore.")
                    return@addSnapshotListener
                }

                val recipeList = mutableListOf<Recipe>()
                for (document in querySnapshot.documents) {
                    val recipe = document.toObject(Recipe::class.java)
                    if (recipe != null) {
                        recipe.recipeId = document.id
                        recipeList.add(recipe)
                    } else {
                        Log.e("HomeFragment", "Error parsing recipe: ${document.id}")
                    }
                }

                Log.d("HomeFragment", "Loaded ${recipeList.size} recipes")
                adapter.updateData(recipeList)
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

        if (navController.currentDestination?.id == R.id.recipeDetailFragment) {
            return
        }

        navController.navigate(R.id.action_navigation_home_to_recipeDetailFragment, bundle)
    }

    private fun updateSelectedCategories() {
        val dietaryRestrictions = mutableListOf<String>()
        for (i in 0 until dietaryRestrictionsLayout.childCount) {
            val checkBox = dietaryRestrictionsLayout.getChildAt(i) as CheckBox
            if (checkBox.isChecked) {
                dietaryRestrictions.add(checkBox.text.toString())
            }
        }

        val mealCategories = mutableListOf<String>()
        for (i in 0 until mealCategoriesLayout.childCount) {
            val checkBox = mealCategoriesLayout.getChildAt(i) as CheckBox
            if (checkBox.isChecked) {
                mealCategories.add(checkBox.text.toString())
            }
        }
        homeViewModel.setSelectedCategories(dietaryRestrictions, mealCategories)
    }
}
