/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2014 Ausenco Engineering Canada Inc.
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
package com.jaamsim.input;

import com.jaamsim.basicsim.Entity;
import com.jaamsim.units.Unit;

public class ExpResult {

	public interface Iterator {
		public boolean hasNext();
		public ExpResult nextKey() throws ExpError;
	}

	public interface Collection {
		public ExpResult index(ExpResult index) throws ExpError;

		public Iterator getIter();
	}

	public final ExpResType type;

	public final double value;
	public final Class<? extends Unit> unitType;

	public final String stringVal;
	public final Entity entVal;
	public final Collection colVal;

	public static ExpResult makeNumResult(double val, Class<? extends Unit> ut) {
		return new ExpResult(ExpResType.NUMBER, val, ut, null, null, null);
	}

	public static ExpResult makeStringResult(String str) {
		return new ExpResult(ExpResType.STRING, 0, null, str, null, null);
	}

	public static ExpResult makeEntityResult(Entity ent) {
		return new ExpResult(ExpResType.ENTITY, 0, null, null, ent, null);
	}

	public static ExpResult makeCollectionResult(Collection col) {
		return new ExpResult(ExpResType.COLLECTION, 0, null, null, null, col);
	}

	private ExpResult(ExpResType type, double val, Class<? extends Unit> ut, String str, Entity ent, Collection col) {
		this.type = type;
		value = val;
		unitType = ut;

		stringVal = str;
		entVal = ent;
		colVal = col;
	}

	public <T> T getValue(double simTime, Class<T> klass) {
		// Make a best effort to return the type
		if (klass.isAssignableFrom(ExpResult.class))
			return klass.cast(this);

		if (type == ExpResType.STRING && klass.isAssignableFrom(String.class)) {
			return klass.cast(stringVal);
		}

		if (type == ExpResType.ENTITY && klass.isAssignableFrom(Entity.class)) {
			return klass.cast(entVal);
		}

		if (klass.equals(double.class) || klass.equals(Double.class)) {
			if (type == ExpResType.NUMBER)
			return klass.cast(value);
		}

		return null;
	}

}
