/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2015 Ausenco Engineering Canada Inc.
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
package com.jaamsim.StringProviders;

import java.util.ArrayList;
import java.util.Collections;

import com.jaamsim.Samples.SampleProvider;
import com.jaamsim.basicsim.Entity;
import com.jaamsim.input.Input;
import com.jaamsim.input.InputErrorException;
import com.jaamsim.input.KeywordIndex;
import com.jaamsim.input.ListInput;
import com.jaamsim.units.Unit;

public class StringProvListInput extends ListInput<ArrayList<StringProvider>> {

	private ArrayList<Class<? extends Unit>> unitTypeList;
	private Entity thisEnt;

	public StringProvListInput(String key, String cat, ArrayList<StringProvider> def) {
		super(key, cat, def);
		// TODO Auto-generated constructor stub
	}

	public void setUnitTypeList(ArrayList<Class<? extends Unit>> utList) {

		if (utList.equals(unitTypeList))
			return;

		unitTypeList = new ArrayList<>(utList);
		this.setValid(false);
	}

	public void setUnitType(Class<? extends Unit> u) {
		ArrayList<Class<? extends Unit>> utList = new ArrayList<>(1);
		utList.add(u);
		this.setUnitTypeList(utList);
	}

	/**
	 * Returns the unit type for the specified expression.
	 * <p>
	 * If the number of expressions exceeds the number of unit types
	 * then the last unit type in the list is returned.
	 * @param i - index of the expression
	 * @return unit type for the expression
	 */
	public Class<? extends Unit> getUnitType(int i) {
		if (unitTypeList.isEmpty())
			return null;
		int k = Math.min(i, unitTypeList.size()-1);
		return unitTypeList.get(k);
	}

	public void setEntity(Entity ent) {
		thisEnt = ent;
	}

	@Override
	public int getListSize() {
		if (value == null)
			return 0;
		else
			return value.size();
	}

	@Override
	public void parse(KeywordIndex kw) throws InputErrorException {
		ArrayList<KeywordIndex> subArgs = kw.getSubArgs();
		ArrayList<StringProvider> temp = new ArrayList<>(subArgs.size());
		for (int i = 0; i < subArgs.size(); i++) {
			KeywordIndex subArg = subArgs.get(i);
			try {
				StringProvider sp = Input.parseStringProvider(subArg, thisEnt, getUnitType(i));
				temp.add(sp);
			}
			catch (InputErrorException e) {
				if (subArgs.size() == 1)
					throw new InputErrorException(e.getMessage());
				else
					throw new InputErrorException(INP_ERR_ELEMENT, i+1, e.getMessage());
			}
		}
		value = temp;
		this.setValid(true);
	}

	@Override
	public ArrayList<String> getValidOptions() {
		ArrayList<String> list = new ArrayList<>();
		for (Entity each : Entity.getClonesOfIterator(Entity.class, SampleProvider.class)) {
			SampleProvider samp = (SampleProvider)each;
			if (unitTypeList.contains(samp.getUnitType()))
				list.add(each.getName());
		}
		Collections.sort(list, Input.uiSortOrder);
		return list;
	}

	@Override
	public void getValueTokens(ArrayList<String> toks) {
		if (value == null) return;

		for (int i = 0; i < value.size(); i++) {
			toks.add("{");
			toks.add(value.get(i).toString());
			toks.add("}");
		}
	}

	@Override
	public void removeReferences(Entity ent) {
		if (value == null)
			return;

		ArrayList<StringProvider> list = new ArrayList<>();
		for (StringProvider samp : value) {
			if (samp instanceof StringProvSample) {
				StringProvSample spsamp = (StringProvSample) samp;
				if (spsamp.getSampleProvider() == ent) {
					list.add(samp);
				}
			}
		}
		value.removeAll(list);
	}

}
