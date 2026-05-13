package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.RankingItem;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.structures.ArbolBST;

import java.util.ArrayList;
import java.util.List;

/**
 * Use case: retrieve the top-N users by points in descending order.
 * Plain class — ZERO Spring/Jakarta imports.
 *
 * ADR-7.2: Ranking uses ArbolBST with a reversed comparator (highest points = "smallest" node),
 * so inOrder() traversal yields descending-by-points order. Tie-break: userId ASC.
 */
public class GetPointsRankingUseCase {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;
    private static final int MIN_LIMIT = 1;

    private final UserRepository userRepository;

    public GetPointsRankingUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Computes the ranking for the given limit.
     *
     * @param limit number of items to return (1..100)
     * @return list of RankingItem sorted by points DESC, userId ASC tie-break
     * @throws BusinessRuleException if limit is outside [1, 100]
     */
    public List<RankingItem> execute(int limit) {
        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                    "limit must be between " + MIN_LIMIT + " and " + MAX_LIMIT + "; got: " + limit);
        }

        ArbolBST<RankingNode> bst = new ArbolBST<>();
        for (Usuario user : userRepository.findAll()) {
            bst.insert(new RankingNode(user.getId(), user.getName(), user.getPoints(), user.getLoyaltyLevel()));
        }

        // inOrder() on reversed comparator yields descending-points order
        List<RankingItem> result = new ArrayList<>();
        int position = 1;
        for (RankingNode node : bst.inOrder()) {
            if (position > limit) break;
            result.add(new RankingItem(position, node.userId(), node.userName(), node.points(), node.loyaltyLevel()));
            position++;
        }

        return result;
    }

    /**
     * Internal node type for the BST.
     * Comparator: descending points (highest first); tie-break ascending userId.
     * With this reversed comparator, BST inOrder() traversal = points DESC.
     */
    private record RankingNode(String userId, String userName, double points,
                                com.proyectofinal.fintech.domain.model.LoyaltyLevel loyaltyLevel)
            implements Comparable<RankingNode> {

        @Override
        public int compareTo(RankingNode other) {
            // Primary: points DESC (higher points = "smaller" in BST order)
            int cmp = Double.compare(other.points, this.points);
            if (cmp != 0) return cmp;
            // Tie-break: userId ASC
            return this.userId.compareTo(other.userId);
        }
    }
}
