package topg.dto;

import java.util.List;

public record VariationContent(    String ServiceName,
                                   String serviceID,
                                   String convinience_fee,
                                   List<Variation> variations) {}
