package org.catalog.records;

public record Category(
        Long id,
        String name,
        Long parentId,
        String parentName
) {
}
