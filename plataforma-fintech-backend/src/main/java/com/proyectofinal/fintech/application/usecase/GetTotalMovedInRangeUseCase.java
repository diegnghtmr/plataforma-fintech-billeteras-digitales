package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.RangeTotalView;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.model.TransactionType;
import com.proyectofinal.fintech.domain.port.TransactionRepository;

import java.time.Instant;

/**
 * Use case: returns the total amount and count of SUCCESSFUL transactions within a date range.
 * Excludes REVERSED status and EXTERNAL_TRANSFER_RECEIVED type.
 * Plain class — ZERO Spring/Jakarta/infrastructure imports.
 */
public class GetTotalMovedInRangeUseCase {

    private final TransactionRepository transactionRepository;

    public GetTotalMovedInRangeUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Returns totals for SUCCESSFUL, non-excluded transactions within [from, to].
     *
     * @param from start of range (inclusive)
     * @param to   end of range (inclusive)
     * @return RangeTotalView with totalAmount, count, from, to
     * @throws BusinessRuleException if from is after to
     */
    public RangeTotalView execute(Instant from, Instant to) {
        if (from.isAfter(to)) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                    "'from' must not be after 'to'");
        }

        double totalAmount = 0.0;
        int count = 0;

        for (Transaccion tx : transactionRepository.findAll()) {
            if (tx.getStatus() != TransactionStatus.SUCCESSFUL) continue;
            if (tx.getType() == TransactionType.EXTERNAL_TRANSFER_RECEIVED) continue;
            Instant ts = tx.getTimestamp();
            if (ts.isBefore(from) || ts.isAfter(to)) continue;
            totalAmount += tx.getAmount();
            count++;
        }

        return new RangeTotalView(totalAmount, count, from.toString(), to.toString());
    }
}
