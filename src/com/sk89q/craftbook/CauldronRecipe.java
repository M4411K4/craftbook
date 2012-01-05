// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

/**
 *
 * @author sk89q
 */
public final class CauldronRecipe {
    /**
     * Recipe name.
     */
    private final String name;
    /**
     * Stores a list of ingredients.
     */
    private final List<CraftBookItem> ingredients;
    /**
     * Stores a list of ingredients.
     */
    private final Map<CraftBookItem,Integer> ingredientLookup
            = new HashMap<CraftBookItem,Integer>();
    /**
     * List of resulting items or blocks.
     */
    private final List<CraftBookItem> results;
    /**
     * List of groups that can use this recipe. This may be null.
     */
    private final String[] groups;

    /**
     * Construct the instance. The list will be sorted.
     * 
     * @param ingredients
     * @param results
     */
    public CauldronRecipe(String name, List<CraftBookItem> ingredients,
            List<CraftBookItem> results, String[] groups) {
        this.name = name;
        this.ingredients = Collections.unmodifiableList(ingredients);
        this.results = Collections.unmodifiableList(results);
        this.groups = groups;

        // Make a list of required ingredients by item ID
        for (CraftBookItem id : ingredients) {
        	
            if (ingredientLookup.containsKey(id)) {
                ingredientLookup.put(id, ingredientLookup.get(id) + 1);
            } else {
                ingredientLookup.put(id, 1);
            }
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the ingredients
     */
    public List<CraftBookItem> getIngredients() {
        return ingredients;
    }

    /**
     * @return the groups
     */
    public String[] getGroups() {
        return groups;
    }

    /**
     * Checks to see if all the ingredients are met.
     *
     * @param check
     * @return
     */
    public boolean hasAllIngredients(Map<CraftBookItem,Integer> check) {
        for (Map.Entry<CraftBookItem,Integer> entry : ingredientLookup.entrySet()) {
            CraftBookItem id = entry.getKey();
            if (!check.containsKey(id)) {
                return false;
            } else if (check.get(id) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the results
     */
    public List<CraftBookItem> getResults() {
        return results;
    }
}
