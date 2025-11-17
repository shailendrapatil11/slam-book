package com.slambook.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeUpdateRequest {
    private String name;
    private String email;
    private String phone;
    private AddressRequest address;
    private String logo;
}
