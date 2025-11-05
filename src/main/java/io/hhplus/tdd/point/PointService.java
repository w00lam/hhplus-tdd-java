package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {
    public final UserPointRepository userPointRepository;

    public UserPoint getUserPoint(long userId) {
        return userPointRepository.selectById(userId);
    }
}
