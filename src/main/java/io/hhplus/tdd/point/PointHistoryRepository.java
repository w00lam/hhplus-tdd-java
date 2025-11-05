package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepository {
    private final PointHistoryTable pointHistoryTable;

    public List<PointHistory> selectAllByUserIdByUpdateMillisDesc(long userId){
        return pointHistoryTable.selectAllByUserId(userId)
                .stream()
                .sorted(Comparator.comparingLong(PointHistory::id).reversed()).toList();
    }
}
