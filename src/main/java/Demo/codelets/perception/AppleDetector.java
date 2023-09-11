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

import WS3DCoppelia.model.Identifiable;
import WS3DCoppelia.model.Thing;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Detect apples in the vision field.
 * 	This class detects a number of things related to apples, such as if there are any within reach,
 * any on sight, if they are rotten, and so on.
 * 
 * @author klaus
 *
 */
public class AppleDetector extends Codelet {

        private Memory visionMO;
        private Memory knownApplesMO;

	public AppleDetector(){
            this.name = "AppleDetector";
	}

	@Override
	public void accessMemoryObjects() {
                synchronized(this) {
		    this.visionMO=(MemoryObject)this.getInput("VISION");
                }
		this.knownApplesMO=(MemoryObject)this.getOutput("KNOWN_APPLES");
	}

	@Override
	public void proc() {
            CopyOnWriteArrayList<Thing> vision;
            List<Thing> known;
            synchronized (visionMO) {
               //vision = Collections.synchronizedList((List<Thing>) visionMO.getI());
               vision = new CopyOnWriteArrayList((List<Identifiable>) visionMO.getI());
               known = Collections.synchronizedList((List<Thing>) knownApplesMO.getI());
               //known = new CopyOnWriteArrayList((List<Thing>) knownApplesMO.getI());    
               synchronized(vision) {
                 for (Identifiable obj : vision) {
                     if (obj instanceof Thing) {
                         Thing t = (Thing) obj;
                         if (t.isFood()) {
                             boolean found = false;
                             synchronized (known) {
                                 CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);
                                 for (Thing e : myknown)
                                     if (Objects.equals(t, e)) {
                                         found = true;
                                         break;
                                     }
                                 if (found == false) known.add(t);
                             }
                         }
                     }
               
                 }
               }
            }
	}// end proc
        
        @Override
        public void calculateActivation() {
        
        }


}//end class


