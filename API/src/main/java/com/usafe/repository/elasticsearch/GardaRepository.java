package com.usafe.repository.elasticsearch;

import com.usafe.entity.elasticsearch.GardaStation;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GardaRepository extends ElasticsearchRepository<GardaStation, String> {
}
