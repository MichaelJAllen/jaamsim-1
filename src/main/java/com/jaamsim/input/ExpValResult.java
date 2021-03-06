package com.jaamsim.input;

import java.util.ArrayList;

import com.jaamsim.units.DimensionlessUnit;
import com.jaamsim.units.Unit;


public class ExpValResult {

	public static enum State {
		VALID, ERROR, UNDECIDABLE
	}

	public final State state;
	public final ArrayList<ExpError> errors;

	public final Class<? extends Unit> unitType;
	public final ExpResType type;

	public static String typeString(ExpResType res) {
		switch(res) {
		case ENTITY:
			return "entity";
		case NUMBER:
			return "number";
		case STRING:
			return "string";
		default:
			assert(false);
			return "unknown type";
		}
	}

	public static ExpValResult makeValidRes(ExpResType t, Class<? extends Unit> ut)
	{
		return new ExpValResult(State.VALID, t, ut, null);
	}

	public static ExpValResult makeUndecidableRes()
	{
		return new ExpValResult(State.UNDECIDABLE, null, DimensionlessUnit.class, null);
	}

	public static ExpValResult makeErrorRes(ArrayList<ExpError> es) {
		return new ExpValResult(State.ERROR, null, DimensionlessUnit.class, es);
	}

	public static ExpValResult makeErrorRes(ExpError error) {
		ArrayList<ExpError> es = new ArrayList<>(1);
		es.add(error);
		return new ExpValResult(State.ERROR, null, DimensionlessUnit.class, es);
	}

	private ExpValResult(State s, ExpResType t, Class<? extends Unit> ut, ArrayList<ExpError> es) {
		state = s;
		unitType = ut;
		type = t;

		if (es == null)
			errors = new ArrayList<>();
		else
			errors = es;
	}
}
