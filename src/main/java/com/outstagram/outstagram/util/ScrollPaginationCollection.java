//package com.outstagram.outstagram.util;
//
//import lombok.RequiredArgsConstructor;
//
//import java.util.List;
//
//@RequiredArgsConstructor
//public class ScrollPaginationCollection<T> {
//    private final List<T> itemsWithNextCursor; // 현재 스크롤의 요소 + 다음 스크롤 요소 1개(다음 스크롤 있는지 확인하기 위해 포함)
//    private final int countPerScroll;  // 1번 조회할 때 가져올 데이터 개수
//
//    public static <T> ScrollPaginationCollection<T> of(List<T> itemsWithNextCursor, int size) {
//        return new ScrollPaginationCollection<>(itemsWithNextCursor, size);
//    }
//
//    public boolean isLastScroll() {
//        // 가져온 개수가 1회 조회시 가져올 개수이하이면 마지막 스크롤이라고 판단
//        return this.itemsWithNextCursor.size() <= countPerScroll;
//    }
//
//    public List<T> getCurrentScrollItems() {
//        if (isLastScroll()) {
//            return this.itemsWithNextCursor;
//        }
//        return this.itemsWithNextCursor.subList(0, countPerScroll);
//    }
//
//    public T getNextCursor() {
//        return itemsWithNextCursor.get(countPerScroll - 1);
//    }
//}
