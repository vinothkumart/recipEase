package com.recipease.project;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.List;


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

    FavoriteRecipeAdapter recipeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View contentView = inflater.inflate(R.layout.personal_recipe_list, null, false);
        mDrawerLayout.addView(contentView, 0);

        TextView tvLogo = (TextView) findViewById(R.id.logoText);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Painter.ttf");
        tvLogo.setTypeface(font);

        //TextView tvEditSearch = (TextView) findViewById(R.id.editSearchTextView);
        //tvEditSearch.setEnabled(false);
        //tvEditSearch.setText("Edit Recipes");

        database = FirebaseDatabase.getInstance();
        database_reference = database.getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeList = new ArrayList<>();

        recipeAdapter = new FavoriteRecipeAdapter(this, recipeList);
        recyclerView.setAdapter(recipeAdapter);

        retrieveRecipes(recipeAdapter, recipeList);

    }


    @Override
    protected void onRestart(){
        super.onRestart();

        recipeList.clear();
        retrieveRecipes(recipeAdapter, recipeList);

    }
    //Returns a list of all recipes
    public void retrieveRecipes(final FavoriteRecipeAdapter recipeAdapter, final ArrayList<Recipe> recipeList) {
        if (user.isAnonymous()) {

        }
        else {
            // Read recipes in from the database and convert them to an ArrayList of Recipe objects
            database_reference.child("users").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot the_user) {
                    User user = the_user.getValue(User.class);
                    if (user != null) {
                        List<String> recipesOwned = user.getRecipesOwned();
                        if (recipesOwned == null || recipesOwned.size() == 0) {
                            TextView resultText = findViewById(R.id.favoritesPageResults);
                            resultText.setText(String.format("You didn't make any recipes yet!", recipeList.size()));
                            return;
                        }
                        for (int i = 0; i < recipesOwned.size(); i++) {
                            database_reference.child("recipes").child(recipesOwned.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot the_recipe) {
                                    Recipe recipe = the_recipe.getValue(Recipe.class);
                                    recipeList.add(recipe);
                                    //Asynchronous so have to use this to notify adapter when finished
                                    recipeAdapter.notifyDataSetChanged();
                                    //Set results TextView
                                    TextView resultText = findViewById(R.id.favoritesPageResults);
                                    if (recipeList.size() == 1) {
                                        resultText.setText(String.format("%d Created Recipe", recipeList.size()));
                                    } else {
                                        resultText.setText(String.format("%d Created Recipes", recipeList.size()));
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        }
    }
}
