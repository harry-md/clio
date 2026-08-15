package com.harry.clio.mapper;

import com.harry.clio.dto.subscription.SubscriptionPlanResponse;
import com.harry.clio.model.SubscriptionPlan;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionPlanMapper {
    SubscriptionPlanResponse toDto(SubscriptionPlan plan);
}
