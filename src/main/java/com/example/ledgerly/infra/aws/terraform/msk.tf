# Amazon MSK (Kafka) - Design Only

resource "aws_msk_cluster" "ledgerly" {
  cluster_name           = "ledgerly-msk"
  kafka_version          = "3.6.0"
  number_of_broker_nodes = 3

  broker_node_group_info {
    instance_type   = "kafka.m5.large"
    client_subnets  = ["subnet-private-a", "subnet-private-b"]
    security_groups = ["sg-msk"]
  }

  tags = {
    Service = "ledgerly"
    Role    = "event-streaming"
  }
}

# Notes:
# - Partitioning by orderId
# - enable.idempotence=true on producers
# - At-least-once delivery + inbox pattern
