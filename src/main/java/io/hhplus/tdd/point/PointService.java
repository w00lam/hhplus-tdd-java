package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    public final UserPointRepository userPointRepository;
    public final PointHistoryRepository pointHistoryRepository;

    public UserPoint getUserPoint(long userId) {
        return userPointRepository.selectById(userId);
    }

    public List<PointHistory> getUserPointHistory(long userId) {
        return pointHistoryRepository.selectAllByUserIdByUpdateMillisDesc(userId);
    }

    public UserPoint chargeUserPoint(long userId, long amount) {
        UserPoint userPoint = getUserPoint(userId);

        userPoint.validateCharge(amount);

        pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return userPointRepository.insertOrUpdate(userId, userPoint.point() + amount);
    }

    public UserPoint useUserPoint(long userId, long amount) {
        UserPoint userPoint = getUserPoint(userId);

        userPoint.validateUse(amount);

        pointHistoryRepository.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

        return userPointRepository.insertOrUpdate(userId, userPoint.point() - amount);
    }
}
