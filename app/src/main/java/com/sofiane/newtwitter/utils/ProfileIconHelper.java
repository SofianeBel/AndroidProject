package com.sofiane.newtwitter.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.sofiane.newtwitter.R;

/**
 * Classe utilitaire pour gérer les icônes et couleurs de profil
 */
public class ProfileIconHelper {

    /**
     * Obtient l'icône de profil correspondant à l'index
     * @param context Le contexte
     * @param iconIndex L'index de l'icône
     * @return Le drawable de l'icône
     */
    public static Drawable getProfileIcon(Context context, int iconIndex) {
        int resourceId;
        
        switch (iconIndex) {
            case 0:
                resourceId = R.drawable.ic_profile_person;
                break;
            case 1:
                resourceId = R.drawable.ic_profile_star;
                break;
            case 2:
                resourceId = R.drawable.ic_profile_heart;
                break;
            case 3:
                resourceId = R.drawable.ic_profile_diamond;
                break;
            case 4:
                resourceId = R.drawable.ic_profile_circle;
                break;
            case 5:
                resourceId = R.drawable.ic_profile_square;
                break;
            default:
                resourceId = R.drawable.ic_profile_person;
                break;
        }
        
        return ContextCompat.getDrawable(context, resourceId);
    }
    
    /**
     * Obtient la couleur de profil correspondant à l'index
     * @param context Le contexte
     * @param colorIndex L'index de la couleur
     * @return La couleur au format int
     */
    public static int getProfileColor(Context context, int colorIndex) {
        TypedArray colors = context.getResources().obtainTypedArray(R.array.profile_color_values);
        
        // Vérifier que l'index est valide
        if (colorIndex < 0 || colorIndex >= colors.length()) {
            colorIndex = 0; // Utiliser la première couleur par défaut
        }
        
        int color = colors.getColor(colorIndex, Color.BLUE);
        colors.recycle();
        
        return color;
    }
    
    /**
     * Obtient l'icône de profil avec la couleur correspondante
     * @param context Le contexte
     * @param iconIndex L'index de l'icône
     * @param colorIndex L'index de la couleur
     * @return Le drawable de l'icône colorée
     */
    public static Drawable getColoredProfileIcon(Context context, int iconIndex, int colorIndex) {
        Drawable icon = getProfileIcon(context, iconIndex);
        int color = getProfileColor(context, colorIndex);
        
        Drawable wrappedDrawable = DrawableCompat.wrap(icon.mutate());
        DrawableCompat.setTint(wrappedDrawable, color);
        
        return wrappedDrawable;
    }
} 