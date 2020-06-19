package com.usafe.repository.elasticsearch;

import com.usafe.entity.elasticsearch.Light;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LightRepository extends ElasticsearchRepository<Light, String> {
}
