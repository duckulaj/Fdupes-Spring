package com.hawkins.utils;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.hawkins.file.ExtendedFile;
import com.hawkins.paging.Paged;
import com.hawkins.paging.Paging;

public class PagingUtils {


	public static Page<ExtendedFile> findPaginated(Pageable pageable, List<ExtendedFile> duplicates) {

		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;
		List<ExtendedFile> list;

		if (duplicates.size() < startItem) {
			list = Collections.emptyList();
		} else {
			int toIndex = Math.min(startItem + pageSize, duplicates.size());
			list = duplicates.subList(startItem, toIndex);
		}

		Page<ExtendedFile> filePage
		= new PageImpl<ExtendedFile>(list, PageRequest.of(currentPage, pageSize), duplicates.size());

		return filePage;
	}

	public static Paged<ExtendedFile> getPage(int pageNumber, int size,  List<ExtendedFile> duplicates) {
		List<ExtendedFile> list;
		int startItem = pageNumber * size;

		if (duplicates.size() == 0) {
			list = Collections.emptyList();
		}
		else {
			if (duplicates.size() < startItem) {
				list = duplicates;
				
			} else {
				int toIndex = Math.min(startItem + size, duplicates.size());
				list = duplicates.subList(startItem, toIndex);
			}
		}

		Page<ExtendedFile> filePage
		= new PageImpl<ExtendedFile>(list, PageRequest.of(pageNumber, size), duplicates.size());
		return new Paged<>(filePage, Paging.of(filePage.getTotalPages(), pageNumber, size));
	}
}
