package com.slambook.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeStatsResponse {
    private Integer totalUsers;
    private Integer activeUsers;
    private Integer totalSlamBookEntries;
    private Integer pendingJoinRequests;
}
