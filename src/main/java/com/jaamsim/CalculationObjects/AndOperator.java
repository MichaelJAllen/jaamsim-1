/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2013 Ausenco Engineering Canada Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.jaamsim.CalculationObjects;

import com.sandwell.JavaSimulation.BooleanListInput;
import com.sandwell.JavaSimulation.EntityListInput;
import com.sandwell.JavaSimulation.InputErrorException;
import com.sandwell.JavaSimulation.Keyword;

/**
 * The AndOperator implements the Boolean "And" operation.
 * @author Harry King
 *
 */
public class AndOperator extends BooleanCalculation {

	@Keyword(desc = "The list of BooleanCalculation entities that are inputs to this operation.",
	         example = "AndOperator1 EntityList { Bool1  Bool2 }")
	private final EntityListInput<BooleanCalculation> entityListInput;

	@Keyword(desc = "The list of true/false inputs cooresponding to the EntityList.  An entry of TRUE indicates that a NOT operation will be applied to the input.",
	         example = "AndOperator1 NegationList { TRUE  FALSE }")
	private final BooleanListInput negationListInput;

	{
	entityListInput = new EntityListInput<BooleanCalculation>( BooleanCalculation.class, "EntityList", "Key Inputs", null);
		this.addInput( entityListInput, true);

		negationListInput = new BooleanListInput( "NegationList", "Key Inputs", null);
		this.addInput( negationListInput, true);
	}

	@Override
	public void validate() {
		super.validate();

		// Confirm that the number of entries in the NegationList matches the EntityList
		if( negationListInput.getValue().size() != entityListInput.getValue().size() ) {
			throw new InputErrorException( "The number of entries for NegationList and EntityList must be equal" );
		}
	}

	@Override
	public void update() {
		boolean val = true;

		// Loop through the input values
		for(int i=0; i<entityListInput.getValue().size(); i++ ) {
			if( negationListInput.getValue().get(i) ) {
				if( entityListInput.getValue().get(i).getValue() ) {
					val = false;
					break;
				}
			}
			else {
				if( ! entityListInput.getValue().get(i).getValue() ) {
					val = false;
					break;
				}
			}
		}

		// Set the present value
		this.setValue( val );
		return;
	}
}
