package com.wedit.backend.api.vendor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
	private Long basePrice;
	private Long vendorId;
	private String vendorName;
	private Double averageRating;
	private Integer reviewCount;
	private String logoMediaUrl;
    private String fullAddress;
    private String addressDetail;
    private Double latitude;
    private Double longitude;
}
