package dev.mikita.issueservice.dto.request;

import dev.mikita.issueservice.entity.IssueStatus;
import lombok.Data;
import lombok.NonNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.beans.ConstructorProperties;
import java.time.LocalDate;
import java.util.List;

@Data
public class GetIssuesInSquareRequestDto {
    private List<IssueStatus> statuses;
    private List<Long> categories;
    private LocalDate from;
    private LocalDate to;
    private Double minLongitude;
    private Double minLatitude;
    private Double maxLongitude;
    private Double maxLatitude;
    private Boolean coordinatesOnly;

    @ConstructorProperties({"statuses","categories","from","to","min_longitude","min_latitude","max_longitude","max_latitude","coordinates_only"})
    public GetIssuesInSquareRequestDto(List<IssueStatus> statuses,
                                       List<Long> categories,
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                       @NonNull Double minLongitude,
                                       @NonNull Double minLatitude,
                                       @NonNull Double maxLongitude,
                                       @NonNull Double maxLatitude,
                                       Boolean coordinatesOnly) {
        this.statuses = statuses;
        this.categories = categories;
        this.from = from;
        this.to = to;
        this.minLongitude = minLongitude;
        this.minLatitude = minLatitude;
        this.maxLongitude = maxLongitude;
        this.maxLatitude = maxLatitude;
        this.coordinatesOnly = coordinatesOnly != null && coordinatesOnly;
    }
}
