/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.model;

import WS3DCoppelia.util.Constants;
import WS3DCoppelia.util.Constants.ThingsType;
import java.util.HashMap;
import java.util.Map;

/**
 * Models a bag used by the Agent to carry itens.
 * The bag model only keeps tracks of the amount of itens stored in it. The object instance
 * of each item is not handle by the bag.
 *
 * Each Agent is initialized with an empty bag (no itens inside).
 *
 * @author bruno
 */
public class Bag {
    
    private Map<ThingsType, Integer> content = new HashMap();

    /**
     * Insert a specific amount of a thing type into the bag. The existence of
     * the things being inserted is not checked by the method.
     *
     * @param type Thing type to be inserted.
     * @param num Quantity to be added.
     */
    public void insertItem(ThingsType type, int num){
        int current = content.getOrDefault(type, 0);
        content.put(type, current + num);
    }

    /**
     * Remove a specific amount of a thing type from bag.
     *
     * @param type Thing type to be removed.
     * @param num Quantity to be removed.
     * @return True if successful, False when the bag has less than the amount tried to be removed.
     */
    public boolean removeItem(ThingsType type, int num){
        int current = content.getOrDefault(type, 0);
        if(current >= num && num >= 0){
            content.put(type, current - num);
            return true;
        }
        return false;
    }

    /**
     * Count the number of itens from a thing type in the bag.
     *
     * @param type
     * @return
     */
    public int getTotalCountOf(ThingsType type){
        return content.getOrDefault(type, 0);
    }

    public int getTotalNumberFood(){
        return getTotalNumberNPFood() + getTotalNumberPFood();
    }

    public int getTotalNumberPFood() {
        return getTotalCountOf(Constants.FoodTypes.PFOOD);
    }

    public int getTotalNumberNPFood() {
        return getTotalCountOf(Constants.FoodTypes.NPFOOD);
    }
}
