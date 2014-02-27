package com.github.hoqhuuep.islandcraft.worldgenerator.mosaic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class Poisson {
    private final double xSize;
    private final double zSize;
    private final double radius;
    private final double diameter;
    private final double maxQuadrance;

    public Poisson(final double xSize, final double zSize, final double radius) {
        this.xSize = xSize;
        this.zSize = zSize;
        this.radius = radius;
        diameter = 2.0 * radius;
        maxQuadrance = diameter * diameter;
    }

    public List<Site> generate(final Random random) {
        final List<Site> candidates = new ArrayList<Site>();
        final List<Site> sites = new ArrayList<Site>();
        final Grid<Site> grid = new Grid<Site>((int) Math.ceil(xSize / diameter), (int) Math.ceil(zSize / diameter));
        final Site firstSite = randomPoint(xSize, zSize, random);
        candidates.add(firstSite);
        sites.add(firstSite);
        gridAdd(grid, firstSite);
        while (!candidates.isEmpty()) {
            final Site candidate = candidates.remove(candidates.size() - 1);
            final RangeList rangeList = new RangeList();
            for (final Site neighbor : gridNeighbors(grid, candidate)) {
                if (neighbor != candidate) {
                    subtractPoint(rangeList, candidate, neighbor);
                }
            }
            while (!rangeList.isEmpty()) {
                final double angle = rangeList.random(random);
                final Site newSite = makePoint(candidate, angle);
                sites.add(newSite);
                candidates.add(newSite);
                newSite.parent = candidate;
                gridAdd(grid, newSite);
                subtractPoint(rangeList, candidate, newSite);
            }
        }
        firstSite.parent = sites.get(1);
        // Find Voronoi neighbors
        for (final Site s : sites) {
            Collections.sort(s.suspectNeighbors, new AngleComparator(s.parent, s));
            final Iterator<Site> iterator = s.suspectNeighbors.iterator();
            Site pa = iterator.next();
            A: while (iterator.hasNext()) {
                final Site pb = iterator.next();
                final Site cc = circumcenter(s, pa, pb);
                final double cq = absq(sub(s, cc));
                for (final Site pc : s.suspectNeighbors) {
                    if (pc != pa && pc != pb && absq(sub(pc, cc)) < cq) {
                        continue A;
                    }
                }
                s.neighbors.add(pb);
                s.polygon.addPoint((int) cc.x, (int) cc.z);
                pa = pb;
            }
            s.neighbors.add(s.parent);
            final Site cc = circumcenter(s, pa, s.parent);
            s.polygon.addPoint((int) cc.x, (int) cc.z);
        }

        return sites;
    }

    private class AngleComparator implements Comparator<Site> {
        private final Site base;
        private final Site zero;

        private AngleComparator(final Site base, final Site zero) {
            this.zero = zero;
            this.base = sub(base, zero);
        }

        public final int compare(final Site p1, final Site p2) {
            return Double.compare(angle(base, sub(p1, zero)), angle(base, sub(p2, zero)));
        }
    }

    private Site circumcenter(final Site p1, final Site p2, final Site p3) {
        final double q1 = absq(p1);
        final double q2 = absq(p2);
        final double q3 = absq(p3);
        final Site s12 = sub(p1, p2);
        final Site s23 = sub(p2, p3);
        final Site s31 = sub(p3, p1);
        final double d = 0.5 / (p1.x * s23.z + p2.x * s31.z + p3.x * s12.z);
        final double cx = (q1 * s23.z + q2 * s31.z + q3 * s12.z) * d;
        final double cz = -(q1 * s23.x + q2 * s31.x + q3 * s12.x) * d;
        return new Site(cx, cz);
    }

    private double angle(final Site p1, final Site p2) {
        return (Math.atan2(cross(p1, p2), dot(p1, p2)) + (Math.PI * 2)) % (Math.PI * 2);
    }

    private Site sub(final Site p1, final Site p2) {
        return new Site(p1.x - p2.x, p1.z - p2.z);
    }

    private double absq(final Site p) {
        return p.x * p.x + p.z * p.z;
    }

    private double cross(final Site p1, final Site p2) {
        return p1.x * p2.z - p1.z * p2.x;
    }

    private double dot(final Site p1, final Site p2) {
        return p1.x * p2.x + p1.z * p2.z;
    }

    private List<Site> gridNeighbors(final Grid<Site> grid, final Site candidate) {
        int xMin = (int) Math.floor(candidate.x / diameter) - 1;
        int zMin = (int) Math.floor(candidate.z / diameter) - 1;
        int xMax = (int) Math.ceil(candidate.x / diameter);
        int zMax = (int) Math.ceil(candidate.z / diameter);
        return grid.getRegion(xMin, zMin, xMax, zMax);
    }

    private void gridAdd(final Grid<Site> grid, final Site point) {
        int xRow = (int) Math.floor(point.x / diameter);
        int zRow = (int) Math.floor(point.z / diameter);
        grid.add(xRow, zRow, point);
    }

    private Site makePoint(final Site candidate, final double angle) {
        final double x = candidate.x + Math.cos(angle) * radius;
        final double z = candidate.z + Math.sin(angle) * radius;
        return new Site((x + xSize) % xSize, (z + zSize) % zSize);
    }

    private void subtractPoint(final RangeList rangeList, final Site candidate, final Site point) {
        final double dx = ((point.x - candidate.x + 3.0 * xSize / 2.0) % xSize) - xSize / 2.0;
        final double dz = ((point.z - candidate.z + 3.0 * zSize / 2.0) % zSize) - zSize / 2.0;
        final double quadrance = dx * dx + dz * dz;
        if (quadrance < maxQuadrance) {
            final double distance = Math.sqrt(quadrance);
            final double angle = Math.atan2(dz, dx);
            final double theta = Math.acos(distance / diameter);
            rangeList.subtract(angle - theta, angle + theta);
            if (!candidate.suspectNeighbors.contains(point)) {
                candidate.suspectNeighbors.add(point);
            }
            if (!point.suspectNeighbors.contains(candidate)) {
                point.suspectNeighbors.add(candidate);
            }
        }
    }

    private Site randomPoint(final double xSize, final double zSize, final Random random) {
        return new Site(random.nextDouble() * xSize, random.nextDouble() * zSize);
    }
}