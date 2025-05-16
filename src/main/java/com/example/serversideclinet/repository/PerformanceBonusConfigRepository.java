package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.PerformanceBonusConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceBonusConfigRepository extends JpaRepository<PerformanceBonusConfig, Integer> {

    /**
     * Tìm tất cả cấu hình thưởng và sắp xếp theo ngưỡng điểm giảm dần
     *
     * @return Danh sách cấu hình thưởng
     */
    List<PerformanceBonusConfig> findAllByOrderByPointsThresholdDesc();

    /**
     * Tìm cấu hình thưởng có ngưỡng điểm lớn nhất nhỏ hơn hoặc bằng số điểm cung cấp
     *
     * @param points Số điểm
     * @return Cấu hình thưởng phù hợp
     */
    PerformanceBonusConfig findFirstByPointsThresholdLessThanEqualOrderByPointsThresholdDesc(Integer points);
}