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

package Demo.codelets.sensors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import Demo.memory.CreatureInnerSense;
import WS3DCoppelia.model.Creature;


/**
 *  This class reads information from this agent's state and writes it to an inner sense sensory buffer.
 * @author klaus
 *
 */

public class InnerSense extends Codelet {

	private Memory innerSenseMO;
        private Creature creature;
        private CreatureInnerSense cis;

	public InnerSense(Creature c) {
                creature = c;
                this.name = "InnerSense";
	}
	@Override
	public void accessMemoryObjects() {
		innerSenseMO=(MemoryObject)this.getOutput("INNER");
                cis = (CreatureInnerSense) innerSenseMO.getI();
	}
	
	public void proc() {
            //env.updateState();
            cis.position = creature.getPos();
            cis.pitch = creature.getPitch();
            cis.fuel = creature.getFuel();
	}
        
        @Override
        public void calculateActivation() {
        
        }



}
