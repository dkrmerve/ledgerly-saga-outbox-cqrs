# ElastiCache Redis (Design Only)

resource "aws_elasticache_cluster" "redis" {
  cluster_id           = "ledgerly-redis"
  engine               = "redis"
  node_type            = "cache.t3.micro"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"

  tags = {
    Service = "ledgerly"
    Role    = "dedup-lock"
  }
}

# Used for:
# - Deduplication store
# - Distributed locking
