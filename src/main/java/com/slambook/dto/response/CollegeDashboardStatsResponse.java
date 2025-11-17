package com.slambook.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CollegeDashboardStatsResponse {
    private Integer totalUsers;
    private Integer activeUsers;
    private Integer pendingJoinRequests;
    private Integer totalSlamBookEntries;
    private Integer todayEntries;
    private Integer thisWeekEntries;
    private Map<String, Integer> usersByBatch;
    private List<TopContributorResponse> topContributors;
}
