package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
public class PointServiceTest {
    @InjectMocks
    private PointService pointService;
    @Mock
    private UserPointRepository userPointRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;

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

    @Test
    @DisplayName("존재하는 사용자 ID로 포인트 내역을 조회하면 최신순(내림차순)으로 반환한다")
    void givenExistentUserId_whenGetPointHistory_thenReturnList() {
        long userId = 1L;
        List<PointHistory> expected = List.of(
                new PointHistory(2L, userId, 500L, TransactionType.CHARGE, Instant.parse("2025-11-05T00:00:01Z").toEpochMilli()),
                new PointHistory(1L, userId, 500L, TransactionType.USE, Instant.parse("2025-11-05T00:00:00Z").toEpochMilli())
        );
        given(pointHistoryRepository.selectAllByUserIdByUpdateMillisDesc(userId)).willReturn(expected);

        List<PointHistory> result = pointService.getUserPointHistory(userId);

        assertEquals(expected, result);

        assertThat(result.get(0).id()).isEqualTo(expected.get(0).id());
        assertThat(result.get(1).id()).isEqualTo(expected.get(1).id());
        assertThat(result.get(0).userId()).isEqualTo(expected.get(0).userId());
        assertThat(result.get(0).type()).isEqualTo(expected.get(0).type());
        assertThat(result.get(1).type()).isEqualTo(expected.get(1).type());
        assertThat(result.get(0).updateMillis()).isEqualTo(expected.get(0).updateMillis());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 포인트 내역을 조회하면 빈 리스트를 반환한다")
    void givenNonExistentUserId_whenGetPointHistory_thenReturnEmptyList() {
        long userId = 1L;
        List<PointHistory> expected = List.of();
        given(pointHistoryRepository.selectAllByUserIdByUpdateMillisDesc(userId)).willReturn(expected);

        List<PointHistory> result = pointService.getUserPointHistory(userId);

        assertEquals(expected, result);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("포인트 충전이 이용내역(PointHistory)에 추가되는지 테스트한다.")
    void givenWhenChargePoint_thenAddPointHistory() {
        long userId = 1L;
        long use = 10000L;
        TransactionType type = TransactionType.CHARGE;
        long updateMillis = Instant.parse("2025-11-06T00:00:00Z").toEpochMilli();
        PointHistory history = new PointHistory(1L, userId, use, type, updateMillis);
        given(pointHistoryRepository.insert(userId, use, type, updateMillis)).willReturn(history);

        PointHistory result = pointHistoryRepository.insert(userId, use, type, updateMillis);

        then(pointHistoryRepository).should(times(1)).insert(userId, use, type, updateMillis);
        then(pointHistoryRepository).shouldHaveNoMoreInteractions();

        assertThat(result).isEqualTo(history);
    }

    @Test
    @DisplayName("포인트 충전에 성공한다.")
    void givenUserIdAndChargePoint_whenThenChargePoint() {
        long userId = 1L;
        long use = 10000L;
        UserPoint beforeCharge = new UserPoint(userId, 10000L, Instant.parse("2025-11-06T00:00:00Z").toEpochMilli());
        UserPoint afterCharge = new UserPoint(userId, 0L, Instant.parse("2025-11-06T00:00:00Z").toEpochMilli());
        given(userPointRepository.selectById(userId)).willReturn(beforeCharge);
        given(userPointRepository.insertOrUpdate(userId, beforeCharge.point() + use)).willReturn(afterCharge);

        UserPoint result = pointService.useUserPoint(userId, use);

        assertThat(result).isEqualTo(afterCharge);
        assertThat(result.point()).isEqualTo(afterCharge.point());
    }

    @Test
    @DisplayName("0이하의 포인트를 입력하면 충전에 실패하고 예외를 반환한다.")
    void givenExistentUserIdAndSmallerEqualThanZeroPoint_whenChargePoint_thenReturnException() {
        long userId = 1L;
        long amount = -10000L;
        UserPoint userPoint = new UserPoint(userId, 10000L, Instant.parse("2025-11-05T00:00:01Z").toEpochMilli());
        given(userPointRepository.selectById(userId)).willReturn(userPoint);

        assertThatThrownBy(() -> {
            pointService.chargeUserPoint(userId, amount);
        })
                .isInstanceOf(PointServiceException.class)
                .hasMessage("최소 충전 금액은 1원 이상입니다.");

        then(pointHistoryRepository).shouldHaveNoInteractions();
        then(userPointRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("포인트 사용이 이용내역(PointHistory)에 추가되는지 테스트한다.")
    void givenWhenUsePoint_thenAddPointHistory() {
        long userId = 1L;
        long use = 10000L;
        TransactionType type = TransactionType.USE;
        long updateMillis = Instant.parse("2025-11-06T00:00:00Z").toEpochMilli();
        PointHistory history = new PointHistory(1L, userId, use, type, updateMillis);
        given(pointHistoryRepository.insert(userId, use, type, updateMillis)).willReturn(history);

        PointHistory result = pointHistoryRepository.insert(userId, use, type, updateMillis);

        then(pointHistoryRepository).should(times(1)).insert(userId, use, type, updateMillis);
        then(pointHistoryRepository).shouldHaveNoMoreInteractions();

        assertThat(result).isEqualTo(history);
    }

    @Test
    @DisplayName("포인트 사용에 성공한다.")
    void givenUserIdAndUsePoint_whenThenUsePoint() {
        long userId = 1L;
        long use = 10000L;
        UserPoint beforeUse = new UserPoint(userId, 10000L, Instant.parse("2025-11-06T00:00:00Z").toEpochMilli());
        UserPoint afterUse = new UserPoint(userId, 0L, Instant.parse("2025-11-06T00:00:00Z").toEpochMilli());
        given(userPointRepository.selectById(userId)).willReturn(beforeUse);
        given(userPointRepository.insertOrUpdate(userId, beforeUse.point() - use)).willReturn(afterUse);

        UserPoint result = pointService.useUserPoint(userId, use);

        assertThat(result).isEqualTo(afterUse);
    }

    @Test
    @DisplayName("사용자의 포인트를 초과하여 사용할 시 예외를 반환한다.")
    void givenUserIdAndBiggerThanHavingUserPoint_whenUsePoint_ReturnException() {
        long userId = 1L;
        long use = 100000L;
        UserPoint userPoint = new UserPoint(userId, 10000L, Instant.parse("2025-11-06T00:00:00Z").toEpochMilli());
        given(userPointRepository.selectById(userId)).willReturn(userPoint);

        assertThatThrownBy(() -> {
            pointService.useUserPoint(userId, use);
        })
                .isInstanceOf(PointServiceException.class)
                .hasMessage("사용 가능한 포인트를 초과했습니다.\n" +
                        "현재 사용할 수 있는 포인트는 " + userPoint.point() + "원 입니다.");

        then(pointHistoryRepository).shouldHaveNoInteractions();
        then(userPointRepository).shouldHaveNoMoreInteractions();
    }
}
