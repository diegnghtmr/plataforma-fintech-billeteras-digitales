package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.RouteMetric;
import com.proyectofinal.fintech.domain.port.TransferGraphRepository;
import com.proyectofinal.fintech.domain.structures.GrafoTransferencias;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns frequent transfer routes meeting minTransfers threshold.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class GetFrequentRoutesUseCase {

    private final TransferGraphRepository graphRepository;

    public GetFrequentRoutesUseCase(TransferGraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }

    /**
     * @param minTransfers minimum number of transfers on a route to be included
     * @return list of RouteMetric for routes meeting the threshold
     */
    public List<RouteMetric> execute(int minTransfers) {
        List<RouteMetric> result = new ArrayList<>();
        for (GrafoTransferencias.Route route : graphRepository.frequentRoutes(minTransfers)) {
            result.add(new RouteMetric(
                    route.source(),
                    route.target(),
                    route.count(),
                    route.totalAmount()
            ));
        }
        return result;
    }
}
