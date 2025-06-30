package topg.dto;

import java.util.List;

public record ServiceResponse(String response_description, List<Service> content) {}

