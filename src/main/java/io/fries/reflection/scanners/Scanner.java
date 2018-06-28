package io.fries.reflection.scanners;

import io.fries.reflection.metadata.ResourceMetadata;

import java.util.Set;

public interface Scanner {
	Set<ResourceMetadata> getResources();
}
