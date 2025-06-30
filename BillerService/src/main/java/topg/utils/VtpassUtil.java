package topg.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class VtpassUtil {

    public static String generateRequestId() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Africa/Lagos"));
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return timestamp + random;
    }
}

