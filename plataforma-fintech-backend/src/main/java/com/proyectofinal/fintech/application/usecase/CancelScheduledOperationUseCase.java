package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.OperacionProgramada;
import com.proyectofinal.fintech.domain.model.ScheduledOperationStatus;
import com.proyectofinal.fintech.domain.port.ScheduledOperationRepository;

/**
 * Use case: cancel a scheduled operation.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class CancelScheduledOperationUseCase {

    private final ScheduledOperationRepository operationRepository;

    public CancelScheduledOperationUseCase(ScheduledOperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    /**
     * Cancels the scheduled operation identified by {@code operationId}.
     *
     * @throws NotFoundException     if the operation does not exist (OPERATION_NOT_FOUND)
     * @throws BusinessRuleException if the operation is not in PENDING status (VALIDATION_ERROR)
     */
    public OperacionProgramada execute(String operationId) {
        OperacionProgramada op = operationRepository.findById(operationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.OPERATION_NOT_FOUND,
                        "Scheduled operation not found: " + operationId));

        if (op.getStatus() != ScheduledOperationStatus.PENDING) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                    "Cannot cancel operation with status: " + op.getStatus());
        }

        op.markCancelled();
        operationRepository.save(op);
        return op;
    }
}
