package com.recipease.project;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by andrewratz on 3/24/18.
 */

public class PersonalRecipesActivity extends DrawerActivity {

    private FirebaseDatabase database;
    private DatabaseReference database_reference;

    private RecyclerView recyclerView;
    private ArrayList<Recipe> recipeList;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    RecipeAdapter recipeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View contentView = inflater.inflate(R.layout.activity_recipe_list, null, false);
        mDrawerLayout.addView(contentView, 0);

        database = FirebaseDatabase.getInstance();
        database_reference = database.getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeList = new ArrayList<>();

        recipeAdapter = new RecipeAdapter(this, recipeList, 0);
        recyclerView.setAdapter(recipeAdapter);

        retrieveRecipes(recipeAdapter, recipeList);

    }

    //Returns a list of all recipes
    public void retrieveRecipes(final RecipeAdapter recipeAdapter, final ArrayList<Recipe> recipeList) {
        // Read recipes in from the database and convert them to an ArrayList of Recipe objects
        database_reference.child("recipes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot all_recipes) {
                //Loop through each separate recipe
                for (DataSnapshot single_recipe : all_recipes.getChildren()) {
                    //Create a new recipe object
                    Recipe recipe = single_recipe.getValue(Recipe.class);
                    if (firebaseUser.getUid().equals(recipe.getOwnerID())) {
                        //Adds this new recipe to the recipe arraylist
                        recipeList.add(recipe);
                    }
                }
                //Asynchronous so have to use this to notify adapter when finished
                recipeAdapter.notifyDataSetChanged();

                //Set results TextView
                TextView resultText = findViewById(R.id.resultText);
                resultText.setText(String.format("%d Results", recipeList.size())); //Size now works :)
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "onCancelled", databaseError.toException());
            }
        });
        return;


    }
}
