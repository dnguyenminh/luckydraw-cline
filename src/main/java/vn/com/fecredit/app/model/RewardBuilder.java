package vn.com.fecredit.app.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class RewardBuilder {
    private Long id;
    private String name;
    private String description;
    private Integer quantity;
    private Integer remainingQuantity;
    private Integer maxQuantityInPeriod;
    private Double probability;
    private String applicableProvinces;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Event event;
    private Set<GoldenHour> goldenHours = new HashSet<>();

    public RewardBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public RewardBuilder name(String name) {
        this.name = name;
        return this;
    }

    public RewardBuilder description(String description) {
        this.description = description;
        return this;
    }

    public RewardBuilder quantity(Integer quantity) {
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        return this;
    }

    public RewardBuilder maxQuantityInPeriod(Integer maxQuantityInPeriod) {
        this.maxQuantityInPeriod = maxQuantityInPeriod;
        return this;
    }

    public RewardBuilder probability(Double probability) {
        this.probability = probability;
        return this;
    }

    public RewardBuilder applicableProvinces(String applicableProvinces) {
        this.applicableProvinces = applicableProvinces;
        return this;
    }

    public RewardBuilder startDate(LocalDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public RewardBuilder endDate(LocalDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public RewardBuilder isActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public RewardBuilder event(Event event) {
        this.event = event;
        return this;
    }

    public RewardBuilder goldenHours(Set<GoldenHour> goldenHours) {
        this.goldenHours = goldenHours;
        return this;
    }

    public Reward build() {
        Reward reward = new Reward();
        reward.setId(id);
        reward.setName(name);
        reward.setDescription(description);
        reward.setQuantity(quantity);
        reward.setRemainingQuantity(remainingQuantity);
        reward.setMaxQuantityInPeriod(maxQuantityInPeriod);
        reward.setProbability(probability);
        reward.setApplicableProvinces(applicableProvinces);
        reward.setStartDate(startDate);
        reward.setEndDate(endDate);
        reward.setIsActive(isActive);
        reward.setEvent(event);
        reward.setGoldenHours(goldenHours);
        return reward;
    }

    public static RewardBuilder aReward() {
        return new RewardBuilder();
    }
}