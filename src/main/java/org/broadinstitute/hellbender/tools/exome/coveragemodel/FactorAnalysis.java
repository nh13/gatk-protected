package org.broadinstitute.hellbender.tools.exome.coveragemodel;

import org.apache.commons.math3.linear.*;
import org.broadinstitute.hellbender.utils.param.ParamUtils;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Given N samples of D-dimensional data, represented as an NxD matrix X, where each measurement
 * X_ij is uncertain with variance S_ij, learn the following generative model:
 *
 * Each sample i is associated with a latent K-dimensional vector z_i (K << D) with isotropic Gaussian prior:
 * z_i ~ N(0, I)
 *
 * All samples share a common mean m and DxK matrix W that maps from K-dimensional latent space to the observed space.
 *
 * Measurements are taken from a normal distribution centered at the mapping from latent space:
 *
 * X_i ~ N(Wz_i + m, Psi)
 *
 * where Psi is a DxD diagonal matrix representing residual variance not explained by the latent variable z.
 *
 * We learn m, W, and Psi via the EM algorithm
 *
 * See CNV_methods.pdf for derivation of equations.
 * @author David Benjamin &lt;davidben@broadinstitute.org&gt;
 */
public class FactorAnalysis {
    final int D;
    final int K;

    // the DxK matrix that maps from latent space to (variations in) observed space
    RealMatrix W;

    // cache the transpose
    RealMatrix Wtranspose;

    // the D-dimensional mean vector in observed space i.e. the expected value of observations when latent variables are zero
    RealVector m;

    // the residual variance, a DxD diagonal matrix
    DiagonalMatrix Psi;

    // the KxK identity matrix
    RealMatrix I;

    final static int RANDOM_SEED = 13;
    final Random rng = new Random(RANDOM_SEED);

    /**
     *
     * @param D dimension of observations
     * @param K dimension of latent space
     */
    public FactorAnalysis(final int D, final int K) {
        this.D = ParamUtils.isPositive(D, "Dimensionality of observations must be 1 or greater.");
        this.K = ParamUtils.isPositive(K, "Dimensionality of latent space must be 1 or greater.");
        ParamUtils.isPositiveOrZero(D-K, "Cannot have more latent dimensions than observed dimensions.");

        I = MatrixUtils.createRealIdentityMatrix(K);
        initializeParametersRandomly();

    }

    /**
     * learn parameters W, m, and Psi via expectation-maximization
     *
     * @param X NxD data matrix of observations
     * @param S NxD matrix of variance of observations -- variance of observation X_ij is S_ij
     */
    public void learn(final RealMatrix X, final RealMatrix S) {
        //TODO: check S is all positive

    }

    /*
     * Latent variables z are O(1) so each entry in the vector Wz is the inner product of K entries of W with K
     * entries of z.  If K is also O(1) this is a sum of K random-ish quantities that are all O(1), which has typical size
     * sqrt(K).  To get an O(1) result we thus need to initialize entries of W randomly with typical size 1/sqrt(K).
     *
     * We also need m and Psi to have O(1) entries for an O(1) result.
     */
    private void initializeParametersRandomly() {
        W = new Array2DRowRealMatrix(D, K);
        final double scale = 1/Math.sqrt(K);
        W.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            @Override
            public double visit(final int row, final int column, final double value) { return scale*rng.nextGaussian(); }
        });
        m = new ArrayRealVector(randomArray(D));
        Psi = new DiagonalMatrix(randomArray(D));
    }

    private double[] randomArray(final int size) {
        ParamUtils.isPositive(size, "Must have size > 0");
        return IntStream.range(0, D).mapToDouble(n -> rng.nextGaussian()).toArray();
    }


    public expectationStepResult expectationStepForOneSample(final RealVector observed, final RealVector variance) {
        if (observed.getDimension() != D) {
            throw new IllegalArgumentException(String.format("Input observed values must have length %d.", D));
        }

        final DiagonalMatrix sampleVariance = new DiagonalMatrix(variance.toArray());
        final DiagonalMatrix totalVariance = Psi.add(sampleVariance);
        final DiagonalMatrix totalPrecision = totalVariance.inverse();

        //the following code implements G_i = (I + W^T (Psi+S_i)^(-1) W)^(-1)
        //this matrix is KxK so all operations are cheap.
        final RealMatrix G = new LUDecomposition(I.add(Wtranspose.multiply(totalPrecision).multiply(W))).getSolver().getInverse();

        //difference between observed values and global mean m
        final RealVector residual = observed.subtract(m);
        final RealVector firstMoment = G.multiply(Wtranspose).multiply(totalPrecision).operate(residual);
        final RealMatrix secondMoment = G.add(firstMoment.outerProduct(firstMoment));

        return new expectationStepResult(firstMoment, secondMoment);
    }

    /**
     * The E step of the EM optimization outputs the K-dimensional first moment vector E[z_i] and the KxK
     * second moment matrix E[z_i z_i^T] of each sample's latent vector with respect to its Gaussian posterior
     *
     * This class encapsulates these moments
     */
    private static final class expectationStepResult {
        public RealVector firstMoment;
        public RealMatrix secondMoment;

        public expectationStepResult(RealVector firstMoment, RealMatrix secondMoment) {
            this.firstMoment = firstMoment;
            this.secondMoment = secondMoment;
        }
    }
}
