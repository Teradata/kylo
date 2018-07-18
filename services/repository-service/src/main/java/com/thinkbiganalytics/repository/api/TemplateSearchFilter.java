package com.thinkbiganalytics.repository.api;

import java.util.Comparator;

public class TemplateSearchFilter {
    Integer limit = 0;
    Integer start = 0;
    String sort;

    public TemplateSearchFilter(String sort, Integer limit, Integer start) {
        this.sort = sort;
        this.limit = limit;
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public static enum TemplateComparator {
        NAME("name", Comparator.comparing(RepositoryItem::getTemplateName)),
        REPOSITORY_TYPE("repositoryType", Comparator
            .comparing(r -> r.getRepository().getType().getKey()));

        private Comparator<RepositoryItem> comparator;
        private String key;

        TemplateComparator(String key, Comparator<RepositoryItem> comparator){
            this.key = key;
            this.comparator = comparator;
        }

        public String getKey() { return this.key; }

        public Comparator<RepositoryItem> getComparator() { return this.comparator; }
    }
}
