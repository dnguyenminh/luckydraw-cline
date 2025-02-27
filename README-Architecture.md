# Lucky Draw System Architecture

## Overview
The lucky draw system is designed with a layered architecture focusing on:
- Clean separation of concerns
- Efficient reward selection algorithm
- Location and province-based filtering
- Thread safety for concurrent access
- Performance optimization for large numbers of spins
- Daily spin limit management
- Golden hour multiplier system

## Entity Relationships
```plantuml
!include src/main/resources/diagram/entities.puml
```
Located in `src/main/resources/diagram/entities.puml`

The system contains these main entities:
- **User**: System users with role-based access
- **Event**: Lucky draw events with time periods
- **EventLocation**: Physical locations with spin quotas and daily limits
- **Participant**: Event participants with daily spin limits
- **Reward**: Prizes that can be won, with location and province restrictions
- **LuckyDrawResult**: Records of spins and wins with unique spin codes
- **GoldenHour**: Special periods with modified win rates

## Service Layer
```plantuml
!include src/main/resources/diagram/services.puml
```
Located in `src/main/resources/diagram/services.puml`

Key services include:
- **RewardSelectionService**: Core logic for selecting rewards using Fisher-Yates Reservoir Sampling
- **EventService**: Manages active events and their lifecycle
- **EventLocationService**: Handles location-specific spin quotas and daily limits
- **ParticipantService**: Manages participant registration and spin limits
- **RewardService**: Handles reward inventory and validation
- **LuckyDrawResultService**: Records spin results with unique spin codes
- **GoldenHourService**: Manages special promotion periods with win multipliers

## Key Workflows

### Reward Selection Flow
```plantuml
!include src/main/resources/diagram/reward_selection_flow.puml
```
Located in `src/main/resources/diagram/reward_selection_flow.puml`

### Participant Registration
```plantuml
!include src/main/resources/diagram/participant_registration_flow.puml
```
Located in `src/main/resources/diagram/participant_registration_flow.puml`

### Event Creation
```plantuml
!include src/main/resources/diagram/event_creation_flow.puml
```
Located in `src/main/resources/diagram/event_creation_flow.puml`

### Golden Hour Management
```plantuml
!include src/main/resources/diagram/golden_hour_management_flow.puml
```
Located in `src/main/resources/diagram/golden_hour_management_flow.puml`

### Daily Reset Process
```plantuml
!include src/main/resources/diagram/participant_daily_reset_flow.puml
```
Located in `src/main/resources/diagram/participant_daily_reset_flow.puml`

### Location Management
```plantuml
!include src/main/resources/diagram/event_location_management_flow.puml
```
Located in `src/main/resources/diagram/event_location_management_flow.puml`

## Algorithm Details

### Fisher-Yates Reservoir Sampling
Used for efficient position selection:
- O(K) memory usage where K is total rewards
- Uniform random distribution
- No need to store all positions
- Efficient for large numbers of spins
- Supports win rate modification during golden hours

### Location and Province Filtering
Optimized by:
- Early filtering before position assignment
- Two-level filtering: location then province
- Province caching for performance
- Prevents wasting memory on inaccessible rewards
- Improves overall performance

### Spin Code Generation
- Unique code per spin attempt
- Used for result verification
- Prevents duplicate submissions
- Enables result tracking

### Daily Limit Management
- Configurable per location and participant
- Automatic midnight reset
- Concurrent access protection
- Prevents limit bypassing

## Viewing the Diagrams

To view the PlantUML diagrams:
1. Install PlantUML plugin for your IDE
2. Or use online viewer at http://www.plantuml.com/plantuml/
3. Open the .puml files from src/main/resources/diagram/

## Performance Considerations

The system is optimized for:
- Large numbers of concurrent users
- Millions of spins per event
- Fast response times
- Memory efficiency
- Thread safety
- Concurrent daily resets
- High-volume golden hour periods

See `RewardSelectionServicePerformanceTest` and `EventLocationServiceConcurrencyTest` for benchmark results.