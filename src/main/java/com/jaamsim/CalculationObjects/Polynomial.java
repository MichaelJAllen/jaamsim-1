/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2013 Ausenco Engineering Canada Inc.
 * Copyright (C) 2016 JaamSim Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaamsim.CalculationObjects;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.Samples.SampleConstant;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.Samples.SampleProvider;
import com.jaamsim.datatypes.DoubleVector;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;
import com.jaamsim.input.ValueListInput;
import com.jaamsim.units.DimensionlessUnit;
import com.jaamsim.units.Unit;

/**
 * The Polynomial entity returns a user-defined polynomial function of its input value.
 * @author Harry King
 *
 */
public class Polynomial extends DisplayEntity implements SampleProvider {

	@Keyword(description = "The input value to the polynomial.\n"
	                     + "The input can be a dimensionless number or an entity that returns a "
	                     + "dimensionless number, such as an expression, CalculationObject, "
	                     + "ProbabilityDistribution, or a TimeSeries.",
	         exampleList = {"2.5", "1.5*[Calculation1].Value", "Calculation1"})
	protected final SampleInput inputValue;

	@Keyword(description = "The list of dimensionless coefficients for the polynomial function.\n"
	                     + "The number of coefficients provided determines the number of terms "
	                     + "in the polynomial. For example, inputs c0, c1, c2 specifies the "
	                     + "second order polynomial P(x) = c0 + c1*x + c2*x^2 ]",
	         exampleList = {"2.0  1.5"})
	private final ValueListInput coefficientList;

	{
		SampleConstant def = new SampleConstant(DimensionlessUnit.class, 0.0d);
		inputValue = new SampleInput("InputValue", "Key Inputs", def);
		inputValue.setUnitType(DimensionlessUnit.class);
		inputValue.setEntity(this);
		this.addInput(inputValue);

		DoubleVector defList = new DoubleVector();
		defList.add(0.0);
		coefficientList = new ValueListInput("CoefficientList", "Key Inputs", defList);
		coefficientList.setUnitType(DimensionlessUnit.class);
		this.addInput( coefficientList);
	}

	public Polynomial() {}

	@Override
	@Output(name = "Value",
	 description = "The calculated value for the polynomial.",
	    unitType = DimensionlessUnit.class)
	public double getNextSample(double simTime) {
		double x = inputValue.getValue().getNextSample(simTime);
		double pow = 1.0;
		double val = 0.0;
		for(int i=0; i<coefficientList.getValue().size(); i++ ) {
			val += coefficientList.getValue().get(i) * pow;
			pow *= x;
		}
		return val;
	}

	@Override
	public Class<? extends Unit> getUnitType() {
		return DimensionlessUnit.class;
	}

	@Override
	public double getMeanValue(double simTime) {
		return 0;
	}

	@Override
	public double getMinValue() {
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	public double getMaxValue() {
		return Double.POSITIVE_INFINITY;
	}

}
