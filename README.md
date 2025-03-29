# OpenSearch Late Interaction Plugin

## Overview
The OpenSearch Late Interaction Plugin implements support for late interaction retrieval models like ColBERT and ColPali in OpenSearch. These models work by comparing multiple token-level vectors per document with query token vectors to produce a relevance score, enabling more precise and context-aware search results.

## Core Features

- **MaxSim Scoring**: Implements maximum similarity scoring between query and document token vectors
- **Multiple Vector Storage**: Supports storing and retrieving multiple vectors per document
- **Token Pooling**: Reduces vector count through token pooling techniques (roadmap)
- **Vector Quantization**: Compresses vectors to reduce storage requirements (roadmap)

## Implementation Approach

This plugin uses a two-stage retrieval approach:
1. **First Stage**: Standard ANN (Approximate Nearest Neighbor) search using pooled document vectors
2. **Second Stage**: MaxSim rescoring on the top-k results using token-level vectors

This approach balances efficiency with the precision benefits of late interaction models.

## Development Roadmap

### Phase 1: Core Infrastructure
- Basic plugin structure and integration with OpenSearch
- Simple MaxSim rescorer implementation
- Tests and documentation

### Phase 2: Vector Storage and Retrieval
- Implement storage for multiple vectors per document
- Configure pooled vs. unpooled vector storage options
- Optimize vector retrieval performance

### Phase 3: Advanced Features
- Token pooling implementation
- Vector quantization for compression
- Performance optimizations

### Phase 4: Integration and Testing
- Integration with popular late interaction models
- Comprehensive performance testing
- Documentation and examples

### Phase 5: Production Readiness
- Performance tuning for large-scale deployments
- Security and stability improvements
- Advanced configuration options

## Installation

```bash
# Build the plugin
./gradlew build

# Install the plugin in OpenSearch
bin/opensearch-plugin install file:///path/to/opensearch-late-interaction-1.0.0.0.zip
```

## Usage Examples

### Index Configuration
```json
PUT /my-index
{
  "mappings": {
    "properties": {
      "text_field": { "type": "text" },
      "vector_field": {
        "type": "knn_vector",
        "dimension": 768,
        "method": {
          "name": "hnsw",
          "space_type": "innerproduct",
          "parameters": {
            "m": 16,
            "ef_construction": 200
          }
        }
      },
      "token_vectors": {
        "type": "token_vectors",
        "dimension": 768,
        "storage": "pooled_only"  // or "both" for pooled and unpooled
      }
    }
  }
}
```

### Search with MaxSim Rescoring
```json
GET /my-index/_search
{
  "size": 10,
  "query": {
    "knn": {
      "vector_field": {
        "vector": [0.1, 0.2, ...],
        "k": 100
      }
    }
  },
  "rescore": {
    "window_size": 100,
    "query": {
      "rescore_query": {
        "maxsim": {
          "query_vectors": [[0.1, 0.2, ...], [0.3, 0.4, ...], ...],
          "field": "token_vectors",
          "similarity": "dot_product"
        }
      },
      "rescore_query_weight": 1.0
    }
  }
}
```

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.