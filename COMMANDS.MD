# Command Documentation

This document provides detailed information about the available commands, example usage, and expected output for the Hotel Room Booking Management System.

## Command Types

The application supports two main commands:

1. **Availability Command** - Check room availability for specific dates
2. **Search Command** - Find available rooms over a date range

## Availability Command

The Availability command checks room availability for a specific hotel, date (or date range), and room type.

### Syntax

```
Availability(hotelId, date, roomType)
Availability(hotelId, startDate-endDate, roomType)
```

Where:
- `hotelId` - Hotel identifier (e.g., "H1")
- `date` - Single date in format YYYYMMDD
- `startDate-endDate` - Date range in format YYYYMMDD-YYYYMMDD
- `roomType` - Room type code (e.g., "SGL", "DBL", "SUITE")

### Output Format

```
Hotel: <hotelId>, Room Type: <roomType>, Date Range: <dateRange>, Available Rooms: <count>
```

### Examples and Expected Output

#### Single Day Availability

```
Availability(H1, 20240901, SGL)
```
Output:
```
Hotel: H1, Room Type: SGL, Date Range: 20240901, Available Rooms: 2
```

#### Date Range Availability

```
Availability(H1, 20240902-20240904, SGL)
```
Output:
```
Hotel: H1, Room Type: SGL, Date Range: 20240902-20240904, Available Rooms: 0
```

#### Overbooking Scenario

```
Availability(H2, 20240902, DBL)
```
Output:
```
Hotel: H2, Room Type: DBL, Date Range: 20240902, Available Rooms: 0
```

#### Multiple Hotel Comparison

```
Availability(H1, 20250501, SGL)
```
Output:
```
Hotel: H1, Room Type: SGL, Date Range: 20250501, Available Rooms: 0
```

```
Availability(H2, 20250501, SGL)
```
Output:
```
Hotel: H2, Room Type: SGL, Date Range: 20250501, Available Rooms: 2
```

#### Holiday Period Availability

```
Availability(H1, 20241224-20241226, DBL)
```
Output:
```
Hotel: H1, Room Type: DBL, Date Range: 20241224-20241226, Available Rooms: 1
```

## Search Command

The Search command finds available rooms over a specified number of days starting from the current date.

### Syntax

```
Search(hotelId, daysAhead, roomType)
```

Where:
- `hotelId` - Hotel identifier (e.g., "H1")
- `daysAhead` - Number of days to search ahead (positive integer)
- `roomType` - Room type code (e.g., "SGL", "DBL", "SUITE")

### Output Format

```
(startDate1-endDate1, availabilityCount1), (startDate2-endDate2, availabilityCount2), ...
```

Where dates are in YYYYMMDD format and availability counts are integers representing the number of available rooms.

### Examples and Expected Output

#### Standard Search with Varying Availability

```
Search(H1, 15, SGL)
```
Output (example with current date being April 28, 2025):
```
(20250428-20250430, 2), (20250505-20250509, 2), (20250515-20250519, 2)
```

This shows SGL rooms are available with:
- 2 rooms for Apr 28-30
- 2 rooms for May 5-9
- 2 rooms for May 15-19
- (with gaps during periods when rooms are booked)

#### Search Showing Different Availability Levels

```
Search(H1, 30, DBL)
```
Output (example):
```
(20250428-20250509, 2), (20250516-20250527, 2)
```

This shows 2 DBL rooms available except for May 10-15 when they're all booked.

#### Search for Special Room Types

```
Search(H2, 15, SUITE)
```
Output (example):
```
(20250428-20250504, 1), (20250511-20250512, 1)
```

This shows the suite is available except during May 5-10.

#### Long-range Search

```
Search(H1, 365, SGL)
```
Output (partial example):
```
(20250428-20250430, 2), (20250505-20250509, 2), (20250515-20250519, 2), (20250526-20261230, 2)
```

This shows availability for the full year ahead, with consolidated date ranges.

## Error Handling

The application provides specific error messages for various scenarios:

### Non-existent Hotel

```
Availability(H3, 20240901, SGL)
```
Output:
```
Error: Hotel not found: H3
```

### Non-existent Room Type

```
Availability(H1, 20240901, SUITE)
```
Output:
```
Error: Room type 'SUITE' not found in hotel 'H1'
```

### Invalid Date Format

```
Availability(H1, 202409, SGL)
```
Output:
```
Error: Invalid date format. Expected YYYYMMDD
```

### Invalid Date Range

```
Availability(H1, 20240910-20240901, SGL)
```
Output:
```
Error: End date cannot be before start date
```

### Invalid Days Ahead Value

```
Search(H2, -5, DBL)
```
Output:
```
Error: Days ahead must be positive
```

### Invalid Command Format

```
Avail(H1, 20240901, SGL)
```
Output:
```
Error: Invalid command format
```