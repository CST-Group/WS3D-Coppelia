/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author bruno
 */
public class Identifiable {
    private static AtomicInteger uniqueId = new AtomicInteger();
    private int id;

    Identifiable() {
       id = uniqueId.incrementAndGet();
    }

    public int getId() {
        return id;
    }
    
    public boolean checkId(int checkId){
        return id == checkId;
    }
}
