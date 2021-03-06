package com.recipease.project;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.content.ContextCompat.startActivity;

public class FavoriteRecipeAdapter extends RecyclerView.Adapter<FavoriteRecipeAdapter.ViewHolder> {


    private FirebaseDatabase database;
    private ArrayList<Recipe> recipeList;
    private Context context;
    private DatabaseReference database_reference;
    int nFavorites;
    // private DatabaseReference favorites_reference;



    FavoriteRecipeAdapter(Context context, ArrayList<Recipe> recipeList) {
        this.recipeList = recipeList;
        this.context = context;
    }

    @Override
    public FavoriteRecipeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.favorites_item_card, parent, false));
    }

    @Override
    public void onBindViewHolder(FavoriteRecipeAdapter.ViewHolder holder, int position) {
        Recipe currentRecipe = recipeList.get(position);
        holder.bindTo(currentRecipe);
        if (currentRecipe.getImageURL().equals("")) {
            Glide.with(context).load(R.drawable.no_image).into(holder.recipeImage);
        }
        else {
            Glide.with(context).load(currentRecipe.getImageURL()).into(holder.recipeImage);
        }
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView titleText;
        private TextView numFavoritesText;
        private ImageView recipeImage;
        private TextView missingIngredientsText;

        ViewHolder(View itemView) {
            super(itemView);

            titleText = (TextView) itemView.findViewById(R.id.title);
            numFavoritesText = (TextView) itemView.findViewById(R.id.favorites);
            missingIngredientsText = (TextView) itemView.findViewById(R.id.missing_ingredients);
            recipeImage = itemView.findViewById(R.id.recipeImage);

            itemView.setOnClickListener( new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Recipe currentRecipe = recipeList.get(getAdapterPosition());
                    Intent details = new Intent(context, RecipeDetailsActivity.class);
                    sendRecipe(details, currentRecipe);
                    startActivity(context, details, null);

                }
            });
        }

        void bindTo(Recipe currentRecipe) {
            //Populate the textviews with data
            titleText.setText(currentRecipe.getTitle());
            numFavoritesText.setText(String.format("%d", currentRecipe.getNumFavorites()));

        }

    }

    private void sendRecipe(Intent intent, Recipe recipe_to_bring) {
        String title = recipe_to_bring.getTitle();
        String recipeID = recipe_to_bring.getRecipeID();
        String imageURL = recipe_to_bring.getImageURL();
        int numFavorites = recipe_to_bring.getNumFavorites();
        List<String> cookingIngredients = recipe_to_bring.getCookingIngredients();
        List<String> cookingInstructions = recipe_to_bring.getCookingInstructions();
        List<String> comments = recipe_to_bring.getComments();
        intent.putExtra("TITLE", title);
        intent.putExtra("UNIQUE ID", recipeID);
        intent.putExtra("IMAGE URL", imageURL);
        intent.putExtra("NUM FAVORITES", numFavorites);
        intent.putStringArrayListExtra("INGREDIENTS LIST", (ArrayList) cookingIngredients);
        intent.putStringArrayListExtra("INSTRUCTIONS LIST", (ArrayList) cookingInstructions);
        intent.putStringArrayListExtra("COMMENTS", (ArrayList) comments);
    }


}