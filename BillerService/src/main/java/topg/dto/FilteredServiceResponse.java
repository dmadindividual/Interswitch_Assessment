package topg.dto;

import java.util.List;

public record FilteredServiceResponse(String response_description, List<FilteredService> content) {}

