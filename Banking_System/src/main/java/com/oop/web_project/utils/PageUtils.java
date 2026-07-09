package com.oop.web_project.utils;

import com.oop.web_project.dto.requests.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtils {

    /**
     * Method builds pageable instance for the usage of pagination
     * @param pageRequest information for building pageable
     * @return Pageable instance
     */
    public static Pageable buildPageable(PageRequest pageRequest) {
        Sort sort = pageRequest.getSortDirection().equalsIgnoreCase("desc") ?
                Sort.by(pageRequest.getSortBy()).descending() :
                Sort.by(pageRequest.getSortBy()).ascending();

       return org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
    }

}
