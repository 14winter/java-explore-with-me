package ru.practicum.ewm.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.enums.RequestStatus;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class EventRequestStatusUpdateRequest {
    @NotNull
    private List<Long> requestIds;
    @NotNull
    private RequestStatus status;
}
