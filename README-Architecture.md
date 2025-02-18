# Lucky Draw System Architecture

## Overview
The lucky draw system is designed with a layered architecture focusing on:
- Clean separation of concerns
- Efficient reward selection algorithm
- Location-based filtering
- Thread safety for concurrent access
- Performance optimization for large numbers of spins

## Entity Relationships
```plantuml
!include src/main/resources/diagram/entities.puml
```
Located in `src/main/resources/diagram/entities.puml`

The system contains these main entities:
- **User**: Participants in lucky draw events
- **Event**: Lucky draw events with time periods
- **Reward**: Prizes that can be won, with location restrictions
- **LuckyDrawResult**: Records of spins and wins
- **GoldenHour**: Special periods with modified win rates

## Service Layer
```plantuml
!include src/main/resources/diagram/services.puml
```
Located in `src/main/resources/diagram/services.puml`

Key services include:
- **RewardSelectionService**: Core logic for selecting rewards using Fisher-Yates Reservoir Sampling
- **EventService**: Manages active events and their lifecycle
- **RewardService**: Handles reward inventory and validation
- **LuckyDrawResultService**: Records spin results and history
- **GoldenHourService**: Manages special promotion periods

## Selection Flow
```plantuml
!include src/main/resources/diagram/reward_selection_flow.puml
```
Located in `src/main/resources/diagram/reward_selection_flow.puml`

The reward selection process:
1. Validate event and user
2. Filter rewards by location
3. Perform reservoir sampling to select positions
4. Generate random spin
5. Check for win and record result

## Algorithm Details

### Fisher-Yates Reservoir Sampling
Used for efficient position selection:
- O(K) memory usage where K is total rewards
- Uniform random distribution
- No need to store all positions
- Efficient for large numbers of spins

### Location Filtering
Optimized by:
- Early filtering before position assignment
- Prevents wasting memory on inaccessible rewards
- Improves overall performance

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

See `RewardSelectionServicePerformanceTest` for benchmark results.