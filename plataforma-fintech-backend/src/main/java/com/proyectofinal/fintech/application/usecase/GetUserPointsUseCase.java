package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.PointsView;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;

/**
 * Use case: retrieve current points and loyalty level for a user.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class GetUserPointsUseCase {

    private final UserRepository userRepository;

    public GetUserPointsUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns a {@link PointsView} for the given user.
     *
     * @param userId the user identifier
     * @return PointsView with current points and loyalty level
     * @throws NotFoundException if the user does not exist
     */
    public PointsView execute(String userId) {
        Usuario user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User with id=" + userId + " not found"));

        return new PointsView(user.getId(), user.getPoints(), user.getLoyaltyLevel());
    }
}
