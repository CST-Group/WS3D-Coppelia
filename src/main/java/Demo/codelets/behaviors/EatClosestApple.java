/*****************************************************************************
 * Copyright 2007-2015 DCA-FEEC-UNICAMP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *    Klaus Raizer, Andre Paraense, Ricardo Ribeiro Gudwin
 *****************************************************************************/

package Demo.codelets.behaviors;

import Demo.Environment;
import java.awt.Point;
import java.awt.geom.Point2D;

import org.json.JSONException;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import Demo.memory.CreatureInnerSense;
import WS3DCoppelia.model.Agent;
import WS3DCoppelia.model.Thing;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class EatClosestApple extends Codelet {

	private Memory closestAppleMO;
	private Memory innerSenseMO;
        private Memory knownMO;
	private double reachDistance;
	private Memory handsMO;
        Thing closestApple;
        CreatureInnerSense cis;
        List<Thing> known;

	public EatClosestApple(double reachDistance) {
                setTimeStep(50);
		this.reachDistance=reachDistance;
                this.name = "EatClosestApple";
	}

	@Override
	public void accessMemoryObjects() {
		closestAppleMO=(MemoryObject)this.getInput("CLOSEST_APPLE");
		innerSenseMO=(MemoryObject)this.getInput("INNER");
		handsMO=(MemoryObject)this.getOutput("HANDS");
                knownMO = (MemoryObject)this.getOutput("KNOWN_APPLES");
	}

	@Override
	public void proc() {
                long appleID=0;
                closestApple = (Thing) closestAppleMO.getI();
                cis = (CreatureInnerSense) innerSenseMO.getI();
                known = (List<Thing>) knownMO.getI();
		//Find distance between closest apple and self
		//If closer than reachDistance, eat the apple
		List<Object> action = new ArrayList<Object>();
		if(closestApple != null)
		{
			double appleX=0;
			double appleY=0;
			try {
				appleX=closestApple.getPos().get(0); 
				appleY=closestApple.getPos().get(1); 
                                

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			double selfX=cis.position.get(0);
			double selfY=cis.position.get(1);

			double distance = calculateDistance((double)selfX, (double)selfY, (double)appleX, (double)appleY);
			
			try {
				if(distance<=reachDistance){ //eat it						
					action.add("EATIT");
					action.add(closestApple);
					handsMO.setI(action);
                                        activation=1.0;
                                        DestroyClosestApple();
				}else{
                                        action.add("NOTHING");
					handsMO.setI(action);	//nothing
                                        activation=0.0;
				}
				
//				System.out.println(message);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
                        action.add("NOTHING");
                        handsMO.setI(action);	//nothing
                        activation=0.0;
		}
        //System.out.println("Before: "+known.size()+ " "+known);
        
        //System.out.println("After: "+known.size()+ " "+known);
	//System.out.println("EatClosestApple: "+ handsMO.getInfo());	

	}
        
        @Override
        public void calculateActivation() {
        
        }
        
        public void DestroyClosestApple() {
           int r = -1;
           int i = 0;
           synchronized(known) {
             CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);  
             for (Thing t : known) {
              if (closestApple != null) 
                 if (Objects.equals(t, closestApple)) r = i;
              i++;
             }   
             if (r != -1) known.remove(r);
             closestApple = null;
           }
        }
        
        private double calculateDistance(double x1, double y1, double x2, double y2) {
            return(Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2)));
        }

}
