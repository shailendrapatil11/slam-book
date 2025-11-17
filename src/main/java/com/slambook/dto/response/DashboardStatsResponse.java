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
class DashboardStatsResponse {
    private Integer totalColleges;
    private Integer activeColleges;
    private Integer totalUsers;
    private Integer totalSlamBookEntries;
    private Integer pendingJoinRequests;
    private Map<String, Integer> subscriptionBreakdown;
    private List<RecentActivityResponse> recentActivities;
}
