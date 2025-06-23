package org.devquality.web.dtos.core.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaginationInfo {
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;

    public static PaginationInfo create(int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);

        return PaginationInfo.builder()
                .currentPage(page)
                .pageSize(size)
                .totalElements(total)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .isFirst(page == 0)
                .isLast(page == totalPages - 1)
                .build();
    }
}