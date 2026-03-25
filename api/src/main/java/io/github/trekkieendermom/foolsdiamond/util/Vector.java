/*
 * Copyright (c) 2026 TrekkieEndermom
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.trekkieendermom.foolsdiamond.util;

import org.bukkit.Location;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class Vector {
    public static final Vector ZERO = new Vector(0, 0, 0);
    public static final Vector NaN = new Vector(Double.NaN, Double.NaN, Double.NaN);
    private final double x;
    private final double y;
    private final double z;
    private @Nullable Double length = null;

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Build a vector from its azimuthal coordinates.
     * @param yaw horizontal rotation in degrees
     * @param pitch vertical rotation in degrees
     */
    public Vector(double yaw, double pitch) {
        final double yawRad = Math.toRadians(yaw);
        final double pitchRad = Math.toRadians(pitch);

        final double xz = Math.cos(pitchRad);
        y = -Math.sin(pitchRad);
        x = -xz * Math.sin(yawRad);
        z = xz * Math.cos(yawRad);
    }

    public static Vector of(Location location) {
        return new Vector(location.x(), location.y(), location.z());
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    /**
     * Gets the precise length of this vector.
     * This is lazily initialized and will perform a costly calculation on the first call.
     * The result is then cached for subsequent calls.
     * @return the precise length
     */
    public double getLength() {
        if (length == null) {
            length = Math.sqrt(getLengthSquared());
        }
        return length;
    }

    public double getLengthSquared() {
        return (x * x) + (y * y) + (z * z);
    }

    public Vector add(Vector other) {
        return new Vector(x + other.x, y + other.y, z + other.z);
    }

    public Vector subtract(Vector other) {
        return new Vector(x - other.x, y - other.y, z - other.z);
    }

    public Vector multiply(Vector other) {
        return new Vector(x * other.x, y * other.y, z * other.z);
    }

    public Vector divide(Vector other) {
        return new Vector(x/other.x, y/other.y, z/other.z);
    }

    public Vector negate() {
        return new Vector(-x, -y, -z);
    }

    public Vector normalize() {
        return new Vector(x/getLength(), y/getLength(), z/getLength());
    }

    public boolean isNaN() {
        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z);
    }

    public boolean isInfinite() {
        return !isNaN() && (Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z));
    }

    public double angle(Vector other) {
        double dot = Math.clamp(dot(other) / (getLength() * other.getLength()), -1.0, 1.0);
        return Math.toDegrees(Math.acos(dot));
    }

    public double dot(Vector other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vector cross(Vector other) {
        return new Vector(y*other.z - other.y * z,
                z * other.x - other.z * x,
                x * other.y - other.x * y);
    }

    public static Vector getMinimum(Vector v1, Vector v2) {
        return new Vector(Math.min(v1.x, v2.x), Math.min(v1.y, v2.y), Math.min(v1.z, v2.z));
    }

    public static Vector getMaximum(Vector v1, Vector v2) {
        return new Vector(Math.max(v1.x, v2.x), Math.max(v1.y, v2.y), Math.max(v1.z, v2.z));
    }
}
