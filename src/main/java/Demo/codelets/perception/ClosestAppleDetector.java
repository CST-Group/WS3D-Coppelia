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

package Demo.codelets.perception;



import Demo.Environment;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.Arrays;
import java.util.Collections;
import Demo.memory.CreatureInnerSense;
import WS3DCoppelia.model.Agent;
import WS3DCoppelia.model.Thing;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author klaus
 *
 */
public class ClosestAppleDetector extends Codelet {

	private Memory knownMO;
	private Memory closestAppleMO;
	private Memory innerSenseMO;
        private Agent creature;
	
        private List<Thing> known;

	public ClosestAppleDetector(Agent c) {
            creature = c;
            this.name = "ClosestAppleDetector";
	}


	@Override
	public void accessMemoryObjects() {
		this.knownMO=(MemoryObject)this.getInput("KNOWN_APPLES");
		this.innerSenseMO=(MemoryObject)this.getInput("INNER");
		this.closestAppleMO=(MemoryObject)this.getOutput("CLOSEST_APPLE");	
	}
	@Override
	public void proc() {
                Thing closest_apple=null;
                known = Collections.synchronizedList((List<Thing>) knownMO.getI());
                CreatureInnerSense cis = (CreatureInnerSense) innerSenseMO.getI();
                synchronized(known) {
		   if(known.size() != 0){
			//Iterate over objects in vision, looking for the closest apple
                        CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);
                        for (Thing t : myknown) {
                                if(closest_apple == null){    
                                        closest_apple = t;
                                }
                                else {
                                        List<Double> applePos;
                                        applePos = t.getPos();

                                        List<Double> closest_applePos;
                                        closest_applePos = closest_apple.getPos();
                                        double Dnew = calculateDistance(applePos.get(0), applePos.get(1), cis.position.get(0), cis.position.get(1));
                                        double Dclosest= calculateDistance(closest_applePos.get(0), closest_applePos.get(1), cis.position.get(0), cis.position.get(1));
                                        if(Dnew<Dclosest){
                                                closest_apple = t;
                                        }
                                }
			}
                        
                        if(closest_apple!=null){    
				if(closestAppleMO.getI() == null || !closestAppleMO.getI().equals(closest_apple)){
                                      closestAppleMO.setI(closest_apple);
				}
				
			}else{
				//couldn't find any nearby apples
                                closest_apple = null;
                                closestAppleMO.setI(closest_apple);
			}
		   }
                   else  { // if there are no known apples closest_apple must be null
                        closest_apple = null;
                        closestAppleMO.setI(closest_apple);
		   }
                }
	}//end proc

@Override
        public void calculateActivation() {
        
        }
        
        private double calculateDistance(double x1, double y1, double x2, double y2) {
            return(Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2)));
        }

}
