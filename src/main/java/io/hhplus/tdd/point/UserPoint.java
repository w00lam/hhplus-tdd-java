package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public void validatePoint(long amount) {
        if (amount < 0) {
            throw new PointServiceException("최소 충전 금액은 1원 이상입니다.");
        }
    }
}
