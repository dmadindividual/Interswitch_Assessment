package topg.dto;

public record FilteredService(
        String serviceID,
        String name,
        String minimum_amount,
        String maximum_amount,
        String convinience_fee,
        String product_type,
        String image
) {}
