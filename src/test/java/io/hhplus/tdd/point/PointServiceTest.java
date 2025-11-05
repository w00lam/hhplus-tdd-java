package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
public class PointServiceTest {
    @InjectMocks
    private PointService pointService;
    @Mock
    private UserPointRepository userPointRepository;

    /***
     * 특정 유저의 포인트를 조회하는 기능을 테스트한다
     */
    @Test
    @DisplayName("존재하는 사용자 ID로 포인트를 조회하면 해당 유저의 포인트를 반환한다")
    void givenExistentUserId_whenGetUserPoint_thenReturnUserPoint() {
        long userId = 1L;
        UserPoint expected = new UserPoint(userId, 10000L, Instant.parse("2025-08-15T00:00:00Z").toEpochMilli());
        given(userPointRepository.selectById(userId)).willReturn(expected);

        UserPoint result = pointService.getUserPoint(userId);

        assertEquals(expected, result);

        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(expected.point());
        assertThat(result.updateMillis()).isEqualTo(expected.updateMillis());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 포인트를 조회하면 0을 반환한다")
    void givenNonExistentUserId_whenGetUserPoint_thenReturnZero() {
        long userId = 1L;
        UserPoint expected = UserPoint.empty(userId);
        given(userPointRepository.selectById(userId)).willReturn(expected);

        UserPoint result = pointService.getUserPoint(userId);

        assertEquals(expected, result);

        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(expected.point());
        assertThat(result.updateMillis()).isEqualTo(expected.updateMillis());
    }
}
