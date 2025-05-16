// NewsWithVideosDto.java
package capston.new_valance.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NewsWithVideosDto {
    private Long newsId;
    private String title;
    private String originalUrl;
    private String thumbnailUrl;
    private List<VideoVersionDto> videoVersions;
    private boolean liked;
}
