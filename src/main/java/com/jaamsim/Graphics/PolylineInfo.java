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
package com.jaamsim.Graphics;

import java.util.ArrayList;

import com.jaamsim.math.Color4d;
import com.jaamsim.math.Vec3d;

public class PolylineInfo {
	public static enum CurveType {
		LINEAR,
		BEZIER,
		SPLINE
	}

	private final ArrayList<Vec3d> points;
	private final CurveType curveType;
	private final ArrayList<Vec3d> curvePoints;
	private final Color4d color;
	private final int width; // Line width in pixels

	public PolylineInfo(ArrayList<Vec3d> pts, CurveType ct, Color4d col, int w) {
		points = pts;
		curveType = ct;
		color = col;
		width = w;

		switch (curveType) {
		case LINEAR:
			curvePoints = points;
			break;
		case BEZIER:
			curvePoints = getBezierPoints(points);
			break;
		case SPLINE:
			curvePoints = getSplinePoints(points);
			break;
		default:
			assert(false);
			curvePoints = null;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof PolylineInfo)) return false;

		PolylineInfo pi = (PolylineInfo)o;

		return points != null && points.equals(pi.points) &&
		       curveType == pi.curveType &&
		       color != null && color.equals(pi.color) &&
		       width == pi.width;
	}

	public ArrayList<Vec3d> getPoints() {
		return points;
	}

	public ArrayList<Vec3d> getCurvePoints() {
		return curvePoints;
	}

	public Color4d getColor() {
		return color;
	}

	public int getWidth() {
		return width;
	}

	@Override
	public String toString() {
		return points.toString();
	}

	public static ArrayList<Vec3d> getBezierPoints(ArrayList<Vec3d> ps) {
		ArrayList<Vec3d> ret = new ArrayList<>();

		// The total number of segments in this curve
		final int NUM_POINTS = 32;

		double tInc = 1.0/NUM_POINTS;
		for (int i = 0; i < NUM_POINTS; ++i) {
			ret.add(solveBezier(i*tInc, 0, ps.size()-1, ps));
		}
		ret.add(ps.get(ps.size()-1));
		return ret;
	}

	public static ArrayList<Vec3d> getSplinePoints(ArrayList<Vec3d> ps) {
		// This spline fitting algorithm is a bit of a custom creation. It is loosely based on the finte differences method described
		// in this article: https://en.wikipedia.org/wiki/Cubic_Hermite_spline (assuming it hasn't changed since).
		// The major changes are:
		// * All segments are solved in Bezier form (this is more stylist than a change in algorithm)
		// * The end two segments are quadratic Bezier curves (not cubic) and as such the end tangent is un-constrained.
		// * The internal control points are scaled down proportional to segment length to avoid kinks and self intersection within a segment

		// If there's too few points, just return the existing line
		if (ps.size() <= 2) {
			return ps;
		}

		// The number of segments between each control point
		final int NUM_SEGMENTS = 16;

		Vec3d temp = new Vec3d();

		// Generate tangents for internal points only
		ArrayList<Vec3d> tangents = new ArrayList<>();
		for (int i = 1; i < ps.size()-1; ++i) {
			Vec3d pMin = ps.get(i-1);
			Vec3d p = ps.get(i);
			Vec3d pPlus = ps.get(i+1);

			temp.sub3(p, pMin);
			double l0 = temp.mag3();

			temp.sub3(p, pPlus);
			double l1 = temp.mag3();

			Vec3d tan = new Vec3d();
			tan.sub3(pPlus, pMin);
			tan.scale3(1.0/(l0 + l1));
			tangents.add(tan);
		}

		double tInc = 1.0/NUM_SEGMENTS;

		ArrayList<Vec3d> ret = new ArrayList<>();

		Vec3d scaledTanTemp = new Vec3d();
		Vec3d segDiffTemp = new Vec3d();

		// Start with a quadratic segment
		{
			Vec3d p0 = ps.get(0);
			Vec3d p1 = ps.get(1);

			segDiffTemp.sub3(p0, p1);
			double segLength = segDiffTemp.mag3();

			Vec3d c = new Vec3d(p1);
			scaledTanTemp.scale3(segLength/2.0, tangents.get(0));
			c.sub3(scaledTanTemp);

			for (int t = 0; t < NUM_SEGMENTS; ++t) {
				Vec3d curvePoint = solveQuadraticBezier(t*tInc, p0, p1, c);
				ret.add(curvePoint);
			}
		}

		// Internal segments are cubic
		for (int i = 2; i < ps.size()-1; ++i) {
			Vec3d p0 = ps.get(i-1);
			Vec3d p1 = ps.get(i);

			segDiffTemp.sub3(p0, p1);
			double segLength = segDiffTemp.mag3();

			Vec3d c0 = new Vec3d(p0);
			scaledTanTemp.scale3(segLength/3.0, tangents.get(i-2));
			c0.add3(scaledTanTemp);

			Vec3d c1 = new Vec3d(p1);
			scaledTanTemp.scale3(segLength/3.0, tangents.get(i-1));
			c1.sub3(scaledTanTemp);

			for (int t = 0; t < NUM_SEGMENTS; ++t) {
				Vec3d curvePoint = solveCubicBezier(t*tInc, p0, p1, c0, c1);
				ret.add(curvePoint);
			}
		}

		// End with another quadratic segment
		{
			Vec3d p0 = ps.get(ps.size()-2);
			Vec3d p1 = ps.get(ps.size()-1);

			segDiffTemp.sub3(p0, p1);
			double segLength = segDiffTemp.mag3();

			Vec3d c = new Vec3d(p0);
			scaledTanTemp.scale3(segLength/2.0, tangents.get(tangents.size()-1));
			c.add3(scaledTanTemp);

			for (int t = 0; t < NUM_SEGMENTS; ++t) {
				Vec3d curvePoint = solveQuadraticBezier(t*tInc, p0, p1, c);
				ret.add(curvePoint);
			}
		}

		ret.add(ps.get(ps.size()-1));

		return ret;
	}

	// Solve a generic bezier with arbitrary control points
	private static Vec3d solveBezier(double t, int start, int end, ArrayList<Vec3d> controls) {
		// Termination case
		if (start == end) {
			return new Vec3d(controls.get(start));
		}
		Vec3d a = solveBezier(t, start, end-1, controls);
		Vec3d b = solveBezier(t, start+1, end, controls);

		a.scale3(1-t);
		b.scale3(t);
		a.add3(b);

		return a;
	}

	// Solve a cubic bezier with explicit control points
	private static Vec3d solveCubicBezier(double s, Vec3d p0, Vec3d p1, Vec3d c0, Vec3d c1) {

		double oneMinS = 1 - s;
		double coeffP0 = oneMinS*oneMinS*oneMinS;
		double coeffC0 = 3*s*oneMinS*oneMinS;
		double coeffC1 = 3*s*s*oneMinS;
		double coeffP1 = s*s*s;

		Vec3d lp0 = new Vec3d(p0);
		lp0.scale3(coeffP0);

		Vec3d lp1 = new Vec3d(p1);
		lp1.scale3(coeffP1);

		Vec3d lc0 = new Vec3d(c0);
		lc0.scale3(coeffC0);

		Vec3d lc1 = new Vec3d(c1);
		lc1.scale3(coeffC1);

		Vec3d ret = new Vec3d();
		ret.add3(lp0);
		ret.add3(lp1);
		ret.add3(lc0);
		ret.add3(lc1);

		return ret;

	}

	// Solve a quadratic bezier with an explicit control point
	private static Vec3d solveQuadraticBezier(double s, Vec3d p0, Vec3d p1, Vec3d c) {

		double oneMinS = 1 - s;

		double coeffP0 = oneMinS*oneMinS;
		double coeffC = 2*s*oneMinS;
		double coeffP1 = s*s;

		Vec3d lp0 = new Vec3d(p0);
		lp0.scale3(coeffP0);

		Vec3d lp1 = new Vec3d(p1);
		lp1.scale3(coeffP1);

		Vec3d lc = new Vec3d(c);
		lc.scale3(coeffC);

		Vec3d ret = new Vec3d();
		ret.add3(lp0);
		ret.add3(lp1);
		ret.add3(lc);

		return ret;

	}

}
