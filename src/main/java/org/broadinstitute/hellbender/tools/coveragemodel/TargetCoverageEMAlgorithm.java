package org.broadinstitute.hellbender.tools.coveragemodel;

import com.google.common.annotations.VisibleForTesting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadinstitute.hellbender.tools.coveragemodel.interfaces.TargetCoverageEMAlgorithmCoreRoutines;
import org.broadinstitute.hellbender.utils.Utils;

/**
 * Implementation of the maximum likelihood estimator of {@link TargetCoverageModelBlock} parameters
 * via the EM algorithm (see CNV-methods.pdf for technical details).
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */

public abstract class TargetCoverageEMAlgorithm<V, M> implements TargetCoverageEMAlgorithmCoreRoutines<V, M> {

    private final Logger logger = LogManager.getLogger(TargetCoverageEMAlgorithm.class);

    protected final TargetCoverageEMParams params;

    protected final TargetCoverageEMWorkspace<V, M> ws;

    protected EMAlgorithmStatus status;

    public enum EMAlgorithmStatus {
        TBD(false, "Status is not determined yet."),
        SUCCESS_LIKELIHOOD(true, "Success -- converged in likelihood change tolerance."),
        SUCCESS_PARAMS(true, "Success -- converged in parameters change tolerance."),
        FAILURE_MAX_ITERS_REACHED(false, "Failure -- maximum iterations reached."),
        FAILURE_PSI_TOL(false, "Failure -- M-step iterations for Psi not converged."),
        FAILURE_W_TOL(false, "Failure -- M-step iterations for W not converged.");

        final boolean success;
        final String message;

        EMAlgorithmStatus(final boolean success, final String message) {
            this.success = success;
            this.message = message;
        }
    }

    public EMAlgorithmStatus getStatus() { return status; }

    public TargetCoverageEMAlgorithm(final TargetCoverageEMParams params,
                                     final TargetCoverageEMWorkspace<V, M> ws) {
        this.params = Utils.nonNull(params, "Target coverage EM algorithm parameters can not be null.");
        this.ws = Utils.nonNull(ws, "Target covarge EM workspace can not be null.");

        this.status = EMAlgorithmStatus.TBD;
        logger.info("EM algorithm initialized.");
    }

    @VisibleForTesting
    public void performEStep() {

    }

    @VisibleForTesting
    public void performMStep() {

    }

}