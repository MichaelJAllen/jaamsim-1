/*
 * JaamSim Discrete Event Simulation
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
package com.jaamsim.ProcessFlow;

import java.util.ArrayList;

import com.jaamsim.BasicObjects.DowntimeEntity;
import com.jaamsim.Thresholds.Threshold;
import com.jaamsim.Thresholds.ThresholdUser;
import com.jaamsim.basicsim.Simulation;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;
import com.jaamsim.states.DowntimeUser;
import com.jaamsim.states.StateEntity;

public abstract class StateUserEntity extends StateEntity implements ThresholdUser, DowntimeUser {

	@Keyword(description = "A list of thresholds that must be satisfied for the object to "
	                     + "operate. Operation is stopped immediately when one of the thresholds "
	                     + "closes. If a threshold closes part way though processing an entity, "
	                     + "the work is considered to be partly done and the remainder is "
	                     + "completed once the threshold re-opens.",
	         exampleList = {"ExpressionThreshold1 TimeSeriesThreshold1 SignalThreshold1"})
	protected final EntityListInput<Threshold> immediateThresholdList;

	@Keyword(description = "A list of thresholds that must be satisfied for the object to "
	                     + "operate. Operation is stopped immediately when one of the thresholds "
	                     + "closes. If a threshold closes part way though processing an entity, "
	                     + "the work is interrupted and the entity is released.",
	         exampleList = {"ExpressionThreshold1 TimeSeriesThreshold1 SignalThreshold1"})
	protected final EntityListInput<Threshold> immediateReleaseThresholdList;

	@Keyword(description = "A list of thresholds that must be satisfied for the object to "
	                     + "operate. If a threshold closes part way though processing an entity, "
	                     + "the remaining work is completed and the entity is released before the "
	                     + "object is closed.",
	         exampleList = {"ExpressionThreshold1 TimeSeriesThreshold1 SignalThreshold1"})
	protected final EntityListInput<Threshold> operatingThresholdList;

	@Keyword(description = "A list of DowntimeEntities representing planned maintenance that "
	                     + "must be performed immediately, interrupting any work underway at "
	                     + "present.",
	         exampleList = {"DowntimeEntity1 DowntimeEntity2 DowntimeEntity3"})
	protected final EntityListInput<DowntimeEntity> immediateMaintenanceList;

	@Keyword(description = "A list of DowntimeEntities representing planned maintenance that "
	                     + "must begin as soon as task underway at present is finished.",
	         exampleList = {"DowntimeEntity1 DowntimeEntity2 DowntimeEntity3"})
	protected final EntityListInput<DowntimeEntity> forcedMaintenanceList;

	@Keyword(description = "A list of DowntimeEntities representing planned maintenance that "
	                     + "can wait until task underway at present is finished and the queue "
	                     + "of tasks is empty.",
	         exampleList = {"DowntimeEntity1 DowntimeEntity2 DowntimeEntity3"})
	protected final EntityListInput<DowntimeEntity> opportunisticMaintenanceList;

	@Keyword(description = "A list of DowntimeEntities representing unplanned maintenance that "
	                     + "must be performed immediately, interrupting any work underway at "
	                     + "present.",
	         exampleList = {"DowntimeEntity1 DowntimeEntity2 DowntimeEntity3"})
	protected final EntityListInput<DowntimeEntity> immediateBreakdownList;

	@Keyword(description = "A list of DowntimeEntities representing unplanned maintenance that "
	                     + "must begin as soon as task underway at present is finished.",
	         exampleList = {"DowntimeEntity1 DowntimeEntity2 DowntimeEntity3"})
	protected final EntityListInput<DowntimeEntity> forcedBreakdownList;

	@Keyword(description = "A list of DowntimeEntities representing unplanned maintenance that "
	                     + "can wait until task underway at present is finished and the queue "
	                     + "of tasks is empty.",
	         exampleList = {"DowntimeEntity1 DowntimeEntity2 DowntimeEntity3"})
	protected final EntityListInput<DowntimeEntity> opportunisticBreakdownList;

	private boolean busy;  // indicates that work is being performed

	{
		immediateThresholdList = new EntityListInput<>(Threshold.class, "ImmediateThresholdList", "Key Inputs", new ArrayList<Threshold>());
		this.addInput(immediateThresholdList);

		immediateReleaseThresholdList = new EntityListInput<>(Threshold.class, "ImmediateReleaseThresholdList", "Key Inputs", new ArrayList<Threshold>());
		this.addInput(immediateReleaseThresholdList);

		operatingThresholdList = new EntityListInput<>(Threshold.class, "OperatingThresholdList", "Key Inputs", new ArrayList<Threshold>());
		this.addInput(operatingThresholdList);

		immediateMaintenanceList =  new EntityListInput<>(DowntimeEntity.class,
				"ImmediateMaintenanceList", "Maintenance", new ArrayList<DowntimeEntity>());
		this.addInput(immediateMaintenanceList);

		forcedMaintenanceList =  new EntityListInput<>(DowntimeEntity.class,
				"ForcedMaintenanceList", "Maintenance", new ArrayList<DowntimeEntity>());
		this.addInput(forcedMaintenanceList);

		opportunisticMaintenanceList =  new EntityListInput<>(DowntimeEntity.class,
				"OpportunisticMaintenanceList", "Maintenance", new ArrayList<DowntimeEntity>());
		this.addInput(opportunisticMaintenanceList);

		immediateBreakdownList =  new EntityListInput<>(DowntimeEntity.class,
				"ImmediateBreakdownList", "Maintenance", new ArrayList<DowntimeEntity>());
		this.addInput(immediateBreakdownList);

		forcedBreakdownList =  new EntityListInput<>(DowntimeEntity.class,
				"ForcedBreakdownList", "Maintenance", new ArrayList<DowntimeEntity>());
		this.addInput(forcedBreakdownList);

		opportunisticBreakdownList =  new EntityListInput<>(DowntimeEntity.class,
				"OpportunisticBreakdownList", "Maintenance", new ArrayList<DowntimeEntity>());
		this.addInput(opportunisticBreakdownList);
	}

	public StateUserEntity() {}

	@Override
	public void earlyInit() {
		super.earlyInit();

		this.addState("Idle");
		this.addState("Working");
		this.addState("Stopped");
		this.addState("Maintenance");
		this.addState("Breakdown");
	}

	@Override
	public boolean isValidState(String state) {
		return true;
	}

	// ********************************************************************************************
	// THRESHOLDS
	// ********************************************************************************************

	@Override
	public ArrayList<Threshold> getThresholds() {
		ArrayList<Threshold> ret = new ArrayList<>(operatingThresholdList.getValue());
		ret.addAll(immediateThresholdList.getValue());
		ret.addAll(immediateReleaseThresholdList.getValue());
		return ret;
	}

	protected boolean isImmediateThresholdClosure() {
		for (Threshold thresh : immediateThresholdList.getValue()) {
			if (!thresh.isOpen())
				return true;
		}
		return false;
	}

	protected boolean isImmediateReleaseThresholdClosure() {
		for (Threshold thresh : immediateReleaseThresholdList.getValue()) {
			if (!thresh.isOpen())
				return true;
		}
		return false;
	}

	// ********************************************************************************************
	// PRESENT STATE
	// ********************************************************************************************

	protected void setBusy(boolean bool) {
		busy = bool;
	}

	protected final boolean isBusy() {
		return busy;
	}

	/**
	 * Tests whether all the thresholds are open.
	 * @return true if all the thresholds are open.
	 */
	protected final boolean isOpen() {
		for (Threshold thr : immediateThresholdList.getValue()) {
			if (!thr.isOpen())
				return false;
		}
		for (Threshold thr : immediateReleaseThresholdList.getValue()) {
			if (!thr.isOpen())
				return false;
		}
		for (Threshold thr : operatingThresholdList.getValue()) {
			if (!thr.isOpen())
				return false;
		}
		return true;
	}

	protected boolean isMaintenance() {
		for (DowntimeEntity de : immediateMaintenanceList.getValue()) {
			if (de.isDown())
				return true;
		}
		for (DowntimeEntity de : forcedMaintenanceList.getValue()) {
			if (de.isDown())
				return true;
		}
		for (DowntimeEntity de : opportunisticMaintenanceList.getValue()) {
			if (de.isDown())
				return true;
		}
		return false;
	}

	protected boolean isBreakdown() {
		for (DowntimeEntity de : immediateBreakdownList.getValue()) {
			if (de.isDown())
				return true;
		}
		for (DowntimeEntity de : forcedBreakdownList.getValue()) {
			if (de.isDown())
				return true;
		}
		for (DowntimeEntity de : opportunisticBreakdownList.getValue()) {
			if (de.isDown())
				return true;
		}
		return false;
	}

	public boolean isAvailable() {
		return isOpen() && !isMaintenance() && !isBreakdown();
	}

	/**
	 * Tests whether the entity is available for work.
	 * <p>
	 * There are three mutually exclusive states: Busy, Idle, and UnableToWork. The UnableToWork
	 * condition is divided into three separate states: Maintenance, Breakdown, and Stopped.
	 * @return true if the LinkedService is available for work
	 */
	public boolean isIdle() {
		return !isBusy() && isAvailable();
	}

	/**
	 * Tests whether something is preventing work from being performed.
	 * <p>
	 * There are three mutually exclusive states: Busy, Idle, and UnableToWork. The UnableToWork
	 * condition is divided into three separate states: Maintenance, Breakdown, and Stopped.
	 * @return true if something is preventing work from being performed
	 */
	public boolean isUnableToWork() {
		return !isBusy() && !isAvailable();
	}

	@Override
	public void setPresentState() {

		// Working (Busy)
		if (this.isBusy()) {
			this.setPresentState("Working");
			return;
		}

		// Not working because of maintenance or a closure (UnableToWork)
		if (this.isMaintenance()) {
			this.setPresentState("Maintenance");
			return;
		}
		if (this.isBreakdown()) {
			this.setPresentState("Breakdown");
			return;
		}
		if (!this.isOpen()) {
			this.setPresentState("Stopped");
			return;
		}

		// Not working because there is nothing to do (Idle)
		this.setPresentState("Idle");
		return;
	}

	// ********************************************************************************************
	// MAINTENANCE AND BREAKDOWNS
	// ********************************************************************************************

	@Override
	public ArrayList<DowntimeEntity> getMaintenanceEntities() {
		ArrayList<DowntimeEntity> ret = new ArrayList<>();
		ret.addAll(immediateMaintenanceList.getValue());
		ret.addAll(forcedMaintenanceList.getValue());
		ret.addAll(opportunisticMaintenanceList.getValue());
		return ret;
	}

	@Override
	public ArrayList<DowntimeEntity> getBreakdownEntities() {
		ArrayList<DowntimeEntity> ret = new ArrayList<>();
		ret.addAll(immediateBreakdownList.getValue());
		ret.addAll(forcedBreakdownList.getValue());
		ret.addAll(opportunisticBreakdownList.getValue());
		return ret;
	}

	public boolean isImmediateDowntime(DowntimeEntity down) {
		return immediateMaintenanceList.getValue().contains(down)
				|| immediateBreakdownList.getValue().contains(down);
	}

	public boolean isForcedDowntime(DowntimeEntity down) {
		return forcedMaintenanceList.getValue().contains(down)
				|| forcedBreakdownList.getValue().contains(down);
	}

	public boolean isOpportunisticDowntime(DowntimeEntity down) {
		return opportunisticMaintenanceList.getValue().contains(down)
				|| opportunisticBreakdownList.getValue().contains(down);
	}

	// ********************************************************************************************
	// OUTPUTS
	// ********************************************************************************************

	@Output(name = "Open",
	 description = "Returns TRUE if all the thresholds specified by the OperatingThresholdList "
	             + "and ImmediateThresholdList keywords are open.",
	    sequence = 1)
	public boolean getOpen(double simTime) {
		return isOpen();
	}

	@Output(name = "Working",
	 description = "Returns TRUE if work is being performed.",
	    sequence = 2)
	public boolean isBusy(double simTime) {
		return isBusy();
	}

	@Output(name = "Maintenance",
	 description = "Returns TRUE if maintenance is being performed.",
	    sequence = 3)
	public boolean isMaintenance(double simTime) {
		return isMaintenance();
	}

	@Output(name = "Breakdown",
	 description = "Returns TRUE if a breakdown is being repaired.",
	    sequence = 4)
	public boolean isBreakdown(double simTime) {
		return isBreakdown();
	}

	@Output(name = "Utilisation",
	 description = "The fraction of calendar time (excluding the initialisation period) that "
	             + "this object is in the Working state.",
	  reportable = true,
	    sequence = 5)
	public double getUtilisation(double simTime) {
		double total = simTime;
		if (simTime > Simulation.getInitializationTime())
			total -= Simulation.getInitializationTime();
		double working = this.getTimeInState(simTime, "Working");
		return working/total;
	}

	@Output(name = "Commitment",
	 description = "The fraction of calendar time (excluding the initialisation period) that "
	             + "this object is in any state other than Idle.",
	  reportable = true,
	    sequence = 6)
	public double getCommitment(double simTime) {
		double total = simTime;
		if (simTime > Simulation.getInitializationTime())
			total -= Simulation.getInitializationTime();
		double idle = this.getTimeInState(simTime, "Idle");
		return 1.0d - idle/total;
	}

	@Output(name = "Availability",
	 description = "The fraction of calendar time (excluding the initialisation period) that "
	             + "this object is in any state other than Maintenance or Breakdown.",
	  reportable = true,
	    sequence = 7)
	public double getAvailability(double simTime) {
		double total = simTime;
		if (simTime > Simulation.getInitializationTime())
			total -= Simulation.getInitializationTime();
		double maintenance = this.getTimeInState(simTime, "Maintenance");
		double breakdown = this.getTimeInState(simTime, "Breakdown");
		return 1.0d - (maintenance + breakdown)/total;
	}

	@Output(name = "Reliability",
	 description = "The ratio of Working time to the sum of Working time and Breakdown time. "
	             + "All times exclude the initialisation period.",
	  reportable = true,
	    sequence = 8)
	public double getReliability(double simTime) {
		double working = this.getTimeInState(simTime, "Working");
		double breakdown = this.getTimeInState(simTime, "Breakdown");
		return working / (working + breakdown);
	}

}
