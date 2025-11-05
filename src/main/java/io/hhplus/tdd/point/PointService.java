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
}
