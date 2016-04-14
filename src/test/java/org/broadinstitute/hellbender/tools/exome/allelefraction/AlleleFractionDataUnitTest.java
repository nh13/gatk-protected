package org.broadinstitute.hellbender.tools.exome.allelefraction;

import org.broadinstitute.hellbender.tools.exome.AllelicCount;
import org.broadinstitute.hellbender.tools.exome.Genome;
import org.broadinstitute.hellbender.tools.exome.SegmentedGenome;
import org.broadinstitute.hellbender.utils.SimpleInterval;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Benjamin
 */
public final class AlleleFractionDataUnitTest {

    @Test
    public void testData() {
        final List<AllelicCount> ac = new ArrayList<>();
        final List<SimpleInterval> segments= new ArrayList<>();

        // segment 0: hets 0-2
        segments.add(new SimpleInterval("chr", 1, 5));
        ac.add(new AllelicCount(new SimpleInterval("chr", 1, 1), 0, 5));
        ac.add(new AllelicCount(new SimpleInterval("chr", 2, 2), 5, 0));
        ac.add(new AllelicCount(new SimpleInterval("chr", 3, 3), 5, 5));

        // segment 1: hets 3-4
        segments.add(new SimpleInterval("chr", 10, 15));
        ac.add(new AllelicCount(new SimpleInterval("chr", 10, 10), 1, 1));
        ac.add(new AllelicCount(new SimpleInterval("chr", 11, 11), 2, 2));

        final Genome genome = new Genome(new ArrayList<>(), ac, "SAMPLE");
        final SegmentedGenome segmentedGenome = new SegmentedGenome(segments, genome);

        final AlleleFractionData dc = new AlleleFractionData(segmentedGenome);
        Assert.assertEquals(dc.numSegments(), 2);
        Assert.assertEquals(dc.refCount(0), 0);
        Assert.assertEquals(dc.altCount(0), 5);
        Assert.assertEquals(dc.readCount(2), 10);
        Assert.assertEquals(dc.readCount(3), 2);
        Assert.assertEquals(dc.refCount(4), 2);
        Assert.assertEquals(dc.altCount(4), 2);

        Assert.assertEquals(dc.count(0).getAltReadCount(), 5);
        Assert.assertEquals(dc.count(1).getAltReadCount(), 0);
        Assert.assertEquals(dc.count(3).getRefReadCount(), 1);
        Assert.assertEquals(dc.count(4).getRefReadCount(), 2);

        Assert.assertEquals(dc.countsInSegment(0).get(1).getRefReadCount(), 5);
        Assert.assertEquals(dc.countsInSegment(0).get(1).getAltReadCount(), 0);

        final List<Integer> hetsInSegment0 = dc.hetsInSegment(0);
        Assert.assertEquals(hetsInSegment0.size(), 3);
        Assert.assertEquals((int) hetsInSegment0.get(0), 0);
        Assert.assertEquals((int) hetsInSegment0.get(2), 2);

        final List<Integer> hetsInSegment1 = dc.hetsInSegment(1);
        Assert.assertEquals(hetsInSegment1.size(), 2);
        Assert.assertEquals((int) hetsInSegment1.get(0), 3);
        Assert.assertEquals((int) hetsInSegment1.get(1), 4);
    }
}