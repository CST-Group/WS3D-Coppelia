/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.model;

import WS3DCoppelia.util.Constants.ThingsType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bruno
 */
public class Bag {
    
    private Map<ThingsType, Integer> content = new HashMap();
    
    public void insertItem(ThingsType type, int num){
        int current = content.getOrDefault(type, 0);
        content.put(type, current + num);
    }
    
    public boolean removeItem(ThingsType type, int num){
        int current = content.getOrDefault(type, 0);
        if(current >= num && num >= 0){
            content.put(type, current - num);
            return true;
        }
        return false;
    }
    
    public int getTotalCountOf(ThingsType type){
        return content.getOrDefault(type, 0);
    }
}
