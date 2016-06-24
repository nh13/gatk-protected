package org.broadinstitute.hellbender.tools.coveragemodel;

import org.broadinstitute.hellbender.utils.param.ParamUtils;

/**
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public final class TargetSpaceBlock {

    /* begin index inclusive, end index not inclusive */
    private final int begIndex, endIndex, numTargets;

    public TargetSpaceBlock(final int begIndex, final int endIndex) {
        this.begIndex = ParamUtils.isPositiveOrZero(begIndex, "The begin index of a target block must be non-negative.");
        this.endIndex = ParamUtils.inRange(endIndex, begIndex + 1, Integer.MAX_VALUE, "The target block must at least" +
                " contain one target.");
        numTargets = endIndex - begIndex;
    }

    public int getBegIndex() { return begIndex; }

    public int getEndIndex() { return endIndex; }

    public int getNumTargets() { return numTargets; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TargetSpaceBlock)) {
            return false;
        }

        final TargetSpaceBlock block = (TargetSpaceBlock) o;
        return begIndex == block.begIndex && endIndex == block.endIndex;
    }

    @Override
    public int hashCode() {
        int result = begIndex;
        result = 31 * result + endIndex;
        return result;
    }

    @Override
    public String toString() {
        return "[" + begIndex + ", " + endIndex + "]";
    }
}
